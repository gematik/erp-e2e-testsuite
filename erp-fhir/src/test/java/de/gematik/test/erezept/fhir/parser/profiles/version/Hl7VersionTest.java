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
import lombok.val;
import org.junit.jupiter.api.Test;

class Hl7VersionTest {

  @Test
  void shouldGetDefaultVersion() {
    val defaultVersion = Hl7Version.getDefaultVersion();
    assertEquals(Hl7Version.V1_1_1, defaultVersion);
    assertEquals("1.0.0", defaultVersion.getVersion());
  }

  @Test
  void getDefaultVersionViaInterface() {
    val defaultVersion = ProfileVersion.getDefaultVersion(Hl7Version.class, CustomProfiles.HL7);
    assertEquals(Hl7Version.V1_1_1, defaultVersion);
    assertEquals("1.0.0", defaultVersion.getVersion());
  }
}
