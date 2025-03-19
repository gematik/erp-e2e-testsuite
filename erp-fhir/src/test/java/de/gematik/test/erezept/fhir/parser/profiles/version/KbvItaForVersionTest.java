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

package de.gematik.test.erezept.fhir.parser.profiles.version;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import java.util.Arrays;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

class KbvItaForVersionTest extends ErpFhirBuildingTest {

  @Test
  void getDefaultVersionViaCurrentDate() {
    val defaultVersion = KbvItaForVersion.getDefaultVersion();

    // Note: this assertion will break in the future!
    assertEquals(KbvItaForVersion.V1_1_0, defaultVersion);
  }

  @ParameterizedTest
  @MethodSource
  @ClearSystemProperty(key = KbvItaForVersion.PROFILE_NAME)
  void getDefaultVersionViaSystemProperty(KbvItaForVersion version) {
    System.setProperty(KbvItaForVersion.PROFILE_NAME, version.getVersion());
    val defaultVersion = KbvItaForVersion.getDefaultVersion();
    assertEquals(version, defaultVersion);
  }

  static Stream<Arguments> getDefaultVersionViaSystemProperty() {
    return Arrays.stream(KbvItaForVersion.values()).map(Arguments::of);
  }
}
