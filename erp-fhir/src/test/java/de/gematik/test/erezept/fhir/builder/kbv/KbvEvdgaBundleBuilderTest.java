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

package de.gematik.test.erezept.fhir.builder.kbv;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.kbv.MedicalOrganizationFaker.OrganizationFakerType;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItvEvdgaVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import de.gematik.test.erezept.fhir.valuesets.StatusKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class KbvEvdgaBundleBuilderTest extends ParsingTest {

  @ParameterizedTest
  @MethodSource
  void shouldBuildKbvEvdgaBundle(
      QualificationType qualificationType, VersicherungsArtDeBasis insuranceType) {
    val patient = PatientFaker.builder().withInsuranceType(VersicherungsArtDeBasis.GKV).fake();
    val practitioner = PractitionerFaker.builder().withQualificationType(qualificationType).fake();
    val insurance = KbvCoverageFaker.builder().withInsuranceType(insuranceType).fake();
    val medicalOrgFaker =
        (qualificationType.equals(QualificationType.DENTIST))
            ? MedicalOrganizationFaker.dentalPractice()
            : MedicalOrganizationFaker.medicalPractice();
    val medicalOrg = medicalOrgFaker.fake();

    val evdgaBundle =
        KbvEvdgaBundleBuilder.forPrescription(
                PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_162))
            .version(KbvItvEvdgaVersion.V1_0_0)
            .statusKennzeichen(StatusKennzeichen.NONE)
            .healthAppRequest(
                KbvHealthAppRequestFaker.forPatient(patient)
                    .withRequester(practitioner)
                    .withInsurance(insurance)
                    .withoutAccident()
                    .fake())
            .insurance(insurance)
            .patient(patient)
            .practitioner(practitioner)
            .medicalOrganization(medicalOrg)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, evdgaBundle);
    assertTrue(result.isSuccessful());
  }

  static Stream<Arguments> shouldBuildKbvEvdgaBundle() {
    return Stream.of(
        Arguments.of(QualificationType.DOCTOR, VersicherungsArtDeBasis.GKV),
        Arguments.of(QualificationType.DENTIST, VersicherungsArtDeBasis.GKV),
        Arguments.of(QualificationType.DOCTOR, VersicherungsArtDeBasis.SEL),
        Arguments.of(QualificationType.DENTIST, VersicherungsArtDeBasis.SEL));
  }

  @ParameterizedTest
  @MethodSource
  void shouldBuildKbvEvdgaBundleWithAccident(
      QualificationType qualificationType,
      VersicherungsArtDeBasis insuranceType,
      AccidentExtension accident) {
    val patient = PatientFaker.builder().withInsuranceType(VersicherungsArtDeBasis.GKV).fake();
    val practitioner = PractitionerFaker.builder().withQualificationType(qualificationType).fake();
    val insurance = KbvCoverageFaker.builder().withInsuranceType(insuranceType).fake();
    val medicalOrgFaker =
        (qualificationType.equals(QualificationType.DENTIST))
            ? MedicalOrganizationFaker.dentalPractice()
            : MedicalOrganizationFaker.medicalPractice();
    val medicalOrg = medicalOrgFaker.fake();

    val evdgaBundle =
        KbvEvdgaBundleBuilder.forPrescription(
                PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_162))
            .healthAppRequest(
                KbvHealthAppRequestFaker.forPatient(patient)
                    .withRequester(practitioner)
                    .withInsurance(insurance)
                    .withAccident(accident)
                    .fake())
            .insurance(insurance)
            .patient(patient)
            .practitioner(practitioner)
            .medicalOrganization(medicalOrg)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, evdgaBundle);
    assertTrue(result.isSuccessful());
  }

  static Stream<Arguments> shouldBuildKbvEvdgaBundleWithAccident() {
    return Stream.of(
        Arguments.of(
            QualificationType.DOCTOR, VersicherungsArtDeBasis.GKV, AccidentExtension.accident()),
        Arguments.of(
            QualificationType.DOCTOR,
            VersicherungsArtDeBasis.BG,
            AccidentExtension.occupationalDisease()),
        Arguments.of(
            QualificationType.DOCTOR,
            VersicherungsArtDeBasis.BG,
            AccidentExtension.accidentAtWork().atWorkplace()),
        Arguments.of(
            QualificationType.DENTIST, VersicherungsArtDeBasis.GKV, AccidentExtension.accident()),
        Arguments.of(
            QualificationType.DENTIST,
            VersicherungsArtDeBasis.BG,
            AccidentExtension.occupationalDisease()),
        Arguments.of(
            QualificationType.DENTIST,
            VersicherungsArtDeBasis.BG,
            AccidentExtension.accidentAtWork().atWorkplace()));
  }

  @ParameterizedTest
  @EnumSource(
      value = OrganizationFakerType.class,
      names = {"HOSPITAL", "HOSPITAL_KSN"})
  void shouldBuildKbvEvdgaBundleForHospital(OrganizationFakerType orgType) {
    val insuranceType = VersicherungsArtDeBasis.GKV;

    val patient = PatientFaker.builder().withInsuranceType(insuranceType).fake();
    val practitioner =
        PractitionerFaker.builder()
            .withQualificationType(QualificationType.DOCTOR_AS_REPLACEMENT)
            .fake();
    val attester =
        PractitionerFaker.builder().withQualificationType(QualificationType.DOCTOR).fake();
    val insurance = KbvCoverageFaker.builder().withInsuranceType(insuranceType).fake();
    val medicalOrg = MedicalOrganizationFaker.builder(orgType).fake();

    val evdgaBundle =
        KbvEvdgaBundleBuilder.forPrescription(
                PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_162))
            .healthAppRequest(
                KbvHealthAppRequestFaker.forPatient(patient)
                    .withRequester(practitioner)
                    .withInsurance(insurance)
                    .withoutAccident()
                    .fake())
            .insurance(insurance)
            .patient(patient)
            .practitioner(practitioner)
            .attester(attester)
            .medicalOrganization(medicalOrg)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, evdgaBundle);
    assertTrue(result.isSuccessful());
  }
}
