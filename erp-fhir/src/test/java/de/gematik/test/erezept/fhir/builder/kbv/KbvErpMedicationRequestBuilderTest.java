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

import static de.gematik.test.erezept.fhir.profiles.definitions.KbvItaForStructDef.SER_EXTENSION;
import static de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem.UCUM;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.builder.dgmp.DosageDgMPBuilder;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.dgmp.DosageDgMP;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.BmpDosiereinheit;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class KbvErpMedicationRequestBuilderTest extends ErpFhirParsingTest {

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with Accident in versions KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldBuildMedicationRequestWithoutDosageInstruction(KbvItaErpVersion version) {
    val medicationRequest =
        KbvErpMedicationRequestBuilder.forPatient(KbvPatientFaker.builder().fake())
            .version(version)
            .insurance(KbvCoverageFaker.builder().fake())
            .requester(KbvPractitionerFaker.builder().fake())
            .medication(KbvErpMedicationPZNFaker.builder().fake())
            .dispenseRequestQuantity(20)
            .status("active") // default ACTIVE
            .intent("order") // default ORDER
            .isBVG(false) // Bundesversorgungsgesetz default true
            .hasEmergencyServiceFee(true) // default false
            .substitution(false) // default true
            .coPaymentStatus(StatusCoPayment.STATUS_1) // default StatusCoPayment.STATUS_0
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    System.out.println(result.getMessages());

    assertTrue(result.isSuccessful());

    val dosageIntsr = medicationRequest.getDosageInstructionFirstRep();
    assertFalse(dosageIntsr.hasText());
    assertFalse(medicationRequest.hasNote());
  }

  @Test
  void shouldFailWhileMissingSupplDuration() {
    val medicationRequest =
        KbvErpMedicationRequestBuilder.forPatient(KbvPatientFaker.builder().fake())
            .version(KbvItaErpVersion.V1_4_0)
            .insurance(KbvCoverageFaker.builder().fake())
            .requester(KbvPractitionerFaker.builder().fake())
            .medication(
                KbvErpMedicationPZNFaker.builder().withCategory(MedicationCategory.C_02).fake())
            .dispenseRequestQuantity(20)
            .status("active") // default ACTIVE
            .intent("order") // default ORDER
            .hasEmergencyServiceFee(true) // default false
            .substitution(false) // default true
            .coPaymentStatus(StatusCoPayment.STATUS_1) // default StatusCoPayment.STATUS_0
        ;
    assertThrows(BuilderException.class, medicationRequest::build);
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with Accident in versions KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void shouldBuildMedicationRequestAndSkipEmptyDosageInstruction(
      KbvItaForVersion forVersion, KbvItaErpVersion erpVersion) {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder(erpVersion, forVersion)
            .withDosageInstruction("")
            .fake();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());

    val firstDosage = medicationRequest.getDosageInstructionFirstRep();
    assertFalse(firstDosage.hasText());
    val dosageExtension =
        firstDosage.getExtensionByUrl(KbvItaErpStructDef.DOSAGE_FLAG.getCanonicalUrl());
    boolean hasDosageInstruction = true;
    if (erpVersion.isSmallerThanOrEqualTo(KbvItaErpVersion.V1_3_0)) {
      hasDosageInstruction =
          dosageExtension.getValue().castToBoolean(dosageExtension.getValue()).booleanValue();
    } else {
      hasDosageInstruction =
          medicationRequest.getDosageInstructionDgMPs().stream()
              .map(Dosage::hasTextElement)
              .findFirst()
              .orElse(false);
    }
    assertFalse(hasDosageInstruction);
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with Accident in versions KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void shouldBuildMedicationRequestWithAccident(
      KbvItaForVersion forVersion, KbvItaErpVersion erpVersion) {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder(erpVersion, forVersion)
            .withAccident(AccidentExtension.accident())
            .fake();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with CoPayment Status in versions"
              + " KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void shouldBuildMedicationRequestWithStatusCoPayment(
      KbvItaForVersion forVersion, KbvItaErpVersion erpVersion) {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder(erpVersion, forVersion)
            .withCoPaymentStatus(StatusCoPayment.STATUS_0)
            .fake();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
    assertTrue(medicationRequest.getCoPaymentStatus().isPresent());
    assertEquals(
        isSERAndProfVers_1_4(medicationRequest)
            ? StatusCoPayment.STATUS_1
            : StatusCoPayment.STATUS_0,
        medicationRequest.getCoPaymentStatus().get());
  }

  private boolean isSERAndProfVers_1_4(KbvErpMedicationRequest medicationRequest) {
    return medicationRequest.getExtension().stream()
            .filter(SER_EXTENSION::matches)
            .map(ex -> ex.getValue().castToBoolean(ex.getValue()).getValue())
            .findFirst()
            .orElse(false)
        && medicationRequest.getMeta().getProfile().get(0).getValue().endsWith("1.4");
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with Accident at Work in versions"
              + " KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void shouldBuildMedicationRequestWithAccidentAtWork(
      KbvItaForVersion forVersion, KbvItaErpVersion erpVersion) {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder(erpVersion, forVersion)
            .withAccident(AccidentExtension.accidentAtWork().atWorkplace())
            .fake();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with Accident at Work in versions"
              + " KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void shouldBuildMedicationRequestWithOccupationalDisease(
      KbvItaForVersion forVersion, KbvItaErpVersion erpVersion) {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder(erpVersion, forVersion)
            .withAccident(AccidentExtension.occupationalDisease())
            .fake();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with random Accident in versions"
              + " KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void shouldBuildMedicationRequestWithFakerAccident(
      KbvItaForVersion forVersion, KbvItaErpVersion erpVersion) {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder(erpVersion, forVersion)
            .withAccident(AccidentExtension.faker())
            .fake();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with random Accident in versions"
              + " KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void shouldBuildMedicationRequestWithRandomMvoIdentifier(
      KbvItaForVersion forVersion, KbvItaErpVersion erpVersion) {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder(erpVersion, forVersion)
            .withMvo(
                MultiplePrescriptionExtension.asMultiple(1, 4).withRandomId().validForDays(365))
            .fake();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
    assertTrue(medicationRequest.getMvoId().isPresent());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build random KBV MedicationRequest with faker in versions KbvItaErpVersion"
              + " {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void shouldBuildMedicationRequestWithFaker(
      KbvItaForVersion forVersion, KbvItaErpVersion erpVersion) {
    val medicationRequest = KbvErpMedicationRequestFaker.builder(erpVersion, forVersion).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldUseUnknownStatusOnInvalidCode() {
    val medicationRequest = KbvErpMedicationRequestFaker.builder().withStatus("abc").fake();
    assertEquals(MedicationRequest.MedicationRequestStatus.UNKNOWN, medicationRequest.getStatus());
  }

  @Test
  void shouldUseNullIntentOnInvalidCode() {
    val medicationRequest = KbvErpMedicationRequestFaker.builder().withIntent("abc").fake();
    assertEquals(MedicationRequest.MedicationRequestIntent.NULL, medicationRequest.getIntent());
  }

  @Test
  void shouldSetPrescriberId() {
    val erpVersion = KbvItaErpVersion.V1_3_0;
    val forVersion = KbvItaForVersion.V1_2_0;
    val medicationRequest =
        KbvErpMedicationRequestBuilder.forPatient(KbvPatientFaker.builder(forVersion).fake())
            .version(erpVersion)
            .prescriberId("123456789")
            .insurance(KbvCoverageFaker.builder(forVersion).fake())
            .requester(KbvPractitionerFaker.builder(forVersion).fake())
            .medication(KbvErpMedicationPZNFaker.builder(erpVersion).fake())
            .dispenseRequestQuantity(20)
            .build();

    assertTrue(parser.isValid(medicationRequest));
  }

  @Test
  void shouldSetDosageDgmp() {

    val medicationRequest =
        KbvErpMedicationRequestBuilder.forPatient(KbvPatientFaker.builder().fake())
            .version(KbvItaErpVersion.V1_4_0)
            .prescriberId("123456789")
            .insurance(KbvCoverageFaker.builder().fake())
            .requester(KbvPractitionerFaker.builder().fake())
            .dgmp(dosage)
            .medication(KbvErpMedicationPZNFaker.builder().fake())
            .dispenseRequestQuantity(20)
            .build();

    assertTrue(parser.isValid(medicationRequest));
    assertEquals(
        Optional.of(5),
        medicationRequest.getDosageInstruction().stream()
            .flatMap(it -> it.getDoseAndRate().stream())
            .map(dAR -> dAR.getDoseQuantity().getValue().intValue())
            .findFirst());
  }

  @Test
  void shouldSetDosageDgmpAsList() {
    val medicationRequest =
        KbvErpMedicationRequestBuilder.forPatient(KbvPatientFaker.builder().fake())
            .version(KbvItaErpVersion.V1_4_0)
            .prescriberId("123456789")
            .insurance(KbvCoverageFaker.builder().fake())
            .requester(KbvPractitionerFaker.builder().fake())
            .medication(KbvErpMedicationPZNFaker.builder().fake())
            .dgmp(List.of(dosage))
            .dispenseRequestQuantity(20)
            .build();

    assertTrue(ValidatorUtil.encodeAndValidate(parser, medicationRequest).isSuccessful());

    assertEquals(
        Optional.of(5),
        medicationRequest.getDosageInstruction().stream()
            .map(
                dI ->
                    dI.getDoseAndRate().stream()
                        .map(dAR -> dAR.getDoseQuantity().getValue().intValue())
                        .findFirst()
                        .orElseThrow())
            .findFirst());
  }

  @Test
  void shouldSetIncorrectDosageDgmpAsList() {
    val incorrectDosage =
        DosageDgMPBuilder.dosageBuilder("Tablette", BmpDosiereinheit.AUGENBADEWANNE)
            .value(new BigDecimal(5))
            .timing(5, 3, Timing.UnitsOfTime.D)
            .text("this Text is to much following anm KBV constrained")
            .build();

    val medicationRequest =
        KbvErpMedicationRequestBuilder.forPatient(KbvPatientFaker.builder().fake())
            .version(KbvItaErpVersion.V1_4_0)
            .prescriberId("123456789")
            .insurance(KbvCoverageFaker.builder().fake())
            .requester(KbvPractitionerFaker.builder().fake())
            .medication(KbvErpMedicationPZNFaker.builder().fake())
            .dgmp(List.of(incorrectDosage))
            .dispenseRequestQuantity(20)
            .build();

    assertFalse(ValidatorUtil.encodeAndValidate(parser, medicationRequest).isSuccessful());
  }

  @Test
  void shouldSetDispRequestQuantityInOldVersion() {
    val medicationRequest =
        KbvErpMedicationRequestBuilder.forPatient(KbvPatientFaker.builder().fake())
            .version(KbvItaErpVersion.V1_1_0)
            .prescriberId("123456789")
            .insurance(KbvCoverageFaker.builder().fake())
            .requester(KbvPractitionerFaker.builder().fake())
            .dispenseRequestQuantity(1)
            .medication(KbvErpMedicationPZNFaker.builder().fake())
            .build();
    assertEquals(
        UCUM.getCanonicalUrl(), medicationRequest.getDispenseRequest().getQuantity().getSystem());
    assertEquals(1, medicationRequest.getDispenseRequest().getQuantity().getValue().intValue());
    assertEquals("{Package}", medicationRequest.getDispenseRequest().getQuantity().getCode());
  }

  @Test
  void shouldSetDispRequestQuantityInOldVersionWithoutDispenseRequest() {
    val medReq =
        KbvErpMedicationRequestBuilder.forPatient(KbvPatientFaker.builder().fake())
            .version(KbvItaErpVersion.V1_1_0)
            .status("")
            .intent("")
            .insurance(KbvCoverageFaker.builder().fake())
            .build();
    assertTrue(medReq.hasStatus());
    assertTrue(medReq.hasDispenseRequest());
    assertEquals("{Package}", medReq.getDispenseRequest().getQuantity().getCode());
  }

  @Test
  void shouldLooseSubstitutionInCaseOfIngredientMedication() {
    val med = KbvErpMedicationIngredientFaker.builder().fake();
    val medRequest =
        KbvErpMedicationRequestBuilder.forPatient(KbvPatientFaker.builder().fake())
            .medication(med)
            .insurance(KbvCoverageFaker.builder().fake())
            .substitution(true)
            .expectedSupplyDurationInWeeks(2)
            .dispenseRequestQuantity(2)
            .build();
    assertFalse(medRequest.hasSubstitution());
  }

  private DosageDgMP dosage =
      DosageDgMPBuilder.dosageBuilder("Tablette", BmpDosiereinheit.AUGENBADEWANNE)
          .value(new BigDecimal(5))
          .timing(5, 3, Timing.UnitsOfTime.D)
          .build();
}
