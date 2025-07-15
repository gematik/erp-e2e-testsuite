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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.client.usecases.CloseTaskCommand;
import de.gematik.test.erezept.client.usecases.DispensePrescriptionAsBundleCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemDispenseCloseOperationPharmaceuticalsBuilder;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.primsys.actors.Pharmacy;
import de.gematik.test.erezept.primsys.data.AcceptedPrescriptionDto;
import de.gematik.test.erezept.primsys.data.DispensedMedicationDto;
import de.gematik.test.erezept.primsys.data.PznDispensedMedicationDto;
import de.gematik.test.erezept.primsys.mapping.GemErpMedicationDataMapper;
import de.gematik.test.erezept.primsys.mapping.PznDispensedMedicationDataMapper;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Parameters;

public abstract class AbstractDispensingUseCase {

  protected final ActorContext ctx;
  protected final Pharmacy pharmacy;

  protected AbstractDispensingUseCase(Pharmacy pharmacy) {
    this.ctx = ActorContext.getInstance();
    this.pharmacy = pharmacy;
  }

  protected DispensedMedicationDto storeDispensedMedication(
      String prescriptionId,
      Secret secret,
      AcceptedPrescriptionDto accepted,
      String receiptId,
      List<PznDispensedMedicationDto> medications) {

    val dispensedData = new DispensedMedicationDto();
    dispensedData.setPrescriptionId(prescriptionId);
    dispensedData.setSecret(secret.getValue());
    dispensedData.setAcceptData(accepted);
    dispensedData.setReceipt(receiptId);
    dispensedData.setDispensedDate(new Date());
    dispensedData.setMedications(medications);

    this.ctx.addDispensedMedications(dispensedData);
    return dispensedData;
  }

  protected AcceptedPrescriptionDto getAcceptedPrescription(String prescriptionId) {
    return ctx.getAcceptedPrescription(prescriptionId)
        .orElseThrow(
            () ->
                ErrorResponseBuilder.createInternalErrorException(
                    404,
                    format(
                        "no prescription with PrescriptionId {0} was accepted", prescriptionId)));
  }

  protected PznDispensedMedicationDto getPrescribedMedicationFromAccept(String prescriptionId) {
    val accepted = getAcceptedPrescription(prescriptionId);
    return PznDispensedMedicationDto.dispensed(accepted.getMedication())
        .withBatchInfo(GemFaker.fakerLotNumber(), GemFaker.fakerFutureExpirationDate());
  }

  /**
   * Creates medication dispenses for $dispense/$close operations prior 1.4.0 FHIR Profiles
   *
   * @param prescriptionId of the prescription
   * @param kvnr of the patient receiving the medication
   * @param medications which are dispensed by the pharmacy
   * @param isSubstituted denotes if the MedicationDispense contains substituted medications
   * @return a List of ErxMedicationDispenses to perform the $dispnse/$close operation
   */
  private List<ErxMedicationDispense> createMedicationDispenses(
      String prescriptionId,
      String kvnr,
      List<PznDispensedMedicationDto> medications,
      boolean isSubstituted) {

    return medications.stream()
        .map(
            dispMedication ->
                PznDispensedMedicationDataMapper.from(
                        dispMedication,
                        KVNR.from(kvnr),
                        PrescriptionId.from(prescriptionId),
                        pharmacy.getSmcb().getTelematikId(),
                        isSubstituted)
                    .convert())
        .toList();
  }

  /**
   * Creates the concrete Parameters-Structure for $dispense/$close operations after 1.3.0 FHIR
   * Profiles
   *
   * @param builder to be feed with the MedicationDispenses and Medications
   * @param prescriptionId of the prescription
   * @param kvnr of the patient receiving the medication
   * @param medications which are dispensed by the pharmacy
   * @param isSubstituted denotes if the MedicationDispense contains substituted medications
   * @param <P> is the concrete type of Parameters-Structure
   * @return a Parameters-Structure to perform the $dispense/$close operation
   */
  private <P extends Parameters> P feedOperationInputParameterBuilder(
      GemDispenseCloseOperationPharmaceuticalsBuilder<P> builder,
      String prescriptionId,
      String kvnr,
      List<PznDispensedMedicationDto> medications,
      boolean isSubstituted) {
    medications.forEach(
        dispMedication -> {
          val medication = GemErpMedicationDataMapper.from(dispMedication).convert();
          val md =
              PznDispensedMedicationDataMapper.from(
                      dispMedication,
                      KVNR.from(kvnr),
                      PrescriptionId.from(prescriptionId),
                      pharmacy.getSmcb().getTelematikId(),
                      isSubstituted)
                  .convert(medication);
          builder.with(md, medication);
        });
    return builder.build();
  }

  protected CloseTaskCommand createCloseCommand(
      String prescriptionId,
      Secret secret,
      List<PznDispensedMedicationDto> medications,
      boolean isSubstituted) {

    val kvnr = this.getAcceptedPrescription(prescriptionId).getForKvnr();

    if (shouldUseOldMedicationDispenseBundle()) {
      val medicationDispenses =
          this.createMedicationDispenses(prescriptionId, kvnr, medications, isSubstituted);
      return new CloseTaskCommand(TaskId.from(prescriptionId), secret, medicationDispenses);
    } else {
      if (medications.isEmpty()) {
        return new CloseTaskCommand(TaskId.from(prescriptionId), secret);
      } else {
        val operationBuilder = GemOperationInputParameterBuilder.forClosingPharmaceuticals();
        val operationParams =
            this.feedOperationInputParameterBuilder(
                operationBuilder, prescriptionId, kvnr, medications, isSubstituted);
        return new CloseTaskCommand(TaskId.from(prescriptionId), secret, operationParams);
      }
    }
  }

  protected DispensePrescriptionAsBundleCommand createDispenseCommand(
      String prescriptionId,
      Secret secret,
      List<PznDispensedMedicationDto> medications,
      boolean isSubstituted) {

    val kvnr = this.getAcceptedPrescription(prescriptionId).getForKvnr();

    if (shouldUseOldMedicationDispenseBundle()) {
      val medicationDispenses =
          this.createMedicationDispenses(prescriptionId, kvnr, medications, isSubstituted);
      return new DispensePrescriptionAsBundleCommand(
          TaskId.from(prescriptionId), secret, medicationDispenses);
    } else {
      val operationBuilder = GemOperationInputParameterBuilder.forDispensingPharmaceuticals();
      val operationParams =
          this.feedOperationInputParameterBuilder(
              operationBuilder, prescriptionId, kvnr, medications, isSubstituted);
      return new DispensePrescriptionAsBundleCommand(
          TaskId.from(prescriptionId), secret, operationParams);
    }
  }

  private boolean shouldUseOldMedicationDispenseBundle() {
    return ErpWorkflowVersion.getDefaultVersion().compareTo(ErpWorkflowVersion.V1_4) < 0;
  }
}
