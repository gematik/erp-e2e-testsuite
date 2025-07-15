/*
 * Copyright 2025 gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.primsys.model;

import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.primsys.actors.Pharmacy;
import de.gematik.test.erezept.primsys.data.PznDispensedMedicationDto;
import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.val;

public class CloseUseCase extends AbstractDispensingUseCase {

  public CloseUseCase(Pharmacy pharmacy) {
    super(pharmacy);
  }

  public Response closePrescription(String taskId, String secret) {
    val response =
        pharmacy.erpRequest(new TaskGetByIdCommand(TaskId.from(taskId), Secret.from(secret)));
    val prescriptionBundle = response.getExpectedResource();
    val hasMedicationDispense = prescriptionBundle.getTask().hasLastMedicationDispenseDate();

    List<PznDispensedMedicationDto> medications = List.of();
    if (!hasMedicationDispense) {
      val acceptData = this.getPrescribedMedicationFromAccept(taskId);
      medications = List.of(acceptData);
    }
    return closePrescription(taskId, Secret.from(secret), medications, false);
  }

  public Response closePrescription(
      String taskId, String secret, List<PznDispensedMedicationDto> dispenseMedications) {
    return closePrescription(taskId, Secret.from(secret), dispenseMedications, true);
  }

  private Response closePrescription(
      String prescriptionId,
      Secret secret,
      List<PznDispensedMedicationDto> medications,
      boolean isSubstituted) {

    val accepted = this.getAcceptedPrescription(prescriptionId);

    val closeCommand = this.createCloseCommand(prescriptionId, secret, medications, isSubstituted);
    val closeResponse = pharmacy.erpRequest(closeCommand);
    val body = closeResponse.getExpectedResource();

    val dispensedData =
        this.storeDispensedMedication(prescriptionId, secret, accepted, body.getId(), medications);
    return Response.status(closeResponse.getStatusCode()).entity(dispensedData).build();
  }
}
