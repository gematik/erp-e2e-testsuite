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
 */

package de.gematik.test.erezept.primsys.mapping;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerAmount;

import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationRequestBuilder;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.r4.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import de.gematik.test.erezept.primsys.data.MedicationRequestDto;
import de.gematik.test.erezept.primsys.data.MvoDto;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;

public class MedicationRequestDataMapper
    extends DataMapper<MedicationRequestDto, KbvErpMedicationRequest> {

  private final KbvPatient patient;
  private final KbvCoverage insurance;
  private final KbvPractitioner practitioner;
  private final KbvErpMedication medication;

  public MedicationRequestDataMapper(
      MedicationRequestDto dto,
      KbvPatient patient,
      KbvCoverage insurance,
      KbvPractitioner practitioner,
      KbvErpMedication medication) {
    super(dto);
    this.patient = patient;
    this.insurance = insurance;
    this.practitioner = practitioner;
    this.medication = medication;
  }

  public static Builder from(KbvErpMedicationRequest medicationRequest) {
    val builder =
        MedicationRequestDto.medicationRequest()
            .dosage(medicationRequest.getDosageInstructionFirstRep().getText())
            .packageQuantity(medicationRequest.getDispenseQuantity())
            .substitutionAllowed(medicationRequest.allowSubstitution())
            .bvg(medicationRequest.isBvg())
            .emergencyFee(medicationRequest.hasEmergencyServiceFee());
    medicationRequest.getNoteText().ifPresent(builder::note);
    if (medicationRequest.isMultiple()) {
      val mvo = new MvoDto();
      medicationRequest.getMvoStart().ifPresent(mvo::setStartDate);
      medicationRequest.getMvoEnd().ifPresent(mvo::setEndDate);
      medicationRequest.getNumerator().ifPresent(mvo::setNumerator);
      medicationRequest.getDemoninator().ifPresent(mvo::setDenominator);
      builder.mvo(mvo);
    }
    return from(builder.build());
  }

  public static Builder from(MedicationRequestDto dto) {
    return new Builder(dto);
  }

  @Override
  protected void complete() {
    ensure(dto::getPackageQuantity, dto::setPackageQuantity, () -> fakerAmount(1, 20));
  }

  @Override
  protected KbvErpMedicationRequest convertInternal() {
    val builder =
        KbvErpMedicationRequestBuilder.forPatient(patient)
            .insurance(insurance)
            .requester(practitioner)
            .medication(medication)
            .dosage(dto.getDosage())
            .note(dto.getNote())
            .quantityPackages(dto.getPackageQuantity())
            .status("active")
            .intent("order")
            .isBVG(dto.isBvg())
            .hasEmergencyServiceFee(dto.isEmergencyFee())
            .substitution(dto.isSubstitutionAllowed())
            .coPaymentStatus(StatusCoPayment.STATUS_0); // default value for now

    val mvoData = dto.getMvo();
    if (mvoData != null) {
      val mvoBuilder =
          MultiplePrescriptionExtension.asMultiple(mvoData.getNumerator(), mvoData.getDenominator())
              .starting(mvoData.getStartDate());
      val mvoExtension =
          Optional.ofNullable(mvoData.getEndDate())
              .map(mvoBuilder::validUntil)
              .orElse(mvoBuilder.withoutEndDate());
      builder.mvo(mvoExtension);
    } else {
      builder.mvo(MultiplePrescriptionExtension.asNonMultiple());
    }
    return builder.build();
  }

  public boolean isMvoValid() {
    return dto.getMvo() == null || MvoDataMapper.from(dto.getMvo()).isValid();
  }

  @RequiredArgsConstructor
  public static class Builder {
    private final MedicationRequestDto dto;
    private KbvPatient patient;
    private KbvCoverage insurance;
    private KbvPractitioner practitioner;

    public Builder coveredBy(KbvCoverage insurance) {
      this.insurance = insurance;
      return this;
    }

    public Builder requestedBy(KbvPractitioner practitioner) {
      this.practitioner = practitioner;
      return this;
    }

    public Builder requestedFor(KbvPatient patient) {
      this.patient = patient;
      return this;
    }

    public MedicationRequestDataMapper forMedication(KbvErpMedication medication) {
      return new MedicationRequestDataMapper(dto, patient, insurance, practitioner, medication);
    }
  }
}
