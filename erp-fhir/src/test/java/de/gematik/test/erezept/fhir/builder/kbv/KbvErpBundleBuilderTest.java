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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerDrugName;
import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.AsvFachgruppennummer;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.Country;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.DmpKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.PersonGroup;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import de.gematik.test.erezept.fhir.valuesets.StatusKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.VersichertenStatus;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.fhir.valuesets.Wop;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.ClearSystemProperty.ClearSystemProperties;

@Slf4j
class KbvErpBundleBuilderTest extends ParsingTest {

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV Bundle for GKV with versions KbvItaForVersion {0} and"
              + " KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void buildKbvBundleWithFixedValuesForGKV(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {
    val medicationResourceId = "c1e7027e-3c5b-4e87-a10a-572676b92e22";
    val medicationRequestResourceId = "75ec9d5d-07ec-44cf-b841-d8a4ef20e521";
    val patientResourceId = "c9e9eeb8-e397-4d62-a977-656a18027f90";
    val practitionerResourceId = "d8ac97db-249d-4f14-8c9b-861f8b93ca76";
    val organizationResourceId = "d55c6c01-057b-483d-a1eb-2bd1e182551f";
    val insuranceResourceId = "914e46d1-95a2-44c7-b900-5ca4ee80b8d5";

    ErpWorkflowNamingSystem prescriptionIdSystem;
    if (kbvErpVersion.compareTo(KbvItaErpVersion.V1_0_2) == 0) {
      prescriptionIdSystem = ErpWorkflowNamingSystem.PRESCRIPTION_ID;
    } else {
      prescriptionIdSystem = ErpWorkflowNamingSystem.PRESCRIPTION_ID_121;
    }
    // bundle values
    val prescriptionId = new PrescriptionId(prescriptionIdSystem, "160.100.000.000.011.09");

    val prescribingPractitioner =
        PractitionerBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(practitionerResourceId)
            .lanr("159753527")
            .name("Mia", "Meyer", "Dr.")
            .addQualification(QualificationType.DOCTOR_IN_TRAINING)
            .addQualification("Super-Facharzt für alles Mögliche")
            .build();

    val responsiblePractitioner =
        PractitionerBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(UUID.randomUUID().toString())
            .lanr(BaseANR.randomFromQualification(QualificationType.DOCTOR).getValue())
            .name("Emmett", "Brown", "Dr.")
            .addQualification(QualificationType.DOCTOR)
            .addQualification("Super-Facharzt für alles Mögliche")
            .build();

    val organization =
        MedicalOrganizationBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(organizationResourceId)
            .name("Arztpraxis Meyer")
            .bsnr("757299999")
            .phone("+490309876543")
            .email("info@praxis.de")
            .address("Berlin", "10623", "Wegelystraße 3")
            .build();

    val patient =
        PatientBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(patientResourceId)
            .kvnr(KVNR.random(), VersicherungsArtDeBasis.GKV)
            .name("Erwin", "Fleischer")
            .birthDate("09.07.1973")
            .address(Country.D, "Berlin", "10117", "Friedrichstraße 136")
            .build();

    val insurance =
        KbvCoverageBuilder.insurance("101377508", "Techniker-Krankenkasse")
            .version(kbvForVersion)
            .beneficiary(patient)
            .setResourceId(insuranceResourceId)
            .personGroup(PersonGroup.NOT_SET) // default NOT_SET
            .dmpKennzeichen(DmpKennzeichen.ASTHMA) // default NOT_SET
            .wop(Wop.BERLIN) // default DUMMY
            .versichertenStatus(VersichertenStatus.PENSIONER) // default MEMBERS
            .build();

    val medication =
        KbvErpMedicationPZNBuilder.builder()
            .version(kbvErpVersion)
            .setResourceId(medicationResourceId)
            .category(MedicationCategory.C_00) // default C_00
            .isVaccine(false) // default false
            .normgroesse(StandardSize.N1) // default NB (nicht betroffen)
            .darreichungsform(Darreichungsform.TKA) // default TAB
            .amount(5, "Stk") // default 10 {tbl}
            .pzn("04773414", "Doxycyclin AL 200 T, 10 Tabletten N1")
            .build();

    val medicationRequest =
        MedicationRequestBuilder.forPatient(patient)
            .version(kbvErpVersion)
            .setResourceId(medicationRequestResourceId)
            .insurance(insurance)
            .requester(prescribingPractitioner)
            .medication(medication)
            .dosage("1-0-0-0")
            .note("immer nur nach dem Essen")
            .quantityPackages(20)
            .status("active") // default ACTIVE
            .intent("order") // default ORDER
            .isBVG(false) // Bundesversorgungsgesetz default true
            .hasEmergencyServiceFee(true) // default false
            .substitution(false) // default true
            .coPaymentStatus(StatusCoPayment.STATUS_0) // default StatusCoPayment.STATUS_0
            .build();

