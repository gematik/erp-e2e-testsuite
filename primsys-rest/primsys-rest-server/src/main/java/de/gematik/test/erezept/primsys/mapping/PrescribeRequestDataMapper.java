/*
 * Copyright 2023 gematik GmbH
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

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.AssignerOrganizationBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.MedicalOrganizationBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.PractitionerBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.primsys.data.MedicationRequestDto;
import de.gematik.test.erezept.primsys.data.PatientDto;
import de.gematik.test.erezept.primsys.data.PrescribeRequestDto;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import lombok.Getter;
import lombok.val;

@Getter
public class PrescribeRequestDataMapper extends BaseMapper<PrescribeRequestDto> {

  private PatientDataMapper patientMapper;
  private CoverageDataMapper coverageMapper;
  private PznMedicationDataMapper medicationMapper;

  public PrescribeRequestDataMapper(PrescribeRequestDto dto) {
    super(dto);
  }

  @Override
  protected void complete() {
    ensure(dto::getPatient, dto::setPatient, PatientDto::new);
    ensure(dto::getMedication, dto::setMedication, PznMedicationDataMapper::randomDto);
    ensure(dto::getMedicationRequest, dto::setMedicationRequest, MedicationRequestDto::new);
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
            this.medicationMapper, () -> PznMedicationDataMapper.from(dto.getMedication()));
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
    val practitioner = PractitionerBuilder.faker(doctorName).build();
    val organization = MedicalOrganizationBuilder.faker().build();
    val patient = this.getPatient();
    val coverage = this.getCoverage();
    val medication = this.getMedication();
    val medicationRequest = this.getMedicationRequest(practitioner);

    if (!this.getMedicationRequestMapper(practitioner).isMvoValid()) {
      // Note: probably a bad idea to throw this one from here!!
      throw ErrorResponseBuilder.createInternalErrorException(400, "MVO Data is invalid");
    }

    val builder =
        KbvErpBundleBuilder.forPrescription(GemFaker.fakerPrescriptionId())
            .practitioner(practitioner)
            .custodian(organization)
            .patient(patient)
            .insurance(coverage)
            .medicationRequest(medicationRequest)
            .medication(medication);

    val isPkv = coverage.getInsuranceKind() == VersicherungsArtDeBasis.PKV;
    val isOldProfile = KbvItaForVersion.getDefaultVersion().compareTo(KbvItaForVersion.V1_0_3) == 0;
    if (isPkv && isOldProfile) {
      // assigner organization was only required in KbvItaFor 1.0.3
      // for now,
      // we do not have the AssignerOrganization (which was faked anyway for getting a Reference +
      // Name
      // build a faked one matching the Reference of the patient
      builder.assigner(AssignerOrganizationBuilder.faker(patient).build());
    }

    return builder.build();
  }

  public static PrescribeRequestDataMapper from(PrescribeRequestDto dto) {
    return new PrescribeRequestDataMapper(dto);
  }
}
