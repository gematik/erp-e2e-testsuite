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

package de.gematik.test.erezept.primsys.mapping;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvMedicalOrganizationFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvPractitionerFaker;
import de.gematik.test.erezept.fhir.r4.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPractitioner;
import de.gematik.test.erezept.primsys.data.MedicationRequestDto;
import de.gematik.test.erezept.primsys.data.PatientDto;
import de.gematik.test.erezept.primsys.data.PrescribeRequestDto;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import java.util.Optional;
import lombok.Getter;
import lombok.val;

@Getter
public class PrescribeRequestDataMapper extends BaseMapper<PrescribeRequestDto> {

  private PatientDataMapper patientMapper;
  private CoverageDataMapper coverageMapper;
  private KbvPznMedicationDataMapper medicationMapper;

  public PrescribeRequestDataMapper(PrescribeRequestDto dto) {
    super(Optional.ofNullable(dto).orElseGet(PrescribeRequestDto::new));
  }

  @Override
  protected void complete() {
    ensure(dto::getPatient, dto::setPatient, PatientDto::new);
    ensure(dto::getMedication, dto::setMedication, KbvPznMedicationDataMapper::randomDto);
    ensure(
        dto::getMedicationRequest,
        dto::setMedicationRequest,
        () -> MedicationRequestDto.medicationRequest().build());
    this.completeInternal();
  }

  private void completeInternal() {
    this.patientMapper =
        this.getOrDefault(this.patientMapper, () -> PatientDataMapper.from(dto.getPatient()));

    ensure(
        dto::getCoverage,
        dto::setCoverage,
        () -> CoverageDataMapper.randomFor(this.getPatient()).dto);

    this.coverageMapper =
        this.getOrDefault(
            this.coverageMapper, () -> CoverageDataMapper.from(dto.getCoverage(), getPatient()));
    this.medicationMapper =
        this.getOrDefault(
            this.medicationMapper, () -> KbvPznMedicationDataMapper.from(dto.getMedication()));
  }

  public KbvPatient getPatient() {
    return getPatientMapper().convert();
  }

  public KbvCoverage getCoverage() {
    return getCoverageMapper().convert();
  }

  public KbvErpMedication getMedication() {
    return getMedicationMapper().convert();
  }

  public MedicationRequestDataMapper getMedicationRequestMapper(KbvPractitioner practitioner) {
    return MedicationRequestDataMapper.from(dto.getMedicationRequest())
        .requestedBy(practitioner)
        .requestedFor(getPatient())
        .coveredBy(getCoverage())
        .forMedication(getMedication());
  }

  public KbvErpMedicationRequest getMedicationRequest(KbvPractitioner practitioner) {
    return getMedicationRequestMapper(practitioner).convert();
  }

  public KbvErpBundle createKbvBundle(String doctorName) {
    val practitioner = KbvPractitionerFaker.builder().withName(doctorName).fake();
    val organization = KbvMedicalOrganizationFaker.forPractitioner(practitioner).fake();
    val patient = this.getPatient();
    val coverage = this.getCoverage();
    val medication = this.getMedication();
    val medicationRequest = this.getMedicationRequest(practitioner);

    if (!this.getMedicationRequestMapper(practitioner).isMvoValid()) {
      // Note: probably a bad idea to throw this one from here!!
      throw ErrorResponseBuilder.createInternalErrorException(400, "MVO Data is invalid");
    }

    return KbvErpBundleBuilder.forPrescription(GemFaker.fakerPrescriptionId())
        .practitioner(practitioner)
        .medicalOrganization(organization)
        .patient(patient)
        .insurance(coverage)
        .medicationRequest(medicationRequest)
        .medication(medication)
        .build();
  }

  public static PrescribeRequestDataMapper from(PrescribeRequestDto dto) {
    return new PrescribeRequestDataMapper(dto);
  }
}
