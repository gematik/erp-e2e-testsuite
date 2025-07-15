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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import lombok.val;
import org.hl7.fhir.r4.model.MedicationRequest;
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
            .quantityPackages(20)
            .status("active") // default ACTIVE
            .intent("order") // default ORDER
            .isBVG(false) // Bundesversorgungsgesetz default true
            .hasEmergencyServiceFee(true) // default false
            .substitution(false) // default true
            .coPaymentStatus(StatusCoPayment.STATUS_0) // default StatusCoPayment.STATUS_0
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    System.out.println(result.getMessages());

    assertTrue(result.isSuccessful());

    val dosage = medicationRequest.getDosageInstructionFirstRep();
    assertFalse(dosage.hasText());
    assertFalse(medicationRequest.hasNote());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with Accident in versions KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldBuildMedicationRequestAndSkipEmptyDosageInstruction(KbvItaErpVersion version) {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withVersion(version)
            .withDosageInstruction("")
            .fake();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());

    val dosage = medicationRequest.getDosageInstructionFirstRep();
    assertFalse(dosage.hasText());
    val dosageExtension =
        dosage.getExtensionByUrl(KbvItaErpStructDef.DOSAGE_FLAG.getCanonicalUrl());
    val hasDosageInstruction = dosageExtension.getValue().castToBoolean(dosageExtension.getValue());
    assertFalse(hasDosageInstruction.booleanValue());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with Accident in versions KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldBuildMedicationRequestWithAccident(KbvItaErpVersion version) {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withVersion(version)
            .withAccident(AccidentExtension.accident())
            .fake();

    assertTrue(parser.isValid(medicationRequest));
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with CoPayment Status in versions"
              + " KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldBuildMedicationRequestWithStatusCoPayment(KbvItaErpVersion version) {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withVersion(version)
            .withCoPaymentStatus(StatusCoPayment.STATUS_0)
            .fake();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
    assertTrue(medicationRequest.getCoPaymentStatus().isPresent());
    assertEquals(StatusCoPayment.STATUS_0, medicationRequest.getCoPaymentStatus().get());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with Accident at Work in versions"
              + " KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldBuildMedicationRequestWithAccidentAtWork(KbvItaErpVersion version) {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withVersion(version)
            .withAccident(AccidentExtension.accidentAtWork().atWorkplace())
            .fake();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with Accident at Work in versions"
              + " KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldBuildMedicationRequestWithOccupationalDisease(KbvItaErpVersion version) {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withVersion(version)
            .withAccident(AccidentExtension.occupationalDisease())
            .fake();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with random Accident in versions"
              + " KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldBuildMedicationRequestWithFakerAccident(KbvItaErpVersion version) {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withVersion(version)
            .withAccident(AccidentExtension.faker())
            .fake();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with random Accident in versions"
              + " KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldBuildMedicationRequestWithRandomMvoIdentifier(KbvItaErpVersion version) {
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withVersion(version)
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
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldBuildMedicationRequestWithFaker(KbvItaErpVersion version) {
    val medicationRequest = KbvErpMedicationRequestFaker.builder().withVersion(version).fake();
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
    val medicationRequest =
        KbvErpMedicationRequestBuilder.forPatient(KbvPatientFaker.builder().fake())
            .version(KbvItaErpVersion.V1_2_0)
            .prescriberId("123456789")
            .insurance(KbvCoverageFaker.builder().fake())
            .requester(KbvPractitionerFaker.builder().fake())
            .medication(KbvErpMedicationPZNFaker.builder().fake())
            .quantityPackages(20)
            .build();

    assertTrue(parser.isValid(medicationRequest));
  }
}
