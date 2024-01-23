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

package de.gematik.test.erezept.fhir.builder.kbv;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.extensions.kbv.*;
import de.gematik.test.erezept.fhir.parser.profiles.version.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import lombok.*;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class KbvErpMedicationRequestBuilderTest extends ParsingTest {

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with Accident in versions KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldBuildMedicationRequestWithAccident(KbvItaErpVersion version) {
    val medicationRequest =
        MedicationRequestBuilder.faker()
            .version(version)
            .accident(AccidentExtension.accident())
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with CoPayment Status in versions"
              + " KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldBuildMedicationRequestWithStatusCoPayment(KbvItaErpVersion version) {
    val medicationRequest =
        MedicationRequestBuilder.faker()
            .version(version)
            .coPaymentStatus(StatusCoPayment.STATUS_0)
            .build();

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
        MedicationRequestBuilder.faker()
            .version(version)
            .accident(AccidentExtension.accidentAtWork().atWorkplace())
            .build();

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
        MedicationRequestBuilder.faker()
            .version(version)
            .accident(AccidentExtension.occupationalDisease())
            .build();

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
        MedicationRequestBuilder.faker()
            .version(version)
            .accident(AccidentExtension.faker())
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with random Accident in versions"
              + " KbvItaErpVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldBuildMedicationRequestWithMvoIdentifier(KbvItaErpVersion version) {
    val medicationRequest =
        MedicationRequestBuilder.faker()
            .version(version)
            .mvo(MultiplePrescriptionExtension.asMultiple(1, 4).withRandomId().validForDays(365))
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build random KBV MedicationRequest with faker in versions KbvItaErpVersion"
              + " {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaErpVersions")
  void shouldBuildMedicationRequestWithFaker(KbvItaErpVersion version) {
    val medicationRequest = MedicationRequestBuilder.faker().version(version).build();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldUseUnknownStatusOnInvalidCode() {
    val medicationRequest = MedicationRequestBuilder.faker().status("abc").build();
    assertEquals(MedicationRequest.MedicationRequestStatus.UNKNOWN, medicationRequest.getStatus());
  }

  @Test
  void shouldUseNullIntentOnInvalidCode() {
    val medicationRequest = MedicationRequestBuilder.faker().intent("abc").build();
    assertEquals(MedicationRequest.MedicationRequestIntent.NULL, medicationRequest.getIntent());
  }
}
