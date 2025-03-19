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

import de.gematik.test.erezept.fhir.builder.kbv.KbvEvdgaBundleBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvMedicalOrganizationFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvPractitionerFaker;
import de.gematik.test.erezept.fhir.r4.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.r4.kbv.KbvEvdgaBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvHealthAppRequest;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.primsys.data.HealthAppRequestDto;
import de.gematik.test.erezept.primsys.data.PatientDto;
import de.gematik.test.erezept.primsys.data.PrescribeEvdgaRequestDto;
import java.util.Optional;
import lombok.Getter;
import lombok.val;

@Getter
public class PrescribeEvdgaRequestDataMapper extends BaseMapper<PrescribeEvdgaRequestDto> {

  private PatientDataMapper patientMapper;
  private CoverageDataMapper coverageMapper;

  public PrescribeEvdgaRequestDataMapper(PrescribeEvdgaRequestDto dto) {
    super(Optional.ofNullable(dto).orElseGet(PrescribeEvdgaRequestDto::new));
  }

  @Override
  protected void complete() {
    ensure(dto::getPatient, dto::setPatient, PatientDto::new);
    ensure(
        dto::getHealthAppRequest,
        dto::setHealthAppRequest,
        () -> HealthAppRequestDto.builder().build());
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
  }

  public KbvPatient getPatient() {
    return getPatientMapper().convert();
  }

  public KbvCoverage getCoverage() {
    return getCoverageMapper().convert();
  }

  public HealthAppRequestDataMapper getHealthAppRequestMapper(KbvPractitioner practitioner) {
    return HealthAppRequestDataMapper.from(dto.getHealthAppRequest())
        .requestedBy(practitioner)
        .requestedFor(getPatient())
        .coveredBy(getCoverage())
        .build();
  }

  public KbvHealthAppRequest getHealthAppRequest(KbvPractitioner practitioner) {
    return getHealthAppRequestMapper(practitioner).convert();
  }

  public KbvEvdgaBundle createEvdgaBundle(String doctorName) {
    val practitioner = KbvPractitionerFaker.builder().withName(doctorName).fake();
    val organization = KbvMedicalOrganizationFaker.forPractitioner(practitioner).fake();
    val patient = this.getPatient();
    val coverage = this.getCoverage();
    val healthAppRequest = this.getHealthAppRequest(practitioner);

    val builder =
        KbvEvdgaBundleBuilder.forPrescription(
                PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_162))
            .practitioner(practitioner)
            .medicalOrganization(organization)
            .patient(patient)
            .insurance(coverage)
            .healthAppRequest(healthAppRequest);

    return builder.build();
  }

  public static PrescribeEvdgaRequestDataMapper from(PrescribeEvdgaRequestDto dto) {
    return new PrescribeEvdgaRequestDataMapper(dto);
  }
}
