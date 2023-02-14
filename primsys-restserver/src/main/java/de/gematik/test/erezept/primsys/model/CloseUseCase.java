/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.primsys.model;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.usecases.DispenseMedicationCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.primsys.model.actor.BaseActor;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.Date;
import java.util.Map;
import lombok.val;

public class CloseUseCase {

  private CloseUseCase() {
    throw new AssertionError();
  }

  public static Response closePrescription(BaseActor actor, String taskId, String secret) {
    return closePrescription(actor, taskId, new Secret(secret));
  }

  private static Response closePrescription(BaseActor actor, String taskId, Secret secret) {
    var acceptDataOptional =
        ActorContext.getInstance().getAcceptedPrescriptions().stream()
            .filter(x -> x.getTaskId().equals(taskId))
            .findFirst();
    if (acceptDataOptional.isEmpty())
      throw new WebApplicationException(
          Response.status(404)
              .entity(
                  new ErrorResponse(
                      format("no prescription with that taskId: {0} in system", taskId)))
              .build());
    val acceptData = acceptDataOptional.get();
    val bundle = actor.getClient().getFhir().decode(KbvErpBundle.class, acceptData.getKbvBundle());
    val erxMedicationDispense =
        ErxMedicationDispenseBuilder.forKvid(bundle.getKvid())
            .performerId(actor.getSmcb().getTelematikId())
            .prescriptionId(bundle.getPrescriptionId())
            .medication(bundle.getMedication())
            .status("completed")
            .whenPrepared(new Date())
            .batch(GemFaker.fakerLotNumber(), GemFaker.fakerFutureExpirationDate())
            .build();
    val dispenseMedicationComand =
        new DispenseMedicationCommand(taskId, secret, erxMedicationDispense);
    var closeResponse = actor.erpRequest(dispenseMedicationComand);
    return Response.ok(
            Map.of(
                "task-id",
                taskId,
                "task-status",
                "closed",
                "fd-status-code",
                closeResponse.getStatusCode()))
        .build();
  }
}
