/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.fhir.builder.kbv;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.builder.kbv.KbvMedicalOrganizationFaker.OrganizationFakerType;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.profiles.version.KbvItvEvdgaVersion;
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

/*@SetSystemProperty(
key = ERP_FHIR_PROFILES_TOGGLE,
value = "1.5.0") // before 1.4.0 EVDGA was not available*/
class KbvEvdgaBundleBuilderTest extends ErpFhirParsingTest {

  private static final KbvItvEvdgaVersion KBV_ITV_EVDGA_VERSION = KbvItvEvdgaVersion.V1_2;
  private static final KbvItaForVersion KBV_ITA_FOR_VERSION = KbvItaForVersion.V1_2_0;

  @ParameterizedTest
  @MethodSource("shouldBuildKbvEvdgaBundle")
  void shouldBuildKbvEvdgaBundleCorrect(
      QualificationType qualificationType, InsuranceTypeDe insuranceType) {
    val patient =
        KbvPatientFaker.builder(KBV_ITA_FOR_VERSION).withInsuranceType(InsuranceTypeDe.GKV).fake();
    val practitioner =
        KbvPractitionerFaker.builder(KBV_ITA_FOR_VERSION)
            .withQualificationType(qualificationType)
            .fake();
    val insurance =
        KbvCoverageFaker.builder(KBV_ITA_FOR_VERSION).withInsuranceType(insuranceType).fake();
    val medicalOrgFaker =
        (qualificationType.equals(QualificationType.DENTIST))
            ? KbvMedicalOrganizationFaker.dentalPractice(KBV_ITA_FOR_VERSION)
            : KbvMedicalOrganizationFaker.medicalPractice(KBV_ITA_FOR_VERSION);
    val medicalOrg = medicalOrgFaker.fake();

    val evdgaBundle =
        KbvEvdgaBundleBuilder.forPrescription(
                PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_162))
            .version(KBV_ITV_EVDGA_VERSION)
            .statusKennzeichen(StatusKennzeichen.NONE, practitioner)
            .healthAppRequest(
                KbvHealthAppRequestFaker.forPatient(patient, KBV_ITV_EVDGA_VERSION)
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

  @ParameterizedTest
  @MethodSource("shouldBuildKbvEvdgaBundle")
  void shouldBuildKbvEvdgaBundleWithPractitionerRole(
      QualificationType qualificationType, InsuranceTypeDe insuranceType) {

    val patient =
        KbvPatientFaker.builder(KBV_ITA_FOR_VERSION).withInsuranceType(InsuranceTypeDe.GKV).fake();

    val practitioner =
        KbvPractitionerFaker.builder(KBV_ITA_FOR_VERSION)
            .withQualificationType(qualificationType)
            .fake();

    val insurance =
        KbvCoverageFaker.builder(KBV_ITA_FOR_VERSION).withInsuranceType(insuranceType).fake();

    val medicalOrgFaker =
        (qualificationType.equals(QualificationType.DENTIST))
            ? KbvMedicalOrganizationFaker.dentalPractice(KBV_ITA_FOR_VERSION)
            : KbvMedicalOrganizationFaker.medicalPractice(KBV_ITA_FOR_VERSION);
    val medicalOrg = medicalOrgFaker.fake();

    val evdgaBundle =
        KbvEvdgaBundleBuilder.forPrescription(
                PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_162))
            .version(KBV_ITV_EVDGA_VERSION)
            .statusKennzeichen(StatusKennzeichen.ASV, practitioner, KBV_ITA_FOR_VERSION)
            .healthAppRequest(
                KbvHealthAppRequestFaker.forPatient(patient, KBV_ITV_EVDGA_VERSION)
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
    val patient =
        KbvPatientFaker.builder(KBV_ITA_FOR_VERSION).withInsuranceType(InsuranceTypeDe.GKV).fake();
    val practitioner =
        KbvPractitionerFaker.builder(KBV_ITA_FOR_VERSION)
            .withQualificationType(qualificationType)
            .fake();
    val insurance =
        KbvCoverageFaker.builder(KBV_ITA_FOR_VERSION).withInsuranceType(insuranceType).fake();
    val medicalOrgFaker =
        (qualificationType.equals(QualificationType.DENTIST))
            ? KbvMedicalOrganizationFaker.dentalPractice(KBV_ITA_FOR_VERSION)
            : KbvMedicalOrganizationFaker.medicalPractice(KBV_ITA_FOR_VERSION);
    val medicalOrg = medicalOrgFaker.fake();

    val evdgaBundle =
        KbvEvdgaBundleBuilder.forPrescription(
                PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_162))
            .version(KBV_ITV_EVDGA_VERSION)
            .healthAppRequest(
                KbvHealthAppRequestFaker.forPatient(patient, KBV_ITV_EVDGA_VERSION)
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
    val patient =
        KbvPatientFaker.builder(KBV_ITA_FOR_VERSION).withInsuranceType(insuranceType).fake();
    val practitioner =
        KbvPractitionerFaker.builder(KBV_ITA_FOR_VERSION)
            .withQualificationType(QualificationType.DOCTOR_AS_REPLACEMENT)
            .fake();
    val attester =
        KbvPractitionerFaker.builder(KBV_ITA_FOR_VERSION)
            .withQualificationType(QualificationType.DOCTOR)
            .fake();
    val insurance =
        KbvCoverageFaker.builder(KBV_ITA_FOR_VERSION).withInsuranceType(insuranceType).fake();
    val medicalOrg = KbvMedicalOrganizationFaker.builder(orgType, KBV_ITA_FOR_VERSION).fake();

    val evdgaBundle =
        KbvEvdgaBundleBuilder.forPrescription(
                PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_162))
            .version(KBV_ITV_EVDGA_VERSION)
            .healthAppRequest(
                KbvHealthAppRequestFaker.forPatient(patient, KBV_ITV_EVDGA_VERSION)
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
