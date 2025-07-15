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

import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.primsys.actors.Pharmacy;
import de.gematik.test.erezept.primsys.data.PznDispensedMedicationDto;
import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.val;

public class DispenseUseCase extends AbstractDispensingUseCase {

  public DispenseUseCase(Pharmacy pharmacy) {
    super(pharmacy);
  }

  public Response dispensePrescription(String taskId, String secret) {
    val dispenseData = this.getPrescribedMedicationFromAccept(taskId);
    return dispensePrescription(taskId, Secret.from(secret), List.of(dispenseData), false);
  }

  public Response dispensePrescription(
      String taskId, String secret, List<PznDispensedMedicationDto> dispenseMedications) {
    return dispensePrescription(taskId, Secret.from(secret), dispenseMedications, true);
  }

  private Response dispensePrescription(
      String prescriptionId,
      Secret secret,
      List<PznDispensedMedicationDto> medications,
      boolean isSubstituted) {

    val accepted = this.getAcceptedPrescription(prescriptionId);

    val dispenseCommand =
        this.createDispenseCommand(prescriptionId, secret, medications, isSubstituted);
    val closeResponse = pharmacy.erpRequest(dispenseCommand);
    val body = closeResponse.getExpectedResource();

    val dispensedData =
        this.storeDispensedMedication(prescriptionId, secret, accepted, body.getId(), medications);
    return Response.status(closeResponse.getStatusCode()).entity(dispensedData).build();
  }
}
