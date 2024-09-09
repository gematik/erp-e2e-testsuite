/*
 * Copyright 2024 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.primsys.model;

import de.gematik.test.erezept.client.usecases.TaskAcceptCommand;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.primsys.actors.Pharmacy;
import de.gematik.test.erezept.primsys.data.AcceptedPrescriptionDto;
import de.gematik.test.erezept.primsys.mapping.CoverageDataMapper;
import de.gematik.test.erezept.primsys.mapping.PznMedicationDataMapper;
import jakarta.ws.rs.core.Response;
import lombok.val;

public class AcceptUseCase {

  private AcceptUseCase() {
    throw new AssertionError();
  }

  public static Response acceptPrescription(Pharmacy actor, String taskId, String accessCode) {
    return acceptPrescription(actor, TaskId.from(taskId), AccessCode.fromString(accessCode));
  }

  public static Response acceptPrescription(Pharmacy actor, TaskId taskId, AccessCode accessCode) {
    val acceptCommand = new TaskAcceptCommand(taskId, accessCode);
    val acceptResponse = actor.erpRequest(acceptCommand);
    val acceptedTask = acceptResponse.getExpectedResource();

    val kbvBundle = actor.decode(KbvErpBundle.class, acceptedTask.getKbvBundleAsString());
    val acceptData =
        AcceptedPrescriptionDto.withPrescriptionId(
                acceptedTask.getTask().getPrescriptionId().getValue())
            .forKvnr(kbvBundle.getPatient().getKvnr().getValue())
            .withAccessCode(acceptedTask.getTask().getAccessCode().getValue())
            .withSecret(acceptedTask.getSecret().getValue())
            .coveredBy(
                CoverageDataMapper.from(kbvBundle.getCoverage(), kbvBundle.getPatient()).getDto())
            .prescriptionReference(kbvBundle.getReference().getReference())
            .andMedication(PznMedicationDataMapper.from(kbvBundle.getMedication()).getDto());

    ActorContext.getInstance().addAcceptedPrescription(acceptData);
    return Response.status(acceptResponse.getStatusCode()).entity(acceptData).build();
  }
}
