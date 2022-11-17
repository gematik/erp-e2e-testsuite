/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.builder.kbv;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.parser.profiles.CustomProfiles;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.Country;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.DmpKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.IdentifierTypeDe;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.PersonGroup;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import de.gematik.test.erezept.fhir.valuesets.VersichertenStatus;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.fhir.valuesets.Wop;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class KbvBuilderTest extends ParsingTest {

  private static Stream<Arguments> kbvBundleVersions() {
    return Stream.of(
        Arguments.of(KbvItaForVersion.V1_0_3, KbvItaErpVersion.V1_0_2),
        Arguments.of(KbvItaForVersion.V1_1_0, KbvItaErpVersion.V1_1_0));
  }

  @AfterEach
  void cleanVersionProperties() {
    System.clearProperty(CustomProfiles.KBV_ITA_ERP.getName());
    System.clearProperty(CustomProfiles.KBV_ITA_FOR.getName());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV Bundle for GKV with versions KbvItaForVersion {0} and KbvItaErpVersion {1}")
  @MethodSource("kbvBundleVersions")
  void buildKbvBundleWithFixedValuesForGKV(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {
    val medicationResourceId = "c1e7027e-3c5b-4e87-a10a-572676b92e22";
    val medicationRequestResourceId = "75ec9d5d-07ec-44cf-b841-d8a4ef20e521";
    val patientResourceId = "c9e9eeb8-e397-4d62-a977-656a18027f90";
    val practitionerResourceId = "d8ac97db-249d-4f14-8c9b-861f8b93ca76";
    val organizationResourceId = "d55c6c01-057b-483d-a1eb-2bd1e182551f";
    val insuranceResourceId = "914e46d1-95a2-44c7-b900-5ca4ee80b8d5";

    // bundle values
    val prescriptionId = new PrescriptionId("160.100.000.000.011.09");

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
            .kvIdentifierDe("X123456789", IdentifierTypeDe.GKV)
            .name("Erwin", "Fleischer")
            .birthDate("09.07.1973")
            .address(Country.D, "Berlin", "10117", "Friedrichstraße 136")
            .build();

    val insurance =
        CoverageBuilder.insurance("101377508", "Techniker-Krankenkasse")
            .version(kbvForVersion)
            .beneficiary(patient)
            .setResourceId(insuranceResourceId)
            .personGroup(PersonGroup.NOT_SET) // default NOT_SET
            .dmpKennzeichen(DmpKennzeichen.ASTHMA) // default NOT_SET
            .wop(Wop.BERLIN) // default DUMMY
            .versichertenStatus(VersichertenStatus.PENSIONER) // default MEMBERS
            .build();

    val medication =
        KbvErpMedicationBuilder.builder()
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

    val kbvBundle =
        KbvErpBundleBuilder.forPrescription(prescriptionId)
            .version(kbvErpVersion)
            .practitioner(practitioner)
            .custodian(organization)
            .patient(patient)
            .insurance(insurance)
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
          "[{index}] -> Build KBV Bundle for PKV with versions KbvItaForVersion {0} and KbvItaErpVersion {1}")
  @MethodSource("kbvBundleVersions")
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
            .kvIdentifierDe("X123456789", IdentifierTypeDe.PKV)
            .assigner(assignerOrganization)
            .name("Erwin", "Fleischer")
            .birthDate("09.07.1973")
            .address(Country.D, "Berlin", "10117", "Friedrichstraße 136")
            .build();

    val insurance =
        CoverageBuilder.insurance("101377508", "Bayerische Beamtenkrankenkasse")
            .version(kbvForVersion)
            .beneficiary(patient)
            .setResourceId(insuranceResourceId)
            .personGroup(PersonGroup.NOT_SET) // default NOT_SET
            .dmpKennzeichen(DmpKennzeichen.ASTHMA) // default NOT_SET
            .wop(Wop.BERLIN) // default DUMMY
            .versichertenStatus(VersichertenStatus.PENSIONER) // default MEMBERS
            .build();

    val medication =
        KbvErpMedicationBuilder.builder()
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

    val kbvBundleBuilder =
        KbvErpBundleBuilder.forPrescription(prescriptionId)
            .version(kbvErpVersion)
            .practitioner(practitioner)
            .custodian(medicalOrganization)
            //            .assigner(assignerOrganization)
            .patient(patient)
            .insurance(insurance)
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication)
            .assigner(assignerOrganization);

    val kbvBundle = kbvBundleBuilder.build();

    // check if all values has been set correctly
    assertNotNull(kbvBundle.getId());
    assertEquals(new PrescriptionId(prescriptionId), kbvBundle.getPrescriptionId());

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

  @ParameterizedTest(
      name =
          "[{index}] -> Build faked KBV Bundle with versions KbvItaForVersion {0} and KbvItaErpVersion {1}")
  @MethodSource("kbvBundleVersions")
  void buildKbvBundleWithFaker(KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {

    for (int i = 0; i < 5; i++) {
      val practitioner = PractitionerBuilder.faker().version(kbvForVersion).build();
      val medicalOrganization = MedicalOrganizationBuilder.faker().version(kbvForVersion).build();
      val assignerOrganization = AssignerOrganizationBuilder.faker().version(kbvForVersion).build();
      val patient =
          PatientBuilder.faker().assigner(assignerOrganization).version(kbvForVersion).build();
      val insurance = CoverageBuilder.faker().beneficiary(patient).version(kbvForVersion).build();
      val medication = KbvErpMedicationBuilder.faker().version(kbvErpVersion).build();
      val medicationRequest =
          MedicationRequestBuilder.faker(patient)
              .version(kbvErpVersion)
              .insurance(insurance)
              .requester(practitioner)
              .medication(medication)
              .build();

      val kbvBundle =
          KbvErpBundleBuilder.faker()
              .version(kbvErpVersion)
              .practitioner(practitioner)
              .custodian(medicalOrganization)
              .assigner(assignerOrganization)
              .patient(patient)
              .insurance(insurance)
              .medicationRequest(medicationRequest) // what is the medication
              .medication(medication)
              .build();

      log.info(format("Validating Faker KBV-Bundle with ID {0}", kbvBundle.getPrescriptionId()));
      val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
      assertTrue(result.isSuccessful());
    }
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build faked KBV Bundle with versions KbvItaForVersion {0} and KbvItaErpVersion {1}")
  @MethodSource("kbvBundleVersions")
  void buildKbvBundleWithSuperFaker(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {
    System.setProperty(kbvErpVersion.getCustomProfile().getName(), kbvErpVersion.getVersion());
    System.setProperty(kbvForVersion.getCustomProfile().getName(), kbvForVersion.getVersion());

    for (int i = 0; i < 5; i++) {
      val kbvBundle = KbvErpBundleBuilder.faker("X123456789", "04773414").build();
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
          "[{index}] -> Build faked KBV Bundle with versions KbvItaForVersion {0} and KbvItaErpVersion {1}")
  @MethodSource("kbvBundleVersions")
  void buildKbvBundleWithSuperFaker02(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {

    System.setProperty(kbvErpVersion.getCustomProfile().getName(), kbvErpVersion.getVersion());
    System.setProperty(kbvForVersion.getCustomProfile().getName(), kbvForVersion.getVersion());

    for (int i = 0; i < 5; i++) {
      val kbvBundle = KbvErpBundleBuilder.faker("X123456789").build();
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
          "[{index}] -> Build faked KBV Bundle with versions KbvItaForVersion {0} and KbvItaErpVersion {1}")
  @MethodSource("kbvBundleVersions")
  void buildKbvBundleWithSuperFaker03(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {
    System.setProperty(kbvErpVersion.getCustomProfile().getName(), kbvErpVersion.getVersion());
    System.setProperty(kbvForVersion.getCustomProfile().getName(), kbvForVersion.getVersion());

    for (int i = 0; i < 5; i++) {
      val kbvBundle =
          KbvErpBundleBuilder.faker("X123456789", new PrescriptionId("160.002.362.150.600.45"))
              .build();

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
          "[{index}] -> Change authoredOn Date in faked KBV Bundle with versions KbvItaForVersion {0} and KbvItaErpVersion {1}")
  @MethodSource("kbvBundleVersions")
  void buildAndChangeAuthoredOnDate(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {

    System.setProperty(kbvErpVersion.getCustomProfile().getName(), kbvErpVersion.getVersion());
    System.setProperty(kbvForVersion.getCustomProfile().getName(), kbvForVersion.getVersion());

    val authoredOn = Date.from(Instant.now().minus(2, ChronoUnit.DAYS));
    val kbvBundle =
        KbvErpBundleBuilder.faker(
                "X123456789", authoredOn, new PrescriptionId("160.002.362.150.600.45"))
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
    assertTrue(result.isSuccessful());

    val expected = authoredOn.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    val actual = kbvBundle.getAuthoredOn().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    assertEquals(expected, actual);
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build incomplete KBV Bundle with versions KbvItaForVersion {0} and KbvItaErpVersion {1}")
  @MethodSource("kbvBundleVersions")
  void shouldThrowOnEmptyKbvBundleBuilder(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {

    val kb = KbvErpBundleBuilder.forPrescription(PrescriptionId.random()).version(kbvErpVersion);
    assertThrows(BuilderException.class, kb::build);
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build incomplete KBV Bundle with versions KbvItaForVersion {0} and KbvItaErpVersion {1}")
  @MethodSource("kbvBundleVersions")
  void buildFakerWithGivenMedicationCategory(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {

    val medication =
        KbvErpMedicationBuilder.faker()
            .version(kbvErpVersion)
            .category(MedicationCategory.C_00)
            .build();

    val coverage =
        CoverageBuilder.faker(VersicherungsArtDeBasis.GKV).version(kbvForVersion).build();
    val patient = PatientBuilder.faker(IdentifierTypeDe.GKV).version(kbvForVersion).build();
    val practitioner = PractitionerBuilder.faker().version(kbvForVersion).build();
    val custodian = MedicalOrganizationBuilder.faker().version(kbvForVersion).build();

    val mvo = MultiplePrescriptionExtension.asNonMultiple();
    val kbvBundleBuilder =
        KbvErpBundleBuilder.builder()
            .version(kbvErpVersion)
            .medication(medication)
            .medicationRequest(
                MedicationRequestBuilder.faker(patient)
                    .version(kbvErpVersion)
                    .medication(medication)
                    .mvo(mvo)
                    .insurance(coverage)
                    .requester(practitioner)
                    .build());

    kbvBundleBuilder
        .insurance(coverage)
        .patient(patient)
        .practitioner(practitioner)
        .custodian(custodian)
        .prescriptionId(PrescriptionId.random());

    val result = ValidatorUtil.encodeAndValidate(parser, kbvBundleBuilder.build());
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Get Practitioner from KBV Bundle with versions KbvItaForVersion {0} and KbvItaErpVersion {1}")
  @MethodSource("kbvBundleVersions")
  void shouldGetPractitionersFromKbvBundle(
      KbvItaForVersion kbvForVersion, KbvItaErpVersion kbvErpVersion) {

    val qualificationTypes = List.of(QualificationType.DOCTOR, QualificationType.DENTIST);

    qualificationTypes.forEach(
        qt -> {
          val practitioner =
              PractitionerBuilder.faker()
                  .version(kbvForVersion)
                  .anr(BaseANR.randomFromQualification(qt))
                  .build();
          val kbvBundle =
              KbvErpBundleBuilder.faker("X123456789")
                  .version(kbvErpVersion)
                  .practitioner(practitioner)
                  .build();
          val actual = kbvBundle.getPractitioner();
          assertEquals(practitioner.getANRType(), actual.getANRType());
        });
  }
}