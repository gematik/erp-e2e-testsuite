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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class KbvErpMedicationRequestBuilderTest extends ParsingTest {

  private static Stream<Arguments> kbvBundleVersions() {
    return Stream.of(Arguments.of(KbvItaErpVersion.V1_0_2), Arguments.of(KbvItaErpVersion.V1_1_0));
  }

  @ParameterizedTest(
      name =
          "[{index}] -> Build KBV MedicationRequest with Accident in versions KbvItaErpVersion {0}")
  @MethodSource("kbvBundleVersions")
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
          "[{index}] -> Build KBV MedicationRequest with Accident at Work in versions KbvItaErpVersion {0}")
  @MethodSource("kbvBundleVersions")
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
          "[{index}] -> Build KBV MedicationRequest with Accident at Work in versions KbvItaErpVersion {0}")
  @MethodSource("kbvBundleVersions")
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
          "[{index}] -> Build KBV MedicationRequest with random Accident in versions KbvItaErpVersion {0}")
  @MethodSource("kbvBundleVersions")
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
          "[{index}] -> Build random KBV MedicationRequest with faker in versions KbvItaErpVersion {0}")
  @MethodSource("kbvBundleVersions")
  void shouldBuildMedicationRequestWithFaker(KbvItaErpVersion version) {
    val medicationRequest = MedicationRequestBuilder.faker().version(version).build();
    val result = ValidatorUtil.encodeAndValidate(parser, medicationRequest);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldUseUnkownStatusOnInvalidCode() {
    val medicationRequest = MedicationRequestBuilder.faker().status("abc").build();
    assertEquals(MedicationRequest.MedicationRequestStatus.UNKNOWN, medicationRequest.getStatus());
  }

  @Test
  void shouldUseNullIntentOnInvalidCode() {
    val medicationRequest = MedicationRequestBuilder.faker().intent("abc").build();
    assertEquals(MedicationRequest.MedicationRequestIntent.NULL, medicationRequest.getIntent());
  }
}
