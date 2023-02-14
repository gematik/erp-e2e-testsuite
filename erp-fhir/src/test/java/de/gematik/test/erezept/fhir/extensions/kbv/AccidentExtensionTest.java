/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.extensions.kbv;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.parser.profiles.CustomProfiles;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AccidentExtensionTest {

  private static Stream<Arguments> kbvBundleVersions() {
    return Stream.of(Arguments.of(KbvItaErpVersion.V1_0_2), Arguments.of(KbvItaErpVersion.V1_1_0));
  }

  @AfterEach
  void cleanVersionProperties() {
    System.clearProperty(CustomProfiles.KBV_ITA_ERP.getName());
  }

  @ParameterizedTest(name = "[{index}] -> AccidentExtension in versions KbvItaErpVersion {0}")
  @MethodSource("kbvBundleVersions")
  void shouldBuildAccidentExtension(KbvItaErpVersion kbvItaErpVersion) {
    System.setProperty(
        kbvItaErpVersion.getCustomProfile().getName(), kbvItaErpVersion.getVersion());

    val ae = AccidentExtension.accident();
    val outer = ae.asExtension();
    val kennzeichen =
        outer.getExtension().stream()
            .filter(e -> e.getUrl().equalsIgnoreCase("unfallkennzeichen"))
            .findAny();
    assertTrue(kennzeichen.isPresent());

    val unfalltag =
        outer.getExtension().stream()
            .filter(e -> e.getUrl().equalsIgnoreCase("unfalltag"))
            .findAny();
    assertTrue(unfalltag.isPresent());

    assertTrue(
        ae.toString()
            .contains(
                ae.accidentCauseType().getDisplay())); // well, simply covering the toString :/
  }

  @ParameterizedTest(name = "[{index}] -> AccidentExtension in versions KbvItaErpVersion {0}")
  @MethodSource("kbvBundleVersions")
  void shouldBuildAccidentExtensionWithFaker(KbvItaErpVersion kbvItaErpVersion) {
    System.setProperty(
        kbvItaErpVersion.getCustomProfile().getName(), kbvItaErpVersion.getVersion());

    for (var i = 0; i < 5; i++) {
      val ae = AccidentExtension.faker();
      val outer = ae.asExtension();
      val kennzeichen =
          outer.getExtension().stream()
              .filter(e -> e.getUrl().equalsIgnoreCase("unfallkennzeichen"))
              .findAny();
      assertTrue(kennzeichen.isPresent());
    }
  }
}
