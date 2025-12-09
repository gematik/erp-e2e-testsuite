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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerDrugName;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.bbriccs.fhir.de.valueset.Country;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider;
import de.gematik.test.erezept.fhir.values.AsvFachgruppennummer;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.values.KZVA;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.val;
import org.junit.Ignore;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.ClearSystemProperty.ClearSystemProperties;

class KbvErpBundleBuilderTest extends ErpFhirParsingTest {

  static Stream<Arguments> buildFakerWithGivenMedicationCompounding() {
    return VersionArgumentProvider.kbvBundleVersions()
        .flatMap(
            args -> {
              val kbvForVersion = (KbvItaForVersion) args.get()[0];
              val kbvErpVersion = (KbvItaErpVersion) args.get()[1];
              return Stream.of(
                  Arguments.of(kbvForVersion, kbvErpVersion, InsuranceTypeDe.GKV),
                  Arguments.of(kbvForVersion, kbvErpVersion, InsuranceTypeDe.PKV));
            });
  }

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

    val prescriptionId = PrescriptionId.from("160.100.000.000.011.09");

    val prescribingPractitioner =
        KbvPractitionerBuilder.builder()
            .version(kbvForVersion)
            .setId(practitionerResourceId)
            .lanr("159753527")
            .name("Mia", "Meyer", "Dr.")
            .addQualification(QualificationType.DOCTOR_IN_TRAINING)
            .addQualification("Super-Facharzt für alles Mögliche")
            .build();

    val responsiblePractitioner =
        KbvPractitionerBuilder.builder()
            .version(kbvForVersion)
            .setId(UUID.randomUUID().toString())
            .lanr(BaseANR.randomFromQualification(QualificationType.DOCTOR).getValue())
            .name("Emmett", "Brown", "Dr.")
            .addQualification(QualificationType.DOCTOR)
            .addQualification("Super-Facharzt für alles Mögliche")
            .build();

    val organization =
        KbvMedicalOrganizationBuilder.builder()
            .version(kbvForVersion)
            .setId(organizationResourceId)
            .name("Arztpraxis Meyer")
            .bsnr("757299999")
            .phone("+490309876543")
            .email("info@praxis.de")
            .address("Berlin", "10623", "Wegelystraße 3")
            .build();

    val patient =
        KbvPatientBuilder.builder()
            .version(kbvForVersion)
            .setId(patientResourceId)
            .kvnr(KVNR.randomGkv())
            .name("Erwin", "Fleischer")
            .birthDate("09.07.1973")
            .address(Country.D, "Berlin", "10117", "Friedrichstraße 136")
            .build();

    val insurance =
        KbvCoverageBuilder.insurance("101377508", "Techniker-Krankenkasse")
            .version(kbvForVersion)
            .beneficiary(patient)
            .setId(insuranceResourceId)
            .personGroup(PersonGroup.NOT_SET) // default NOT_SET
            .dmpKennzeichen(DmpKennzeichen.ASTHMA) // default NOT_SET
            .wop(Wop.BERLIN) // default DUMMY
            .versichertenStatus(VersichertenStatus.PENSIONER) // default MEMBERS
            .build();

    val medication =
        KbvErpMedicationPZNBuilder.builder()
            .version(kbvErpVersion)
            .setId(medicationResourceId)
            .category(MedicationCategory.C_00) // default C_00
            .isVaccine(false) // default false
            .normgroesse(StandardSize.N1) // default NB (nicht betroffen)
            .darreichungsform(Darreichungsform.TKA) // default TAB
            .amount(5, "Stk") // default 10 {tbl}
            .pzn("04773414", "Doxycyclin AL 200 T, 10 Tabletten N1")
            .build();

