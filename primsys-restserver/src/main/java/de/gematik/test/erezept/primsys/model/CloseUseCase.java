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
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.primsys.mapping.MedicationDataConverter;
import de.gematik.test.erezept.primsys.model.actor.BaseActor;
import de.gematik.test.erezept.primsys.rest.data.AcceptData;
import de.gematik.test.erezept.primsys.rest.data.DispensedData;
import de.gematik.test.erezept.primsys.rest.data.MedicationData;
import de.gematik.test.erezept.primsys.rest.request.DispenseRequestData;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.val;

public class CloseUseCase {

  private CloseUseCase() {
    throw new AssertionError();
  }

  public static Response closePrescription(BaseActor actor, String taskId, String secret) {
    val acceptData = getAcceptData(taskId);
    val bundle = getKbvErpBundle(acceptData, actor);
    return closePrescription(actor, taskId, new Secret(secret), List.of(bundle.getMedication()));
  }

  public static Response closePrescription(
      BaseActor actor, String taskId, String secret, DispenseRequestData dispenseRequestData) {
    val medications =
        dispenseRequestData.getMedications().stream()
            .map(m -> new MedicationDataConverter(m).convert())
            .toList();
    return closePrescription(actor, taskId, new Secret(secret), medications);
  }

  private static Response closePrescription(
      BaseActor actor, String taskId, Secret secret, List<KbvErpMedication> medications) {
    val acceptData = getAcceptData(taskId);
    val bundle = getKbvErpBundle(acceptData, actor);
    val erxMedicationDispense = getErxMedicationDispenses(actor, bundle, medications);
    val dispenseMedicationCommand =
        new DispenseMedicationCommand(TaskId.from(taskId), secret, erxMedicationDispense);
    val closeResponse = actor.erpRequest(dispenseMedicationCommand);
    if (closeResponse.isOperationOutcome()) {
      val oo = closeResponse.getAsOperationOutcome();
      String fdIssue = oo.getIssueFirstRep().getDiagnostics();
      String fdIssue2 = oo.getIssueFirstRep().getDetails().getText();
      val errorMessage = format("details:{0} / diagnostics: {1}", fdIssue, fdIssue2);
      return Response.status(400)
          .entity(
              Map.of(
                  "task-id",
                  taskId,
                  "task-status",
                  "in-progress",
                  "fd-issue",
                  errorMessage,
                  "fd-status-code",
                  closeResponse.getStatusCode()))
          .build();
      // Fehlerbehandlung!!
    } else {
      val body = closeResponse.getExpectedResource();
      val medData = MedicationData.fromKbvBundle(bundle);
      val dd = new DispensedData();
      dd.setTaskId(taskId);
      dd.setSecret(secret.getValue());
      dd.setAcceptData(acceptData);
      dd.setReceipt(body.getId());
      dd.setDispensedDate(new Date());
      dd.getMedications().add(medData);
      ActorContext.getInstance().addDispensedMedications(dd);
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

  private static AcceptData getAcceptData(String taskId) {
    return ActorContext.getInstance().getAcceptedPrescriptions().stream()
        .filter(x -> x.getTaskId().equals(taskId))
        .findFirst()
        .orElseThrow(
            () ->
                new WebApplicationException(
                    Response.status(404)
                        .entity(
                            new ErrorResponse(
                                format("no prescription with that taskId: {0} in system", taskId)))
                        .build()));
  }

  private static KbvErpBundle getKbvErpBundle(AcceptData acceptData, BaseActor actor) {
    return actor.getClient().getFhir().decode(KbvErpBundle.class, acceptData.getKbvBundle());
  }

  private static List<ErxMedicationDispense> getErxMedicationDispenses(
      BaseActor actor, KbvErpBundle bundle, List<KbvErpMedication> medications) {

    return medications.stream()
        .map(
            x ->
                ErxMedicationDispenseBuilder.forKvnr(bundle.getKvnr())
                    .performerId(actor.getSmcb().getTelematikId())
                    .prescriptionId(bundle.getPrescriptionId())
                    .medication(x)
                    .status("completed")
                    .whenPrepared(new Date())
                    .batch(GemFaker.fakerLotNumber(), GemFaker.fakerFutureExpirationDate())
                    .build())
        .toList();
  }
}
