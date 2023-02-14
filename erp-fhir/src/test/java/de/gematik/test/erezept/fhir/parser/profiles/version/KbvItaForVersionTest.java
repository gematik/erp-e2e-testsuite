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

package de.gematik.test.erezept.fhir.parser.profiles.version;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.fhir.parser.profiles.CustomProfiles;
import java.util.Arrays;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class KbvItaForVersionTest {

  @Test
  void getDefaultVersionViaCurrentDate() {
    val defaultVersion = KbvItaForVersion.getDefaultVersion();

    // Note: this assertion will break in the future!
    assertEquals(KbvItaForVersion.V1_0_3, defaultVersion);
  }

  @Test
  void getDefaultVersionViaSystemProperty() {
    val propertyName = CustomProfiles.KBV_ITA_FOR.getName();
    Arrays.stream(KbvItaForVersion.values())
        .forEach(
            version -> {
              System.setProperty(propertyName, version.getVersion());
              val defaultVersion = KbvItaForVersion.getDefaultVersion();
              assertEquals(version, defaultVersion);
            });
  }

  @AfterEach
  void cleanProperties() {
    val propertyName = CustomProfiles.KBV_ITA_FOR.getName();
    System.clearProperty(propertyName);
  }
}