    val kbvBundle =
        KbvErpBundleBuilder.forPrescription(prescriptionId)
            .version(kbvErpVersion)
            .practitioner(prescribingPractitioner)
            .attester(responsiblePractitioner)
            .medicalOrganization(organization)
            .patient(patient)
            .insurance(insurance)
            .statusKennzeichen("00") // 00/NONE is default
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication)
            .build();

    // check if all values has been set correctly
    assertNotNull(kbvBundle.getId());
    assertEquals(prescriptionId, kbvBundle.getPrescriptionId());

    val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
    assertTrue(result.isSuccessful());
    assertEquals(VersicherungsArtDeBasis.GKV, kbvBundle.getPatient().getInsuranceKind());
    assertFalse(kbvBundle.getPatient().getPkvAssigner().isPresent());
    assertFalse(kbvBundle.getPatient().getPkvAssignerName().isPresent());
    assertEquals(
        "1-0-0-0", kbvBundle.getMedicationRequest().getDosageInstructionFirstRep().getText());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV Bundle for PKV with versions KbvItaForVersion {0} and"
              + " KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void buildKbvBundleWithFixedValuesForPKV(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {
    val medicationResourceId = "c1e7027e-3c5b-4e87-a10a-572676b92e22";
    val medicationRequestResourceId = "75ec9d5d-07ec-44cf-b841-d8a4ef20e521";
    val patientResourceId = "c9e9eeb8-e397-4d62-a977-656a18027f90";
    val practitionerResourceId = "d8ac97db-249d-4f14-8c9b-861f8b93ca76";
    val organizationResourceId = "d55c6c01-057b-483d-a1eb-2bd1e182551f";
    val insuranceResourceId = "914e46d1-95a2-44c7-b900-5ca4ee80b8d5";

    // bundle values
    val prescriptionId = "160.100.000.000.011.09";

    val practitioner =
        PractitionerBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(practitionerResourceId)
            .zanr("159753527")
            .name("Mia", "Meyer", "Dr.")
            .addQualification(QualificationType.DENTIST)
            // .addQualification(AsvFachgruppennummer.from("123"))
            .addQualification("Super-Facharzt für alles Mögliche")
            .build();

    val medicalOrganization =
        MedicalOrganizationBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(organizationResourceId)
            .name("Arztpraxis Meyer")
            .bsnr("757299999")
            .phone("+490309876543")
            .email("info@praxis.de")
            .address(Country.D, "Berlin", "10623", "Wegelystraße 3")
            .build();

    val assignerOrganization =
        AssignerOrganizationBuilder.builder()
            .version(kbvForVersion)
            .name("Bayerische Beamtenkrankenkasse")
            .iknr("168141347")
            .phone("0301111111")
            .build();

    val patient =
        PatientBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(patientResourceId)
            .kvnr(KVNR.random(), VersicherungsArtDeBasis.PKV)
            .assigner(assignerOrganization)
            .name("Erwin", "Fleischer")
            .birthDate("09.07.1973")
            .address(Country.D, "Berlin", "10117", "Friedrichstraße 136")
            .build();

    val insurance =
        KbvCoverageBuilder.insurance("101377508", "Bayerische Beamtenkrankenkasse")
            .version(kbvForVersion)
            .beneficiary(patient)
            .setResourceId(insuranceResourceId)
            .personGroup(PersonGroup.NOT_SET) // default NOT_SET
            .dmpKennzeichen(DmpKennzeichen.ASTHMA) // default NOT_SET
            .wop(Wop.BERLIN) // default DUMMY
            .versichertenStatus(VersichertenStatus.PENSIONER) // default MEMBERS
            .build();

    val medication =
        KbvErpMedicationPZNBuilder.builder()
            .version(kbvErpVersion)
            .setResourceId(medicationResourceId)
            .category(MedicationCategory.C_00) // default C_00
            .isVaccine(false) // default false
            .normgroesse(StandardSize.N1) // default NB (nicht betroffen)
            .darreichungsform(Darreichungsform.TKA) // default TAB
            .amount(5, "Stk") // default 10 {tbl}
            .pzn("04773414", "Doxycyclin AL 200 T, 10 Tabletten N1")
            .build();

    val medicationRequest =
        MedicationRequestBuilder.forPatient(patient)
            .version(kbvErpVersion)
            .setResourceId(medicationRequestResourceId)
            .insurance(insurance)
            .requester(practitioner)
            .medication(medication)
            .dosage("1-0-0-0")
            .quantityPackages(20)
            .status("active") // default ACTIVE
            .intent("order") // default ORDER
            .isBVG(false) // Bundesversorgungsgesetz default true
            .mvo(MultiplePrescriptionExtension.asMultiple(1, 4).validForDays(360))
            .hasEmergencyServiceFee(true) // default false
            .substitution(false) // default true
            .coPaymentStatus(StatusCoPayment.STATUS_1) // default StatusCoPayment.STATUS_0
            .build();

    val kbvBundle =
        KbvErpBundleBuilder.forPrescription(prescriptionId)
            .version(kbvErpVersion)
            .practitioner(practitioner)
            .medicalOrganization(medicalOrganization)
            .patient(patient)
            .insurance(insurance)
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication)
            .assigner(assignerOrganization)
            .build();

    // check if all values has been set correctly
    assertNotNull(kbvBundle.getId());

    ErpWorkflowNamingSystem prescriptionIdSystem;
    if (kbvErpVersion.compareTo(KbvItaErpVersion.V1_0_2) == 0) {
      prescriptionIdSystem = ErpWorkflowNamingSystem.PRESCRIPTION_ID;
    } else {
      prescriptionIdSystem = ErpWorkflowNamingSystem.PRESCRIPTION_ID_121;
    }

    assertEquals(
        new PrescriptionId(prescriptionIdSystem, prescriptionId), kbvBundle.getPrescriptionId());

    val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
    assertTrue(result.isSuccessful());

    assertEquals(VersicherungsArtDeBasis.PKV, kbvBundle.getPatient().getInsuranceKind());
    assertTrue(kbvBundle.getPatient().getPkvAssigner().isPresent());

    if (kbvForVersion.compareTo(KbvItaForVersion.V1_1_0) < 0) {
      assertTrue(kbvBundle.getPatient().getPkvAssignerName().isPresent());
    } else {
      // well, that's how it's currently implemented: assigner is not set anymore!
      assertFalse(kbvBundle.getPatient().getPkvAssignerName().isPresent());
    }
  }

  @Test
  void buildKbvBundleWithFixedValuesAndNoAnrInPractitioner() {
    val medicationResourceId = "c1e7027e-3c5b-4e87-a10a-572676b92e22";
    val medicationRequestResourceId = "75ec9d5d-07ec-44cf-b841-d8a4ef20e521";
    val patientResourceId = "c9e9eeb8-e397-4d62-a977-656a18027f90";
    val practitionerResourceId = "d8ac97db-249d-4f14-8c9b-861f8b93ca76";
    val organizationResourceId = "d55c6c01-057b-483d-a1eb-2bd1e182551f";
    val insuranceResourceId = "914e46d1-95a2-44c7-b900-5ca4ee80b8d5";

    // bundle values
    val prescriptionId = "160.100.000.000.011.09";

    KbvItaForVersion kbvForVersion = KbvItaForVersion.V1_1_0;
    KbvItaErpVersion kbvErpVersion = KbvItaErpVersion.V1_1_0;
    val practitioner =
        PractitionerBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(practitionerResourceId)
            .name("Mia", "Meyer", "Dr.")
            .addQualification(QualificationType.DOCTOR_AS_REPLACEMENT)
            .addQualification(AsvFachgruppennummer.from("01"))
            .addQualification("Super-Facharzt für alles Mögliche")
            .build();

    val medicalOrganization =
        MedicalOrganizationBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(organizationResourceId)
            .name("Arztpraxis Meyer")
            .bsnr("757299999")
            .phone("+490309876543")
            .email("info@praxis.de")
            .address(Country.D, "Berlin", "10623", "Wegelystraße 3")
            .build();

    val assignerOrganization =
        AssignerOrganizationBuilder.builder()
            .version(kbvForVersion)
            .name("Bayerische Beamtenkrankenkasse")
            .iknr("168141347")
            .phone("0301111111")
            .build();

    val patient =
        PatientBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(patientResourceId)
            .kvnr(KVNR.random(), VersicherungsArtDeBasis.PKV)
            .assigner(assignerOrganization)
            .name("Erwin", "Fleischer")
            .birthDate("09.07.1973")
            .address(Country.D, "Berlin", "10117", "Friedrichstraße 136")
            .build();

    val insurance =
        KbvCoverageBuilder.insurance("101377508", "Bayerische Beamtenkrankenkasse")
            .version(kbvForVersion)
            .beneficiary(patient)
            .setResourceId(insuranceResourceId)
            .personGroup(PersonGroup.NOT_SET) // default NOT_SET
            .dmpKennzeichen(DmpKennzeichen.ASTHMA) // default NOT_SET
            .wop(Wop.BERLIN) // default DUMMY
            .versichertenStatus(VersichertenStatus.PENSIONER) // default MEMBERS
            .build();

    val medication =
        KbvErpMedicationPZNBuilder.builder()
            .version(kbvErpVersion)
            .setResourceId(medicationResourceId)
            .category(MedicationCategory.C_00) // default C_00
            .isVaccine(false) // default false
            .normgroesse(StandardSize.N1) // default NB (nicht betroffen)
            .darreichungsform(Darreichungsform.TKA) // default TAB
            .amount(5, "Stk") // default 10 {tbl}
            .pzn("04773414", "Doxycyclin AL 200 T, 10 Tabletten N1")
            .build();

    val medicationRequest =
        MedicationRequestBuilder.forPatient(patient)
            .version(kbvErpVersion)
            .setResourceId(medicationRequestResourceId)
            .insurance(insurance)
            .requester(practitioner)
            .medication(medication)
            .dosage("1-0-0-0")
            .quantityPackages(20)
            .status("active") // default ACTIVE
            .intent("order") // default ORDER
            .isBVG(false) // Bundesversorgungsgesetz default true
            .mvo(MultiplePrescriptionExtension.asMultiple(1, 4).validForDays(360))
            .hasEmergencyServiceFee(true) // default false
            .substitution(false) // default true
            .coPaymentStatus(StatusCoPayment.STATUS_1) // default StatusCoPayment.STATUS_0
            .build();

    val kbvBundle =
        KbvErpBundleBuilder.forPrescription(prescriptionId)
            .version(kbvErpVersion)
            .statusKennzeichen(StatusKennzeichen.ASV)
            .practitioner(practitioner)
            .medicalOrganization(medicalOrganization)
            .patient(patient)
            .insurance(insurance)
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication)
            .assigner(assignerOrganization)
            .build();

    // check if all values has been set correctly
    assertNotNull(kbvBundle.getId());
    val prescriptionIdSystem = ErpWorkflowNamingSystem.PRESCRIPTION_ID_121;
    assertEquals(
        new PrescriptionId(prescriptionIdSystem, prescriptionId), kbvBundle.getPrescriptionId());
    val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
    assertTrue(result.isSuccessful());
    assertEquals(VersicherungsArtDeBasis.PKV, kbvBundle.getPatient().getInsuranceKind());
    assertTrue(kbvBundle.getPatient().getPkvAssigner().isPresent());
    // well, that's how it's currently implemented: assigner is not set anymore!
    assertFalse(kbvBundle.getPatient().getPkvAssignerName().isPresent());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV Bundle for BG with Accident at Work and with versions"
              + " KbvItaForVersion {0} and KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void buildKbvBundleWithFixedValuesForBGWithAccidentAtWork(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {
    val medicationResourceId = "c1e7027e-3c5b-4e87-a10a-572676b92e22";
    val medicationRequestResourceId = "75ec9d5d-07ec-44cf-b841-d8a4ef20e521";
    val patientResourceId = "c9e9eeb8-e397-4d62-a977-656a18027f90";
    val practitionerResourceId = "d8ac97db-249d-4f14-8c9b-861f8b93ca76";
    val organizationResourceId = "d55c6c01-057b-483d-a1eb-2bd1e182551f";
    val insuranceResourceId = "914e46d1-95a2-44c7-b900-5ca4ee80b8d5";

    // bundle values
    val prescriptionId = "160.100.000.000.011.09";
    val practitioner =
        PractitionerBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(practitionerResourceId)
            .zanr("159753527")
            .name("Mia", "Meyer", "Dr.")
            .addQualification(QualificationType.DENTIST)
            .addQualification("Super-Facharzt für alles Mögliche")
            .build();

    val medicalOrganization =
        MedicalOrganizationBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(organizationResourceId)
            .name("Arztpraxis Meyer")
            .bsnr("757299999")
            .phone("+490309876543")
            .email("info@praxis.de")
            .address(Country.D, "Berlin", "10623", "Wegelystraße 3")
            .build();

    val assignerOrganization =
        AssignerOrganizationBuilder.builder()
            .version(kbvForVersion)
            .name("Bayerische Beamtenkrankenkasse")
            .iknr("168141347")
            .phone("0301111111")
            .build();

    val patient =
        PatientBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(patientResourceId)
            .kvnr(KVNR.random(), VersicherungsArtDeBasis.PKV)
            .assigner(assignerOrganization)
            .name("Erwin", "Fleischer")
            .birthDate("09.07.1973")
            .address(Country.D, "Berlin", "10117", "Friedrichstraße 136")
            .build();

    val insurance =
        KbvCoverageBuilder.insurance("101377508", "Bayerische Beamtenkrankenkasse")
            .version(kbvForVersion)
            .beneficiary(patient)
            .versicherungsArt(VersicherungsArtDeBasis.BG)
            .setResourceId(insuranceResourceId)
            .personGroup(PersonGroup.NOT_SET) // default NOT_SET
            .dmpKennzeichen(DmpKennzeichen.ASTHMA) // default NOT_SET
            .wop(Wop.BERLIN) // default DUMMY
            .versichertenStatus(VersichertenStatus.PENSIONER) // default MEMBERS
            .build();

    val medication =
        KbvErpMedicationPZNBuilder.builder()
            .version(kbvErpVersion)
            .setResourceId(medicationResourceId)
            .category(MedicationCategory.C_00) // default C_00
            .isVaccine(false) // default false
            .normgroesse(StandardSize.N1) // default NB (nicht betroffen)
            .darreichungsform(Darreichungsform.TKA) // default TAB
            .amount(5, "Stk") // default 10 {tbl}
            .pzn("04773414", "Doxycyclin AL 200 T, 10 Tabletten N1")
            .build();

    val medicationRequest =
        MedicationRequestBuilder.forPatient(patient)
            .version(kbvErpVersion)
            .setResourceId(medicationRequestResourceId)
            .insurance(insurance)
            .requester(practitioner)
            .medication(medication)
            .accident(AccidentExtension.accidentAtWork().atWorkplace())
            .dosage("1-0-0-0")
            .quantityPackages(20)
            .status("active") // default ACTIVE
            .intent("order") // default ORDER
            .isBVG(false) // Bundesversorgungsgesetz default true
            .mvo(MultiplePrescriptionExtension.asMultiple(1, 4).validForDays(360))
            .hasEmergencyServiceFee(true) // default false
            .substitution(false) // default true
            .coPaymentStatus(StatusCoPayment.STATUS_1) // default StatusCoPayment.STATUS_0
            .build();

    val kbvBundle =
        KbvErpBundleBuilder.forPrescription(prescriptionId)
            .version(kbvErpVersion)
            .practitioner(practitioner)
            .medicalOrganization(medicalOrganization)
            .patient(patient)
            .insurance(insurance)
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication)
            .assigner(assignerOrganization)
            .build();

    // check if all values has been set correctly
    assertNotNull(kbvBundle.getId());

    ErpWorkflowNamingSystem prescriptionIdSystem;
    if (kbvErpVersion.compareTo(KbvItaErpVersion.V1_0_2) == 0) {
      prescriptionIdSystem = ErpWorkflowNamingSystem.PRESCRIPTION_ID;
    } else {
      prescriptionIdSystem = ErpWorkflowNamingSystem.PRESCRIPTION_ID_121;
    }

    assertEquals(
        new PrescriptionId(prescriptionIdSystem, prescriptionId), kbvBundle.getPrescriptionId());

    val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
    assertTrue(result.isSuccessful());

    // patient is PKV insured
    assertEquals(VersicherungsArtDeBasis.PKV, kbvBundle.getPatient().getInsuranceKind());
    // but the payor is a BG
    assertEquals(VersicherungsArtDeBasis.BG, kbvBundle.getCoverage().getInsuranceKind());
    assertTrue(kbvBundle.getPatient().getPkvAssigner().isPresent());

    if (kbvForVersion.compareTo(KbvItaForVersion.V1_1_0) < 0) {
      assertTrue(kbvBundle.getPatient().getPkvAssignerName().isPresent());
    } else {
      // well, that's how it's currently implemented: assigner is not set anymore!
      assertFalse(kbvBundle.getPatient().getPkvAssignerName().isPresent());
    }
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV Bundle for PKV with Accident and with versions KbvItaForVersion"
              + " {0} and KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void buildKbvBundleWithFixedValuesForPKVWithAccident(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {
    val medicationResourceId = "c1e7027e-3c5b-4e87-a10a-572676b92e22";
    val medicationRequestResourceId = "75ec9d5d-07ec-44cf-b841-d8a4ef20e521";
    val patientResourceId = "c9e9eeb8-e397-4d62-a977-656a18027f90";
    val practitionerResourceId = "d8ac97db-249d-4f14-8c9b-861f8b93ca76";
    val organizationResourceId = "d55c6c01-057b-483d-a1eb-2bd1e182551f";
    val insuranceResourceId = "914e46d1-95a2-44c7-b900-5ca4ee80b8d5";

    // bundle values
    val prescriptionId = "160.100.000.000.011.09";

    val practitioner =
        PractitionerBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(practitionerResourceId)
            .zanr("159753527")
            .name("Mia", "Meyer", "Dr.")
            .addQualification(QualificationType.DENTIST)
            .addQualification("Super-Facharzt für alles Mögliche")
            .build();

    val medicalOrganization =
        MedicalOrganizationBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(organizationResourceId)
            .name("Arztpraxis Meyer")
            .bsnr("757299999")
            .phone("+490309876543")
            .email("info@praxis.de")
            .address(Country.D, "Berlin", "10623", "Wegelystraße 3")
            .build();

    val assignerOrganization =
        AssignerOrganizationBuilder.builder()
            .version(kbvForVersion)
            .name("Bayerische Beamtenkrankenkasse")
            .iknr("168141347")
            .phone("0301111111")
            .build();

    val patient =
        PatientBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(patientResourceId)
            .kvnr(KVNR.random(), VersicherungsArtDeBasis.PKV)
            .assigner(assignerOrganization)
            .name("Erwin", "Fleischer")
            .birthDate("09.07.1973")
            .address(Country.D, "Berlin", "10117", "Friedrichstraße 136")
            .build();

    val insurance =
        KbvCoverageBuilder.insurance("101377508", "Bayerische Beamtenkrankenkasse")
            .version(kbvForVersion)
            .beneficiary(patient)
            .versicherungsArt(VersicherungsArtDeBasis.PKV)
            .setResourceId(insuranceResourceId)
            .personGroup(PersonGroup.NOT_SET) // default NOT_SET
            .dmpKennzeichen(DmpKennzeichen.ASTHMA) // default NOT_SET
            .wop(Wop.BERLIN) // default DUMMY
            .versichertenStatus(VersichertenStatus.PENSIONER) // default MEMBERS
            .build();

    val medication =
        KbvErpMedicationPZNBuilder.builder()
            .version(kbvErpVersion)
            .setResourceId(medicationResourceId)
            .category(MedicationCategory.C_00) // default C_00
            .isVaccine(false) // default false
            .normgroesse(StandardSize.N1) // default NB (nicht betroffen)
            .darreichungsform(Darreichungsform.TKA) // default TAB
            .amount(5, "Stk") // default 10 {tbl}
            .pzn("04773414", "Doxycyclin AL 200 T, 10 Tabletten N1")
            .build();

    val medicationRequest =
        MedicationRequestBuilder.forPatient(patient)
            .version(kbvErpVersion)
            .setResourceId(medicationRequestResourceId)
            .insurance(insurance)
            .requester(practitioner)
            .medication(medication)
            .accident(AccidentExtension.accident())
            .dosage("1-0-0-0")
            .quantityPackages(20)
            .status("active") // default ACTIVE
            .intent("order") // default ORDER
            .isBVG(false) // Bundesversorgungsgesetz default true
            .mvo(MultiplePrescriptionExtension.asMultiple(1, 4).validForDays(360))
            .hasEmergencyServiceFee(true) // default false
            .substitution(false) // default true
            .coPaymentStatus(StatusCoPayment.STATUS_1) // default StatusCoPayment.STATUS_0
            .build();

    val kbvBundle =
        KbvErpBundleBuilder.forPrescription(prescriptionId)
            .version(kbvErpVersion)
            .practitioner(practitioner)
            .medicalOrganization(medicalOrganization)
            .patient(patient)
            .insurance(insurance)
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication)
            .assigner(assignerOrganization)
            .build();

    // check if all values has been set correctly
    assertNotNull(kbvBundle.getId());

    ErpWorkflowNamingSystem prescriptionIdSystem;
    if (kbvErpVersion.compareTo(KbvItaErpVersion.V1_0_2) == 0) {
      prescriptionIdSystem = ErpWorkflowNamingSystem.PRESCRIPTION_ID;
    } else {
      prescriptionIdSystem = ErpWorkflowNamingSystem.PRESCRIPTION_ID_121;
    }

    assertEquals(
        new PrescriptionId(prescriptionIdSystem, prescriptionId), kbvBundle.getPrescriptionId());

    val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
    assertTrue(result.isSuccessful());

    // patient is PKV insured
    assertEquals(VersicherungsArtDeBasis.PKV, kbvBundle.getPatient().getInsuranceKind());
    // but the payor is also PKV
    assertEquals(VersicherungsArtDeBasis.PKV, kbvBundle.getCoverage().getInsuranceKind());
    assertTrue(kbvBundle.getPatient().getPkvAssigner().isPresent());

    if (kbvForVersion.compareTo(KbvItaForVersion.V1_1_0) < 0) {
      assertTrue(kbvBundle.getPatient().getPkvAssignerName().isPresent());
    } else {
      // well, that's how it's currently implemented: assigner is not set anymore!
      assertFalse(kbvBundle.getPatient().getPkvAssignerName().isPresent());
    }
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV Bundle for PKV with Accident at Work and invalid Coverage Type"
              + " with versions KbvItaForVersion {0} and KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void buildKbvBundleForPKVWithAccidentAndInvalidCoverage(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {

    // bundle values
    val prescriptionId = "160.100.000.000.011.09";
    val practitioner = PractitionerFaker.builder().withVersion(kbvForVersion).fake();

    val medicalOrgBuilder =
        (kbvForVersion.compareTo(KbvItaForVersion.V1_1_0) < 0)
            ? MedicalOrganizationFaker.medicalPractice()
            : MedicalOrganizationFaker.builder();
    val medicalOrganization = medicalOrgBuilder.withVersion(kbvForVersion).fake();

    val assignerOrganization =
        AssignerOrganizationFaker.builder().withVersion(kbvForVersion).fake();
    val patient =
        PatientFaker.builder()
            .withKvnrAndInsuranceType(KVNR.random(), VersicherungsArtDeBasis.PKV)
            .withVersion(kbvForVersion)
            .withAssignerRef(assignerOrganization)
            .fake();
    val insurance =
        KbvCoverageFaker.builder()
            .withInsuranceType(VersicherungsArtDeBasis.PKV)
            .withVersion(kbvForVersion)
            .withBeneficiary(patient)
            .fake();
    val medication = KbvErpMedicationPZNFaker.builder().withVersion(kbvErpVersion).fake();
    val medicationRequest =
        MedicationRequestFaker.builder()
            .withPatient(patient)
            .withVersion(kbvErpVersion)
            .withInsurance(insurance)
            .withRequester(practitioner)
            .withMedication(medication)
            .withAccident(AccidentExtension.accidentAtWork().atWorkplace())
            .fake();

    val kbvBundle =
        KbvErpBundleBuilder.forPrescription(prescriptionId)
            .version(kbvErpVersion)
            .practitioner(practitioner)
            .medicalOrganization(medicalOrganization)
            .patient(patient)
            .insurance(insurance)
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication)
            .assigner(assignerOrganization)
            .build();

    if (kbvForVersion.compareTo(KbvItaForVersion.V1_1_0) < 0) {
      // accident at work and PKV insurance is allowed for old profiles
      val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
      assertTrue(result.isSuccessful());
    } else {
      // but accident at work and PKV insurance is not allowed for new profiles
      val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
      assertFalse(result.isSuccessful());
    }
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build faked KBV Bundle with versions KbvItaForVersion {0} and"
              + " KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void buildKbvBundleWithFaker(KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {

    for (int i = 0; i < 5; i++) {
      val practitioner = PractitionerFaker.builder().withVersion(kbvForVersion).fake();
      val medicalOrganization =
          MedicalOrganizationFaker.medicalPractice().withVersion(kbvForVersion).fake();
      val assignerOrganization =
          AssignerOrganizationFaker.builder().withVersion(kbvForVersion).fake();

      val patient =
          PatientFaker.builder()
              .withAssignerRef(assignerOrganization)
              .withVersion(kbvForVersion)
              .fake();
      val insurance =
          KbvCoverageFaker.builder().withBeneficiary(patient).withVersion(kbvForVersion).fake();
      val medication = KbvErpMedicationPZNFaker.builder().withVersion(kbvErpVersion).fake();

      val kbvBundle =
          KbvErpBundleFaker.builder()
              .withVersion(kbvErpVersion)
              .withPractitioner(practitioner)
              .withCustodian(medicalOrganization)
              .withAssignerOrganization(assignerOrganization)
              .withPatient(patient)
              .withInsurance(insurance, patient)
              .withMedicationRequestVersion(kbvErpVersion)
              .withMedication(medication)
              .fake();
      log.info(format("Validating Faker KBV-Bundle with ID {0}", kbvBundle.getPrescriptionId()));
      val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
      assertTrue(result.isSuccessful());
    }
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build faked KBV Bundle with versions KbvItaForVersion {0} and"
              + " KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  @ClearSystemProperties(
      value = {
        @ClearSystemProperty(key = "kbv.ita.for"),
        @ClearSystemProperty(key = "kbv.ita.erp")
      })
  void buildKbvBundleWithSuperFaker(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {
    System.setProperty(kbvErpVersion.getCustomProfile().getName(), kbvErpVersion.getVersion());
    System.setProperty(kbvForVersion.getCustomProfile().getName(), kbvForVersion.getVersion());

    for (int i = 0; i < 5; i++) {
      val kbvBundle =
          KbvErpBundleFaker.builder()
              .withKvnr(KVNR.random())
              .withPznAndMedicationName(PZN.from("04773414"), fakerDrugName())
              .fake();
      log.info(
          format(
              "Validating Faker KBV-Bundle {0} with ID {1}",
              kbvBundle.getMetaProfileVersion(), kbvBundle.getPrescriptionId()));
      val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
      assertTrue(result.isSuccessful());
    }
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build faked KBV Bundle with versions KbvItaForVersion {0} and"
              + " KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  @ClearSystemProperties(
      value = {
        @ClearSystemProperty(key = "kbv.ita.for"),
        @ClearSystemProperty(key = "kbv.ita.erp")
      })
  void buildKbvBundleWithSuperFaker02(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {

    System.setProperty(kbvErpVersion.getCustomProfile().getName(), kbvErpVersion.getVersion());
    System.setProperty(kbvForVersion.getCustomProfile().getName(), kbvForVersion.getVersion());

    for (int i = 0; i < 1; i++) {
      val kbvBundle = KbvErpBundleFaker.builder().withKvnr(KVNR.random()).fake();
      log.info(
          format(
              "Validating Faker KBV-Bundle {0} with ID {1}",
              kbvBundle.getMetaProfileVersion(), kbvBundle.getPrescriptionId()));
      val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
      assertTrue(result.isSuccessful());
    }
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build faked KBV Bundle with versions KbvItaForVersion {0} and"
              + " KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  @ClearSystemProperties(
      value = {
        @ClearSystemProperty(key = "kbv.ita.for"),
        @ClearSystemProperty(key = "kbv.ita.erp")
      })
  void buildKbvBundleWithSuperFaker03(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {
    System.setProperty(kbvErpVersion.getCustomProfile().getName(), kbvErpVersion.getVersion());
    System.setProperty(kbvForVersion.getCustomProfile().getName(), kbvForVersion.getVersion());

    for (int i = 0; i < 5; i++) {
      val kbvBundle =
          KbvErpBundleFaker.builder()
              .withKvnr(KVNR.random())
              .withPrescriptionId(new PrescriptionId("160.002.362.150.600.45"))
              .fake();

      log.info(
          format(
              "Validating Faker KBV-Bundle {0} with ID {1}",
              kbvBundle.getMetaProfileVersion(), kbvBundle.getPrescriptionId()));
      val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
      assertTrue(result.isSuccessful());
    }
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Change authoredOn Date in faked KBV Bundle with versions KbvItaForVersion"
              + " {0} and KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  @ClearSystemProperties(
      value = {
        @ClearSystemProperty(key = "kbv.ita.for"),
        @ClearSystemProperty(key = "kbv.ita.erp")
      })
  void buildAndChangeAuthoredOnDate(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {

    System.setProperty(kbvErpVersion.getCustomProfile().getName(), kbvErpVersion.getVersion());
    System.setProperty(kbvForVersion.getCustomProfile().getName(), kbvForVersion.getVersion());

    val authoredOn = Date.from(Instant.now().minus(2, ChronoUnit.DAYS));
    val kbvBundle =
        KbvErpBundleFaker.builder()
            .withKvnr(KVNR.random())
            .withAuthorDate(authoredOn)
            .withPrescriptionId(new PrescriptionId("160.002.362.150.600.45"))
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
    assertTrue(result.isSuccessful());

    val expected = authoredOn.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    val actual = kbvBundle.getAuthoredOn().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    assertEquals(expected, actual);
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build incomplete KBV Bundle with versions KbvItaForVersion {0} and"
              + " KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldThrowOnEmptyKbvBundleBuilder(KbvItaErpVersion kbvErpVersion) {

    val kb = KbvErpBundleBuilder.forPrescription(PrescriptionId.random()).version(kbvErpVersion);
    assertThrows(BuilderException.class, kb::build);
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build incomplete KBV Bundle with versions KbvItaForVersion {0} and"
              + " KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void buildFakerWithGivenMedicationCategory(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {

    val medication =
        KbvErpMedicationPZNFaker.builder()
            .withVersion(kbvErpVersion)
            .withCategory(MedicationCategory.C_00)
            .fake();
    val coverage =
        KbvCoverageFaker.builder()
            .withInsuranceType(VersicherungsArtDeBasis.GKV)
            .withVersion(kbvForVersion)
            .fake();
    val patient =
        PatientFaker.builder()
            .withKvnrAndInsuranceType(KVNR.random(), VersicherungsArtDeBasis.GKV)
            .withVersion(kbvForVersion)
            .fake();
    val practitioner = PractitionerFaker.builder().withVersion(kbvForVersion).fake();
    val custodian = MedicalOrganizationFaker.medicalPractice().withVersion(kbvForVersion).fake();

    val mvo = MultiplePrescriptionExtension.asNonMultiple();
    val kbvBundleBuilder =
        KbvErpBundleBuilder.builder()
            .version(kbvErpVersion)
            .medication(medication)
            .medicationRequest(
                MedicationRequestFaker.builder()
                    .withPatient(patient)
                    .withVersion(kbvErpVersion)
                    .withMedication(medication)
                    .withMvo(mvo)
                    .withInsurance(coverage)
                    .withRequester(practitioner)
                    .fake());

    kbvBundleBuilder
        .insurance(coverage)
        .patient(patient)
        .practitioner(practitioner)
        .medicalOrganization(custodian)
        .prescriptionId(PrescriptionId.random());

    val result = ValidatorUtil.encodeAndValidate(parser, kbvBundleBuilder.build());
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Get Practitioner from KBV Bundle with versions KbvItaForVersion {0} and"
              + " KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void shouldGetPractitionersFromKbvBundle(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {

    val qualificationTypes = List.of(QualificationType.DOCTOR, QualificationType.DENTIST);

    qualificationTypes.forEach(
        qt -> {
          val practitioner =
              PractitionerFaker.builder()
                  .withVersion(kbvForVersion)
                  .withAnr(BaseANR.randomFromQualification(qt))
                  .fake();
          val kbvBundle =
              KbvErpBundleFaker.builder()
                  .withKvnr(KVNR.random())
                  .withVersion(kbvErpVersion)
                  .withPractitioner(practitioner)
                  .fake();
          val actual = kbvBundle.getPractitioner();
          assertEquals(practitioner.getANRType(), actual.getANRType());
        });
  }

  @Test
  void shouldBuildBundleWithSupplyRequestCorrect() {
    KbvItaForVersion kbvForVersion = KbvItaForVersion.V1_1_0;
    KbvItaErpVersion kbvErpVersion = KbvItaErpVersion.V1_1_0;

    val medicationResourceId = "c1e7027e-3c5b-4e87-a10a-572676b92e22";
    val patientResourceId = "c9e9eeb8-e397-4d62-a977-656a18027f90";
    val practitionerResourceId = "d8ac97db-249d-4f14-8c9b-861f8b93ca76";
    val organizationResourceId = "d55c6c01-057b-483d-a1eb-2bd1e182551f";
    val insuranceResourceId = "914e46d1-95a2-44c7-b900-5ca4ee80b8d5";

    val prescriptionIdSystem = ErpWorkflowNamingSystem.PRESCRIPTION_ID_121;
    // bundle values
    val prescriptionId = new PrescriptionId(prescriptionIdSystem, "160.100.000.000.011.09");

    val practitioner =
        PractitionerBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(practitionerResourceId)
            .lanr("159753527")
            .name("Mia", "Meyer", "Dr.")
            .addQualification(QualificationType.DOCTOR)
            .addQualification("Super-Facharzt für alles Mögliche")
            .build();

    val organization =
        MedicalOrganizationBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(organizationResourceId)
            .name("Arztpraxis Meyer")
            .bsnr("757299999")
            .phone("+490309876543")
            .email("info@praxis.de")
            .address("Berlin", "10623", "Wegelystraße 3")
            .build();

    val patient =
        PatientBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(patientResourceId)
            .kvnr(KVNR.random(), VersicherungsArtDeBasis.GKV)
            .name("Erwin", "Fleischer")
            .birthDate("09.07.1973")
            .address(Country.D, "Berlin", "10117", "Friedrichstraße 136")
            .build();

    val insurance =
        KbvCoverageBuilder.insurance("101377508", "Techniker-Krankenkasse")
            .version(kbvForVersion)
            .beneficiary(patient)
            .setResourceId(insuranceResourceId)
            .personGroup(PersonGroup.NOT_SET) // default NOT_SET
            .dmpKennzeichen(DmpKennzeichen.ASTHMA) // default NOT_SET
            .wop(Wop.BERLIN) // default DUMMY
            .versichertenStatus(VersichertenStatus.PENSIONER) // default MEMBERS
            .build();

    val medication =
        KbvErpMedicationPZNBuilder.builder()
            .version(kbvErpVersion)
            .setResourceId(medicationResourceId)
            .category(MedicationCategory.C_00) // default C_00
            .isVaccine(false) // default false
            .normgroesse(StandardSize.N1) // default NB (nicht betroffen)
            .darreichungsform(Darreichungsform.TKA) // default TAB
            .amount(5, "Stk") // default 10 {tbl}
            .pzn("04773414", "Doxycyclin AL 200 T, 10 Tabletten N1")
            .build();

    val supplyRequest =
        SupplyRequestBuilder.withCoverage(insurance)
            .requester(practitioner)
            .medication(medication)
            .build();
    val kbvBundle =
        KbvErpBundleBuilder.forPrescription(prescriptionId)
            .version(kbvErpVersion)
            .practitioner(practitioner)
            .medicalOrganization(organization)
            .patient(patient)
            .insurance(insurance)
            .statusKennzeichen("00") // 00/NONE is default
            .supplyRequest(supplyRequest)
            .medication(medication)
            .build();

    // check if all values has been set correctly
    assertNotNull(kbvBundle.getId());
    assertEquals(prescriptionId, kbvBundle.getPrescriptionId());
    val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
    assertTrue(result.isSuccessful());
    assertEquals(VersicherungsArtDeBasis.GKV, kbvBundle.getPatient().getInsuranceKind());
    assertFalse(kbvBundle.getPatient().getPkvAssigner().isPresent());
    assertFalse(kbvBundle.getPatient().getPkvAssignerName().isPresent());
  }

  @Test
  void shouldBuildInvalidBundleWithSupplyRequestAndMedicationRequest() {

    KbvItaForVersion kbvForVersion = KbvItaForVersion.V1_1_0;
    KbvItaErpVersion kbvErpVersion =
        de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion.V1_1_0;

    val medicationResourceId = "c1e7027e-3c5b-4e87-a10a-572676b92e22";
    val medicationRequestResourceId = "75ec9d5d-07ec-44cf-b841-d8a4ef20e521";
    val supplyRequestResourceId = UUID.randomUUID().toString();
    val patientResourceId = "c9e9eeb8-e397-4d62-a977-656a18027f90";
    val practitionerResourceId = "d8ac97db-249d-4f14-8c9b-861f8b93ca76";
    val organizationResourceId = "d55c6c01-057b-483d-a1eb-2bd1e182551f";
    val insuranceResourceId = "914e46d1-95a2-44c7-b900-5ca4ee80b8d5";

    val prescriptionIdSystem = ErpWorkflowNamingSystem.PRESCRIPTION_ID_121;

    // bundle values
    val prescriptionId = new PrescriptionId(prescriptionIdSystem, "160.100.000.000.011.09");

    val practitioner =
        PractitionerBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(practitionerResourceId)
            .lanr("159753527")
            .name("Mia", "Meyer", "Dr.")
            .addQualification(QualificationType.DOCTOR)
            .addQualification("Super-Facharzt für alles Mögliche")
            .build();

    val organization =
        MedicalOrganizationBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(organizationResourceId)
            .name("Arztpraxis Meyer")
            .bsnr("757299999")
            .phone("+490309876543")
            .email("info@praxis.de")
            .address("Berlin", "10623", "Wegelystraße 3")
            .build();

    val patient =
        PatientBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(patientResourceId)
            .kvnr(KVNR.random(), VersicherungsArtDeBasis.GKV)
            .name("Erwin", "Fleischer")
            .birthDate("09.07.1973")
            .address(Country.D, "Berlin", "10117", "Friedrichstraße 136")
            .build();

    val insurance =
        KbvCoverageBuilder.insurance("101377508", "Techniker-Krankenkasse")
            .version(kbvForVersion)
            .beneficiary(patient)
            .setResourceId(insuranceResourceId)
            .personGroup(PersonGroup.NOT_SET) // default NOT_SET
            .dmpKennzeichen(DmpKennzeichen.ASTHMA) // default NOT_SET
            .wop(Wop.BERLIN) // default DUMMY
            .versichertenStatus(VersichertenStatus.PENSIONER) // default MEMBERS
            .build();

    val medication =
        KbvErpMedicationPZNBuilder.builder()
            .version(kbvErpVersion)
            .setResourceId(medicationResourceId)
            .category(MedicationCategory.C_00) // default C_00
            .isVaccine(false) // default false
            .normgroesse(StandardSize.N1) // default NB (nicht betroffen)
            .darreichungsform(Darreichungsform.TKA) // default TAB
            .amount(5, "Stk") // default 10 {tbl}
            .pzn("04773414", "Doxycyclin AL 200 T, 10 Tabletten N1")
            .build();

    val medicationRequest =
        MedicationRequestBuilder.forPatient(patient)
            .version(kbvErpVersion)
            .setResourceId(medicationRequestResourceId)
            .insurance(insurance)
            .requester(practitioner)
            .medication(medication)
            .dosage("1-0-0-0")
            .quantityPackages(20)
            .status("active") // default ACTIVE
            .intent("order") // default ORDER
            .isBVG(false) // Bundesversorgungsgesetz default true
            .hasEmergencyServiceFee(true) // default false
            .substitution(false) // default true
            .coPaymentStatus(StatusCoPayment.STATUS_0) // default StatusCoPayment.STATUS_0
            .build();
    val supplyRequest =
        SupplyRequestBuilder.withCoverage(insurance)
            .setResourceId(supplyRequestResourceId)
            .requester(practitioner)
            .medication(medication)
            .build();
    val kbvBundle =
        KbvErpBundleBuilder.forPrescription(prescriptionId)
            .version(kbvErpVersion)
            .practitioner(practitioner)
            .medicalOrganization(organization)
            .patient(patient)
            .insurance(insurance)
            .statusKennzeichen("00") // 00/NONE is default
            .medicationRequest(medicationRequest) // what is the medication
            .supplyRequest(supplyRequest)
            .medication(medication)
            .build();

    // check if all values has been set correctly
    assertNotNull(kbvBundle.getId());
    assertEquals(prescriptionId, kbvBundle.getPrescriptionId());
    val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
    assertFalse(result.isSuccessful());
    assertEquals(VersicherungsArtDeBasis.GKV, kbvBundle.getPatient().getInsuranceKind());
    assertFalse(kbvBundle.getPatient().getPkvAssigner().isPresent());
    assertFalse(kbvBundle.getPatient().getPkvAssignerName().isPresent());
    assertEquals(supplyRequestResourceId, kbvBundle.getEntry().get(7).getResource().getId());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build incomplete KBV Bundle with MedicationCompounding and versions"
              + " KbvItaForVersion {0} and KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void buildFakerWithGivenMedicationCompounding(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {

    val medicationResourceId = "c1e7027e-3c5b-4e87-a10a-572676b92e22";
    val medicationRequestResourceId = "75ec9d5d-07ec-44cf-b841-d8a4ef20e521";
    val patientResourceId = "c9e9eeb8-e397-4d62-a977-656a18027f90";
    val practitionerResourceId = "d8ac97db-249d-4f14-8c9b-861f8b93ca76";
    val organizationResourceId = "d55c6c01-057b-483d-a1eb-2bd1e182551f";
    val insuranceResourceId = "914e46d1-95a2-44c7-b900-5ca4ee80b8d5";

    ErpWorkflowNamingSystem prescriptionIdSystem;
    if (kbvErpVersion.compareTo(KbvItaErpVersion.V1_0_2) == 0) {
      prescriptionIdSystem = ErpWorkflowNamingSystem.PRESCRIPTION_ID;
    } else {
      prescriptionIdSystem = ErpWorkflowNamingSystem.PRESCRIPTION_ID_121;
    }
    // bundle values
    val prescriptionId = new PrescriptionId(prescriptionIdSystem, "160.100.000.000.011.09");

    val practitioner =
        PractitionerBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(practitionerResourceId)
            .lanr("159753527")
            .name("Mia", "Meyer", "Dr.")
            .addQualification(QualificationType.DOCTOR)
            .addQualification("Super-Facharzt für alles Mögliche")
            .build();

    val organization =
        MedicalOrganizationBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(organizationResourceId)
            .name("Arztpraxis Meyer")
            .bsnr("757299999")
            .phone("+490309876543")
            .email("info@praxis.de")
            .address("Berlin", "10623", "Wegelystraße 3")
            .build();

    val patient =
        PatientBuilder.builder()
            .version(kbvForVersion)
            .setResourceId(patientResourceId)
            .kvnr(KVNR.random(), VersicherungsArtDeBasis.GKV)
            .name("Erwin", "Fleischer")
            .birthDate("09.07.1973")
            .address(Country.D, "Berlin", "10117", "Friedrichstraße 136")
            .build();

    val insurance =
        KbvCoverageBuilder.insurance("101377508", "Techniker-Krankenkasse")
            .version(kbvForVersion)
            .beneficiary(patient)
            .setResourceId(insuranceResourceId)
            .personGroup(PersonGroup.NOT_SET) // default NOT_SET
            .dmpKennzeichen(DmpKennzeichen.ASTHMA) // default NOT_SET
            .wop(Wop.BERLIN) // default DUMMY
            .versichertenStatus(VersichertenStatus.PENSIONER) // default MEMBERS
            .build();
    val medication =
        KbvErpMedicationCompoundingFaker.builder()
            .withMedicationIngredient(PZN.random(), "nameOfMedicine", "freetext")
            .withResourceId(medicationResourceId)
            .withVersion(kbvErpVersion)
            .withCategory(MedicationCategory.C_00)
            .fake();

    val medicationRequest =
        MedicationRequestBuilder.forPatient(patient)
            .version(kbvErpVersion)
            .setResourceId(medicationRequestResourceId)
            .insurance(insurance)
            .requester(practitioner)
            .medication(medication)
            .dosage("im schleim baden!")
            .note("immer nur nach dem Essen")
            .quantityPackages(20)
            .status("active") // default ACTIVE
            .intent("order") // default ORDER
            .isBVG(false) // Bundesversorgungsgesetz default true
            .hasEmergencyServiceFee(true) // default false
            .substitution(false) // default true
            .coPaymentStatus(StatusCoPayment.STATUS_0) // default StatusCoPayment.STATUS_0
            .build();

    val kbvBundle =
        KbvErpBundleBuilder.forPrescription(prescriptionId)
            .version(kbvErpVersion)
            .practitioner(practitioner)
            .medicalOrganization(organization)
            .patient(patient)
            .insurance(insurance)
            .statusKennzeichen("00") // 00/NONE is default
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication)
            .build();

    // check if all values has been set correctly
    assertNotNull(kbvBundle.getId());
    assertEquals(prescriptionId, kbvBundle.getPrescriptionId());

    val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
    assertTrue(result.isSuccessful());
    assertEquals(VersicherungsArtDeBasis.GKV, kbvBundle.getPatient().getInsuranceKind());
    assertFalse(kbvBundle.getPatient().getPkvAssigner().isPresent());
    assertFalse(kbvBundle.getPatient().getPkvAssignerName().isPresent());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build incomplete KBV Bundle with MedicationCompounding and versions"
              + " KbvItaForVersion {0} and KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void fakerShouldWorkWithMedicationCompounding(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {
    val medication = KbvErpMedicationCompoundingFaker.builder().withVersion(kbvErpVersion).fake();
    val assigner = AssignerOrganizationFaker.builder().withVersion(kbvForVersion).fake();
    val patient =
        PatientFaker.builder().withVersion(kbvForVersion).withAssignerRef(assigner).fake();
    val insurance =
        KbvCoverageFaker.builder()
            .withInsuranceType(patient.getInsuranceKind())
            .withVersion(kbvForVersion)
            .fake();
    val practitioner = PractitionerFaker.builder().withVersion(kbvForVersion).fake();
    val medicationRequest =
        MedicationRequestFaker.builder()
            .withPatient(patient)
            .withMedication(medication)
            .withRequester(practitioner)
            .withInsurance(insurance)
            .withVersion(kbvErpVersion)
            .fake();
    val organisation = MedicalOrganizationFaker.medicalPractice().withVersion(kbvForVersion).fake();
    val bundle =
        KbvErpBundleBuilder.forPrescription(PrescriptionId.random())
            .patient(patient)
            .version(kbvErpVersion)
            .assigner(assigner)
            .practitioner(practitioner)
            .medicalOrganization(organisation)
            .medicationRequest(medicationRequest)
            .insurance(insurance)
            .medication(medication)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build incomplete KBV Bundle with MedicationFreeText and versions"
              + " KbvItaForVersion {0} and KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void fakerShouldWorkWithMedicationFreeText(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {
    val medication = KbvErpMedicationFreeTextFaker.builder().withVersion(kbvErpVersion).fake();
    val assigner = AssignerOrganizationFaker.builder().withVersion(kbvForVersion).fake();
    val patient =
        PatientFaker.builder().withVersion(kbvForVersion).withAssignerRef(assigner).fake();
    val insurance =
        KbvCoverageFaker.builder()
            .withInsuranceType(patient.getInsuranceKind())
            .withVersion(kbvForVersion)
            .fake();
    val practitioner = PractitionerFaker.builder().withVersion(kbvForVersion).fake();
    val medicationRequest =
        MedicationRequestFaker.builder()
            .withPatient(patient)
            .withMedication(medication)
            .withRequester(practitioner)
            .withInsurance(insurance)
            .withVersion(kbvErpVersion)
            .fake();
    val organisation = MedicalOrganizationFaker.medicalPractice().withVersion(kbvForVersion).fake();
    val bundle =
        KbvErpBundleBuilder.forPrescription(PrescriptionId.random())
            .patient(patient)
            .version(kbvErpVersion)
            .assigner(assigner)
            .practitioner(practitioner)
            .medicalOrganization(organisation)
            .medicationRequest(medicationRequest)
            .insurance(insurance)
            .medication(medication)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
  }
}
