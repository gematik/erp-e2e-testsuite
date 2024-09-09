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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.usecases.ClosePrescriptionCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.primsys.actors.Pharmacy;
import de.gematik.test.erezept.primsys.data.AcceptedPrescriptionDto;
import de.gematik.test.erezept.primsys.data.DispensedMedicationDto;
import de.gematik.test.erezept.primsys.data.PznDispensedMedicationDto;
import de.gematik.test.erezept.primsys.mapping.PznDispensedMedicationDataMapper;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import jakarta.ws.rs.core.Response;
import java.util.Date;
import java.util.List;
import lombok.val;

public class CloseUseCase {

  private CloseUseCase() {
    throw new AssertionError();
  }

  public static Response closePrescription(Pharmacy pharmacy, String taskId, String secret) {
    val acceptData = getPrescribedMedicationFromAccept(taskId);
    return closePrescription(pharmacy, taskId, new Secret(secret), List.of(acceptData), false);
  }

  public static Response closePrescription(
      Pharmacy pharmacy,
      String taskId,
      String secret,
      List<PznDispensedMedicationDto> dispenseMedications) {
    return closePrescription(pharmacy, taskId, new Secret(secret), dispenseMedications, true);
  }

  private static Response closePrescription(
      Pharmacy pharmacy,
      String prescriptionId,
      Secret secret,
      List<PznDispensedMedicationDto> medications,
      boolean isSubstituted) {

    val accepted = getAcceptedPrescription(prescriptionId);

    val medicationDispenses =
        medications.stream()
            .map(
                dispMedication ->
                    PznDispensedMedicationDataMapper.from(
                            dispMedication,
                            KVNR.from(accepted.getForKvnr()),
                            PrescriptionId.from(prescriptionId),
                            pharmacy.getSmcb().getTelematikId(),
                            isSubstituted)
                        .convert())
            .toList();

    val dispenseMedicationCommand =
        new ClosePrescriptionCommand(TaskId.from(prescriptionId), secret, medicationDispenses);
    val closeResponse = pharmacy.erpRequest(dispenseMedicationCommand);
    val body = closeResponse.getExpectedResource();

    val dispensedData = new DispensedMedicationDto();
    dispensedData.setPrescriptionId(prescriptionId);
    dispensedData.setSecret(secret.getValue());
    dispensedData.setAcceptData(accepted);
    dispensedData.setReceipt(body.getId());
    dispensedData.setDispensedDate(new Date());
    dispensedData.setMedications(medications);

    ActorContext.getInstance().addDispensedMedications(dispensedData);
    return Response.status(closeResponse.getStatusCode()).entity(dispensedData).build();
  }

  private static PznDispensedMedicationDto getPrescribedMedicationFromAccept(
      String prescriptionId) {
    val accepted = getAcceptedPrescription(prescriptionId);
    return PznDispensedMedicationDto.dispensed(accepted.getMedication())
        .withBatchInfo(GemFaker.fakerLotNumber(), GemFaker.fakerFutureExpirationDate());
  }

  public static AcceptedPrescriptionDto getAcceptedPrescription(String prescriptionId) {
    return ActorContext.getInstance()
        .getAcceptedPrescription(prescriptionId)
        .orElseThrow(
            () ->
                ErrorResponseBuilder.createInternalErrorException(
                    404,
                    format(
                        "no prescription with PrescriptionId {0} was accepted", prescriptionId)));
  }
}
