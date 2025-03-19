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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.parser.profiles.ProfileFhirParserFactory.ERP_FHIR_PROFILES_TOGGLE;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.builder.kbv.KbvMedicalOrganizationFaker.OrganizationFakerType;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import de.gematik.test.erezept.fhir.valuesets.StatusKennzeichen;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.SetSystemProperty;

@SetSystemProperty(
    key = ERP_FHIR_PROFILES_TOGGLE,
    value = "1.4.0") // before 1.4.0 EVDGA was not available
class KbvEvdgaBundleBuilderTest extends ErpFhirParsingTest {

  @ParameterizedTest
  @MethodSource
  void shouldBuildKbvEvdgaBundle(
      QualificationType qualificationType, InsuranceTypeDe insuranceType) {
    val patient = KbvPatientFaker.builder().withInsuranceType(InsuranceTypeDe.GKV).fake();
    val practitioner =
        KbvPractitionerFaker.builder().withQualificationType(qualificationType).fake();
    val insurance = KbvCoverageFaker.builder().withInsuranceType(insuranceType).fake();
    val medicalOrgFaker =
        (qualificationType.equals(QualificationType.DENTIST))
            ? KbvMedicalOrganizationFaker.dentalPractice()
            : KbvMedicalOrganizationFaker.medicalPractice();
    val medicalOrg = medicalOrgFaker.fake();

    val evdgaBundle =
        KbvEvdgaBundleBuilder.forPrescription(
                PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_162))
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
        Arguments.of(QualificationType.DOCTOR, InsuranceTypeDe.GKV),
        Arguments.of(QualificationType.DENTIST, InsuranceTypeDe.GKV),
        Arguments.of(QualificationType.DOCTOR, InsuranceTypeDe.SEL),
        Arguments.of(QualificationType.DENTIST, InsuranceTypeDe.SEL));
  }

  @ParameterizedTest
  @MethodSource
  void shouldBuildKbvEvdgaBundleWithAccident(
      QualificationType qualificationType,
      InsuranceTypeDe insuranceType,
      AccidentExtension accident) {
    val patient = KbvPatientFaker.builder().withInsuranceType(InsuranceTypeDe.GKV).fake();
    val practitioner =
        KbvPractitionerFaker.builder().withQualificationType(qualificationType).fake();
    val insurance = KbvCoverageFaker.builder().withInsuranceType(insuranceType).fake();
    val medicalOrgFaker =
        (qualificationType.equals(QualificationType.DENTIST))
            ? KbvMedicalOrganizationFaker.dentalPractice()
            : KbvMedicalOrganizationFaker.medicalPractice();
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
        Arguments.of(QualificationType.DOCTOR, InsuranceTypeDe.GKV, AccidentExtension.accident()),
        Arguments.of(
            QualificationType.DOCTOR, InsuranceTypeDe.BG, AccidentExtension.occupationalDisease()),
        Arguments.of(
            QualificationType.DOCTOR,
            InsuranceTypeDe.BG,
            AccidentExtension.accidentAtWork().atWorkplace()),
        Arguments.of(QualificationType.DENTIST, InsuranceTypeDe.GKV, AccidentExtension.accident()),
        Arguments.of(
            QualificationType.DENTIST, InsuranceTypeDe.BG, AccidentExtension.occupationalDisease()),
        Arguments.of(
            QualificationType.DENTIST,
            InsuranceTypeDe.BG,
            AccidentExtension.accidentAtWork().atWorkplace()));
  }

  @ParameterizedTest
  @EnumSource(
      value = OrganizationFakerType.class,
      names = {"HOSPITAL", "HOSPITAL_KSN"})
  void shouldBuildKbvEvdgaBundleForHospital(OrganizationFakerType orgType) {
    val insuranceType = InsuranceTypeDe.GKV;

    val patient = KbvPatientFaker.builder().withInsuranceType(insuranceType).fake();
    val practitioner =
        KbvPractitionerFaker.builder()
            .withQualificationType(QualificationType.DOCTOR_AS_REPLACEMENT)
            .fake();
    val attester =
        KbvPractitionerFaker.builder().withQualificationType(QualificationType.DOCTOR).fake();
    val insurance = KbvCoverageFaker.builder().withInsuranceType(insuranceType).fake();
    val medicalOrg = KbvMedicalOrganizationFaker.builder(orgType).fake();

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