    val medicationRequest =
        KbvErpMedicationRequestBuilder.forPatient(patient)
            .version(kbvErpVersion)
            .setId(medicationRequestResourceId)
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
            .statusKennzeichen("00", prescribingPractitioner) // 00/NONE is default
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication)
            .build();

    // check if all values has been set correctly
    assertNotNull(kbvBundle.getId());
    assertEquals(prescriptionId, kbvBundle.getPrescriptionId());

    assertTrue(parser.isValid(kbvBundle));
    assertEquals(InsuranceTypeDe.GKV, kbvBundle.getPatient().getInsuranceType());
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
    val prescriptionId = PrescriptionId.from("160.100.000.000.011.09");

    val practitioner =
        KbvPractitionerBuilder.builder()
            .version(kbvForVersion)
            .setId(practitionerResourceId)
            .zanr("159753527")
            .name("Mia", "Meyer", "Dr.")
            .addQualification(QualificationType.DENTIST)
            // .addQualification(AsvFachgruppennummer.from("123"))
            .addQualification("Super-Facharzt für alles Mögliche")
            .build();

    val medicalOrganization =
        KbvMedicalOrganizationBuilder.builder()
            .version(kbvForVersion)
            .setId(organizationResourceId)
            .name("Arztpraxis Meyer")
            .kzva(KZVA.from("123456789")) // Denitists need to use a KZVA in Organization
            .phone("+490309876543")
            .email("info@praxis.de")
            .address(Country.D, "Berlin", "10623", "Wegelystraße 3")
            .build();

    val patient =
        KbvPatientBuilder.builder()
            .version(kbvForVersion)
            .setId(patientResourceId)
            .kvnr(KVNR.randomPkv(), InsuranceTypeDe.PKV)
            .name("Erwin", "Fleischer")
            .birthDate("09.07.1973")
            .address(Country.D, "Berlin", "10117", "Friedrichstraße 136")
            .build();

    val insurance =
        KbvCoverageBuilder.insurance("101377508", "Bayerische Beamtenkrankenkasse")
            .version(kbvForVersion)
            .beneficiary(patient)
            .setId(insuranceResourceId)
            .personGroup(PersonGroup.NOT_SET) // default NOT_SET
            .dmpKennzeichen(DmpKennzeichen.ASTHMA) // default NOT_SET
            .wop(Wop.BERLIN) // default DUMMY
            .versichertenStatus(VersichertenStatus.PENSIONER) // default MEMBERS
            .build();

    val medication =
        KbvErpMedicationPZNBuilder.builder()
            .version(kbvErpVersion)
            .setId(medicationResourceId)
            .category(MedicationCategory.C_00) // default C_00
            .isVaccine(false) // default false
            .normgroesse(StandardSize.N1) // default NB (nicht betroffen)
            .darreichungsform(Darreichungsform.TKA) // default TAB
            .amount(5, "Stk") // default 10 {tbl}
            .pzn("04773414", "Doxycyclin AL 200 T, 10 Tabletten N1")
            .build();

    val medicationRequest =
        KbvErpMedicationRequestBuilder.forPatient(patient)
            .version(kbvErpVersion)
            .setId(medicationRequestResourceId)
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
            .medicationRequest(medicationRequest)
            .medication(medication)
            .build();

    // check if all values has been set correctly
    assertNotNull(kbvBundle.getId());
    assertEquals(prescriptionId, kbvBundle.getPrescriptionId());

    assertTrue(parser.isValid(kbvBundle));
    if (kbvErpVersion.compareTo(KbvItaErpVersion.V1_1_0) <= 0)
      assertEquals(InsuranceTypeDe.PKV, kbvBundle.getPatient().getInsuranceType());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Create a prescription bundle with a Practitioner without ANR (with versions"
              + " KbvItaForVersion {0} and KbvItaErpVersion {1})")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void shouldBuildKbvBundleWithFixedValuesWithoutAnrInPractitioner(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {
    val prescriptionId = PrescriptionId.from("160.100.000.000.011.09");
    val practitioner =
        KbvPractitionerBuilder.builder()
            .version(kbvForVersion)
            .name("Mia", "Meyer", "Dr.")
            .addQualification(QualificationType.DOCTOR_AS_REPLACEMENT)
            .addQualification(AsvFachgruppennummer.from("01"))
            .addQualification("Super-Facharzt für alles Mögliche")
            .build();

    val medicalOrganization =
        KbvMedicalOrganizationBuilder.builder()
            .version(kbvForVersion)
            .name("Arztpraxis Meyer")
            .bsnr("757299999")
            .phone("+490309876543")
            .email("info@praxis.de")
            .address(Country.D, "Berlin", "10623", "Wegelystraße 3")
            .build();

    val patient =
        KbvPatientBuilder.builder()
            .version(kbvForVersion)
            .kvnr(KVNR.randomPkv(), InsuranceTypeDe.PKV)
            .name("Erwin", "Fleischer")
            .birthDate("09.07.1973")
            .address(Country.D, "Berlin", "10117", "Friedrichstraße 136")
            .build();

    val insurance =
        KbvCoverageBuilder.insurance("101377508", "Bayerische Beamtenkrankenkasse")
            .version(kbvForVersion)
            .beneficiary(patient)
            .personGroup(PersonGroup.NOT_SET) // default NOT_SET
            .dmpKennzeichen(DmpKennzeichen.ASTHMA) // default NOT_SET
            .wop(Wop.BERLIN) // default DUMMY
            .versichertenStatus(VersichertenStatus.PENSIONER) // default MEMBERS
            .build();

    val medication =
        KbvErpMedicationPZNBuilder.builder()
            .version(kbvErpVersion)
            .category(MedicationCategory.C_00) // default C_00
            .isVaccine(false) // default false
            .normgroesse(StandardSize.N1) // default NB (nicht betroffen)
            .darreichungsform(Darreichungsform.TKA) // default TAB
            .amount(5, "Stk") // default 10 {tbl}
            .pzn("04773414", "Doxycyclin AL 200 T, 10 Tabletten N1")
            .build();

    val medicationRequest =
        KbvErpMedicationRequestBuilder.forPatient(patient)
            .version(kbvErpVersion)
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
            .statusKennzeichen(StatusKennzeichen.ASV, practitioner, kbvForVersion)
            .practitioner(practitioner)
            .medicalOrganization(medicalOrganization)
            .patient(patient)
            .insurance(insurance)
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication)
            .build();

    // check if all values has been set correctly
    assertNotNull(kbvBundle.getId());
    assertEquals(prescriptionId, kbvBundle.getPrescriptionId());

    assertTrue(parser.isValid(kbvBundle));
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
    val prescriptionId = PrescriptionId.from("160.100.000.000.011.09");
    val practitioner =
        KbvPractitionerBuilder.builder()
            .version(kbvForVersion)
            .setId(practitionerResourceId)
            .zanr("159753527")
            .name("Mia", "Meyer", "Dr.")
            .addQualification(QualificationType.DENTIST)
            .addQualification("Super-Facharzt für alles Mögliche")
            .build();

    val medicalOrganization =
        KbvMedicalOrganizationBuilder.builder()
            .version(kbvForVersion)
            .setId(organizationResourceId)
            .name("Arztpraxis Meyer")
            .kzva(KZVA.random()) // up to KbvItaForVersion 1.2 kzv is mandatory for Dentists!
            .phone("+490309876543")
            .email("info@praxis.de")
            .address(Country.D, "Berlin", "10623", "Wegelystraße 3")
            .build();

    val patient =
        KbvPatientBuilder.builder()
            .version(kbvForVersion)
            .setId(patientResourceId)
            .kvnr(KVNR.randomPkv(), InsuranceTypeDe.PKV)
            .name("Erwin", "Fleischer")
            .birthDate("09.07.1973")
            .address(Country.D, "Berlin", "10117", "Friedrichstraße 136")
            .build();

    val insurance =
        KbvCoverageBuilder.insurance("101377508", "Bayerische Beamtenkrankenkasse")
            .version(kbvForVersion)
            .beneficiary(patient)
            .insuranceType(InsuranceTypeDe.BG)
            .setId(insuranceResourceId)
            .personGroup(PersonGroup.NOT_SET) // default NOT_SET
            .dmpKennzeichen(DmpKennzeichen.ASTHMA) // default NOT_SET
            .wop(Wop.BERLIN) // default DUMMY
            .versichertenStatus(VersichertenStatus.PENSIONER) // default MEMBERS
            .build();

    val medication =
        KbvErpMedicationPZNBuilder.builder()
            .version(kbvErpVersion)
            .setId(medicationResourceId)
            .category(MedicationCategory.C_00) // default C_00
            .isVaccine(false) // default false
            .normgroesse(StandardSize.N1) // default NB (nicht betroffen)
            .darreichungsform(Darreichungsform.TKA) // default TAB
            .amount(5, "Stk") // default 10 {tbl}
            .pzn("04773414", "Doxycyclin AL 200 T, 10 Tabletten N1")
            .build();

    val medicationRequest =
        KbvErpMedicationRequestBuilder.forPatient(patient)
            .version(kbvErpVersion)
            .setId(medicationRequestResourceId)
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
            .build();

    // check if all values has been set correctly
    assertNotNull(kbvBundle.getId());
    assertEquals(prescriptionId, kbvBundle.getPrescriptionId());

    assertTrue(parser.isValid(kbvBundle));
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV Bundle for PKV with Accident and with versions KbvItaForVersion"
              + " {0} and KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void buildKbvBundleWithFixedValuesForPKVWithAccident(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {

    val prescriptionId = PrescriptionId.from("160.100.000.000.011.09");

    val practitioner =
        KbvPractitionerBuilder.builder()
            .version(kbvForVersion)
            .zanr("159753527")
            .name("Mia", "Meyer", "Dr.")
            .addQualification(QualificationType.DENTIST)
            .addQualification("Super-Facharzt für alles Mögliche")
            .build();

    val medicalOrganization =
        KbvMedicalOrganizationBuilder.builder()
            .version(kbvForVersion)
            .name("Arztpraxis Meyer")
            .kzva(KZVA.random()) // up to KbvItaForVersion 1.2 kzv is mandatory for Dentists!
            .phone("+490309876543")
            .email("info@praxis.de")
            .address(Country.D, "Berlin", "10623", "Wegelystraße 3")
            .build();

    val patient =
        KbvPatientBuilder.builder()
            .version(kbvForVersion)
            .kvnr(KVNR.randomPkv(), InsuranceTypeDe.PKV)
            .name("Erwin", "Fleischer")
            .birthDate("09.07.1973")
            .address(Country.D, "Berlin", "10117", "Friedrichstraße 136")
            .build();

    val insurance =
        KbvCoverageBuilder.insurance("101377508", "Bayerische Beamtenkrankenkasse")
            .version(kbvForVersion)
            .beneficiary(patient)
            .insuranceType(InsuranceTypeDe.PKV)
            .personGroup(PersonGroup.NOT_SET) // default NOT_SET
            .dmpKennzeichen(DmpKennzeichen.ASTHMA) // default NOT_SET
            .wop(Wop.BERLIN) // default DUMMY
            .versichertenStatus(VersichertenStatus.PENSIONER) // default MEMBERS
            .build();

    val medication =
        KbvErpMedicationPZNBuilder.builder()
            .version(kbvErpVersion)
            .category(MedicationCategory.C_00) // default C_00
            .isVaccine(false) // default false
            .normgroesse(StandardSize.N1) // default NB (nicht betroffen)
            .darreichungsform(Darreichungsform.TKA) // default TAB
            .amount(5, "Stk") // default 10 {tbl}
            .pzn("04773414", "Doxycyclin AL 200 T, 10 Tabletten N1")
            .build();

    val medicationRequest =
        KbvErpMedicationRequestBuilder.forPatient(patient)
            .version(kbvErpVersion)
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
            .build();

    assertTrue(parser.isValid(kbvBundle));

    assertEquals(prescriptionId, kbvBundle.getPrescriptionId());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV Bundle for PKV with Accident at Work and invalid Coverage Type"
              + " with versions KbvItaForVersion {0} and KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void buildKbvBundleForPKVWithAccidentAndInvalidCoverage(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {

    val practitioner = KbvPractitionerFaker.builder().withVersion(kbvForVersion).fake();
    val medicalOrganization =
        KbvMedicalOrganizationFaker.forPractitioner(practitioner).withVersion(kbvForVersion).fake();

    val patient =
        KbvPatientFaker.builder()
            .withKvnrAndInsuranceType(KVNR.randomPkv(), InsuranceTypeDe.PKV)
            .withVersion(kbvForVersion)
            .fake();
    val insurance =
        KbvCoverageFaker.builder()
            .withInsuranceType(InsuranceTypeDe.PKV)
            .withVersion(kbvForVersion)
            .withBeneficiary(patient)
            .fake();
    val medication = KbvErpMedicationPZNFaker.builder().withVersion(kbvErpVersion).fake();
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withPatient(patient)
            .withVersion(kbvErpVersion)
            .withInsurance(insurance)
            .withRequester(practitioner)
            .withMedication(medication)
            .withAccident(AccidentExtension.accidentAtWork().atWorkplace())
            .fake();

    val kbvBundle =
        KbvErpBundleBuilder.forPrescription(PrescriptionId.random())
            .version(kbvErpVersion)
            .practitioner(practitioner)
            .medicalOrganization(medicalOrganization)
            .patient(patient)
            .insurance(insurance)
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication)
            .build();

    // accident at work and PKV insurance is not allowed for new profiles
    // TODO: better: build method throws directly an exception in this case!
    assertFalse(parser.isValid(kbvBundle));
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build faked KBV Bundle with versions KbvItaForVersion {0} and"
              + " KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void buildKbvBundleWithFaker(KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {

    for (int i = 0; i < 1; i++) {
      val practitioner = KbvPractitionerFaker.builder().withVersion(kbvForVersion).fake();
      val medicalOrganization =
          KbvMedicalOrganizationFaker.forPractitioner(practitioner)
              .withVersion(kbvForVersion)
              .fake();

      val patient =
          KbvPatientFaker.builder()
              .withVersion(kbvForVersion)
              .withKvnrAndInsuranceType(KVNR.random(), InsuranceTypeDe.GKV)
              .fake();
      val insurance =
          KbvCoverageFaker.builder().withBeneficiary(patient).withVersion(kbvForVersion).fake();
      val medication = KbvErpMedicationPZNFaker.builder().withVersion(kbvErpVersion).fake();

      val kbvBundle =
          KbvErpBundleFaker.builder()
              .withVersion(kbvErpVersion, kbvForVersion)
              .withPractitioner(practitioner)
              .withCustodian(medicalOrganization)
              .withPatient(patient)
              .withInsurance(insurance, patient)
              .withMedication(medication)
              .fake();
      assertTrue(parser.isValid(kbvBundle));
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
    System.setProperty(kbvErpVersion.getName(), kbvErpVersion.getVersion());
    System.setProperty(kbvForVersion.getName(), kbvForVersion.getVersion());

    for (int i = 0; i < 5; i++) {
      val kbvBundle =
          KbvErpBundleFaker.builder()
              .withKvnr(KVNR.random())
              .withPznAndMedicationName(PZN.from("04773414"), fakerDrugName())
              .fake();

      assertTrue(parser.isValid(kbvBundle));
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
  void buildKbvBundleWithSuperFakerAndDentist(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {
    System.setProperty(kbvErpVersion.getName(), kbvErpVersion.getVersion());
    System.setProperty(kbvForVersion.getName(), kbvForVersion.getVersion());

    for (int i = 0; i < 1; i++) {
      val kbvBundle =
          KbvErpBundleFaker.builder()
              .withKvnr(KVNR.random())
              .withPractitioner(KbvPractitionerFaker.builder().fake())
              .fake();

      assertTrue(parser.isValid(kbvBundle));
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

    System.setProperty(kbvErpVersion.getName(), kbvErpVersion.getVersion());
    System.setProperty(kbvForVersion.getName(), kbvForVersion.getVersion());

    for (int i = 0; i < 1; i++) {
      val kbvBundle = KbvErpBundleFaker.builder().withKvnr(KVNR.random()).fake();
      assertTrue(parser.isValid(kbvBundle));
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
    System.setProperty(kbvErpVersion.getName(), kbvErpVersion.getVersion());
    System.setProperty(kbvForVersion.getName(), kbvForVersion.getVersion());

    for (int i = 0; i < 5; i++) {
      val kbvBundle =
          KbvErpBundleFaker.builder()
              .withKvnr(KVNR.random())
              .withPrescriptionId(PrescriptionId.from("160.002.362.150.600.45"))
              .fake();

      assertTrue(parser.isValid(kbvBundle));
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
  void shouldBuildWithCustomAuthoredOnDate(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {
    System.setProperty(kbvErpVersion.getName(), kbvErpVersion.getVersion());
    System.setProperty(kbvForVersion.getName(), kbvForVersion.getVersion());

    val authoredOn = Date.from(Instant.now().minus(2, ChronoUnit.DAYS));
    val kbvBundle = KbvErpBundleFaker.builder().withAuthorDate(authoredOn).fake();
    assertTrue(parser.isValid(kbvBundle));

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
            .withInsuranceType(InsuranceTypeDe.GKV)
            .withVersion(kbvForVersion)
            .fake();
    val patient =
        KbvPatientFaker.builder()
            .withKvnrAndInsuranceType(KVNR.randomGkv(), InsuranceTypeDe.GKV)
            .withVersion(kbvForVersion)
            .fake();
    val practitioner = KbvPractitionerFaker.builder().withVersion(kbvForVersion).fake();
    val custodian =
        KbvMedicalOrganizationFaker.forPractitioner(practitioner).withVersion(kbvForVersion).fake();

    val mvo = MultiplePrescriptionExtension.asNonMultiple();
    val kbvBundleBuilder =
        KbvErpBundleBuilder.builder()
            .version(kbvErpVersion)
            .medication(medication)
            .medicationRequest(
                KbvErpMedicationRequestFaker.builder()
                    .withPatient(patient)
                    .withVersion(kbvErpVersion)
                    .withMedication(medication)
                    .withMvo(mvo)
                    .withInsurance(coverage)
                    .withRequester(practitioner)
                    .fake());

    val kbvBundle =
        kbvBundleBuilder
            .insurance(coverage)
            .patient(patient)
            .practitioner(practitioner)
            .medicalOrganization(custodian)
            .prescriptionId(PrescriptionId.random())
            .build();

    assertTrue(parser.isValid(kbvBundle));
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
              KbvPractitionerFaker.builder()
                  .withVersion(kbvForVersion)
                  .withAnr(BaseANR.randomFromQualification(qt))
                  .fake();
          val kbvBundle =
              KbvErpBundleFaker.builder()
                  .withKvnr(KVNR.random())
                  .withVersion(kbvErpVersion, kbvForVersion)
                  .withPractitioner(practitioner)
                  .fake();
          val actual = kbvBundle.getPractitioner();
          assertEquals(practitioner.getANR(), actual.getANR());
        });
  }

  /* @ParameterizedTest(
      name =
          "[{index}] -> Create a prescription bundle with a SupplyRequest (with versions"
              + " KbvItaForVersion {0} and KbvItaErpVersion {1})")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")*/
  @Ignore(
      "just reactivate after solving the spookey issue:[ERROR in Line 1 at Bundle]: Rule"
          + " -erp-angabeUnfallkennzeichenArbeitsunfallBerufskrankheitVerbot: 'In der Ressource vom"
          + " Typ MedicationRequest ist das Unfallkennzeichen für einen Arbeitsunfall oder"
          + " Berufskrankheit angegeben, dies darf aber nur bei einem Kostentraeger vom Typ \"BG\""
          + " oder \"UK\" erfolgen.' Failed ")
  void shouldBuildBundleWithSupplyRequestCorrectly(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {
    val prescriptionId = PrescriptionId.from("160.100.000.000.011.09");
    val practitioner = KbvPractitionerFaker.builder().withVersion(kbvForVersion).fake();

    val organization =
        KbvMedicalOrganizationFaker.forPractitioner(practitioner).withVersion(kbvForVersion).fake();

    val patient =
        KbvPatientFaker.builder()
            .withKvnrAndInsuranceType(KVNR.random(), InsuranceTypeDe.GKV)
            .withVersion(kbvForVersion)
            .fake();

    val insurance =
        KbvCoverageFaker.builder()
            .withVersion(kbvForVersion)
            .withInsuranceType(InsuranceTypeDe.GKV)
            .withBeneficiary(patient)
            .fake();

    val medication = KbvErpMedicationPZNFaker.builder().withVersion(kbvErpVersion).fake();

    val supplyRequest =
        SupplyRequestBuilder.withCoverage(insurance)
            .version(kbvErpVersion)
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
            .statusKennzeichen("00", practitioner) // 00/NONE is default
            .supplyRequest(supplyRequest)
            .medication(medication)
            .build();

    assertTrue(parser.isValid(kbvBundle));
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Should not allow to build a prescription bundle with both a SupplyRequest"
              + " AND MedicationRequest (with versions KbvItaForVersion {0} and KbvItaErpVersion"
              + " {1})")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void shouldNotAllowPrescriptionWithSupplyRequestAndMedicationRequest(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {
    val supplyRequestResourceId = UUID.randomUUID().toString();

    // bundle values
    val prescriptionId = PrescriptionId.random();
    val practitioner = KbvPractitionerFaker.builder().withVersion(kbvForVersion).fake();

    val organization =
        KbvMedicalOrganizationFaker.forPractitioner(practitioner).withVersion(kbvForVersion).fake();

    val patient = KbvPatientFaker.builder().withVersion(kbvForVersion).fake();

    val insurance =
        KbvCoverageFaker.builder().withVersion(kbvForVersion).withBeneficiary(patient).fake();

    val medication = KbvErpMedicationPZNFaker.builder().withVersion(kbvErpVersion).fake();

    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withVersion(kbvErpVersion)
            .withInsurance(insurance)
            .withRequester(practitioner)
            .withMedication(medication)
            .fake();

    val supplyRequest =
        SupplyRequestBuilder.withCoverage(insurance)
            .setId(supplyRequestResourceId)
            .requester(practitioner)
            .medication(medication)
            .build();

    val kbvBundleBuilder =
        KbvErpBundleBuilder.forPrescription(prescriptionId)
            .version(kbvErpVersion)
            .practitioner(practitioner)
            .medicalOrganization(organization)
            .patient(patient)
            .insurance(insurance)
            .statusKennzeichen("00", practitioner)
            .medicationRequest(medicationRequest)
            .supplyRequest(supplyRequest)
            .medication(medication);

    // this check is currently deactivated because we intentionally use this behavior in
    // produkt-testsuite
    //    assertThrows(BuilderException.class, kbvBundleBuilder::build);
    assertDoesNotThrow(kbvBundleBuilder::build);
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build incomplete KBV Bundle with MedicationCompounding and versions"
              + " KbvItaForVersion: {0}, KbvItaErpVersion: {1} and InsuranceType: {2}")
  @MethodSource
  void buildFakerWithGivenMedicationCompounding(
      KbvItaForVersion kbvForVersion,
      KbvItaErpVersion kbvErpVersion,
      InsuranceTypeDe insuranceType) {

    // bundle values
    val prescriptionId = PrescriptionId.random();
    val practitioner = KbvPractitionerFaker.builder().withVersion(kbvForVersion).fake();
    val organization =
        KbvMedicalOrganizationFaker.forPractitioner(practitioner).withVersion(kbvForVersion).fake();
    val patient =
        KbvPatientFaker.builder()
            .withVersion(kbvForVersion)
            .withInsuranceType(insuranceType)
            .fake();
    val insurance =
        KbvCoverageFaker.builder()
            .withVersion(kbvForVersion)
            .withInsuranceType(insuranceType)
            .fake();

    val medication =
        KbvErpMedicationCompoundingFaker.builder()
            .withMedicationIngredient(PZN.random(), "nameOfMedicine", "freetext")
            .withVersion(kbvErpVersion)
            .withCategory(MedicationCategory.C_00)
            .fake();

    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withVersion(kbvErpVersion)
            .withPatient(patient)
            .withInsurance(insurance)
            .withRequester(practitioner)
            .withMedication(medication)
            .fake();

    val kbvBundle =
        KbvErpBundleBuilder.forPrescription(prescriptionId)
            .version(kbvErpVersion)
            .practitioner(practitioner)
            .medicalOrganization(organization)
            .patient(patient)
            .insurance(insurance)
            .statusKennzeichen("00", practitioner) // 00/NONE is default
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV Bundle with MedicationCompounding and versions"
              + " KbvItaForVersion {0} and KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void shouldBuildMedicationCompoundingWithFaker(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {
    val medication = KbvErpMedicationCompoundingFaker.builder().withVersion(kbvErpVersion).fake();

    val patient = KbvPatientFaker.builder().withVersion(kbvForVersion).fake();
    val insurance =
        KbvCoverageFaker.builder()
            .withInsuranceType(patient.getInsuranceType())
            .withVersion(kbvForVersion)
            .fake();
    val practitioner = KbvPractitionerFaker.builder().withVersion(kbvForVersion).fake();
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withPatient(patient)
            .withMedication(medication)
            .withRequester(practitioner)
            .withInsurance(insurance)
            .withVersion(kbvErpVersion)
            .fake();
    val organisation =
        KbvMedicalOrganizationFaker.forPractitioner(practitioner).withVersion(kbvForVersion).fake();
    val bundle =
        KbvErpBundleBuilder.forPrescription(PrescriptionId.random())
            .patient(patient)
            .version(kbvErpVersion)
            .practitioner(practitioner)
            .medicalOrganization(organisation)
            .medicationRequest(medicationRequest)
            .insurance(insurance)
            .medication(medication)
            .build();

    assertTrue(parser.isValid(bundle));
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build incomplete KBV Bundle with MedicationFreeText and versions"
              + " KbvItaForVersion {0} and KbvItaErpVersion {1}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void fakerShouldWorkWithMedicationFreeText(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {
    val medication = KbvErpMedicationFreeTextFaker.builder().withVersion(kbvErpVersion).fake();
    val patient = KbvPatientFaker.builder().withVersion(kbvForVersion).fake();
    val insurance =
        KbvCoverageFaker.builder()
            .withInsuranceType(patient.getInsuranceType())
            .withVersion(kbvForVersion)
            .fake();
    val practitioner = KbvPractitionerFaker.builder().withVersion(kbvForVersion).fake();
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withPatient(patient)
            .withMedication(medication)
            .withRequester(practitioner)
            .withInsurance(insurance)
            .withVersion(kbvErpVersion)
            .fake();
    val organisation =
        KbvMedicalOrganizationFaker.forPractitioner(practitioner).withVersion(kbvForVersion).fake();
    val bundle =
        KbvErpBundleBuilder.forPrescription(PrescriptionId.random())
            .patient(patient)
            .version(kbvErpVersion)
            .practitioner(practitioner)
            .medicalOrganization(organisation)
            .medicationRequest(medicationRequest)
            .insurance(insurance)
            .medication(medication)
            .build();

    assertTrue(parser.isValid(bundle));
  }
}
