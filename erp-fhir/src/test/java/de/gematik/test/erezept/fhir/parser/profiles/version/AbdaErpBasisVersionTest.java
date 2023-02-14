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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.parser.profiles.*;
import java.util.*;
import lombok.*;
import org.junit.jupiter.api.*;
import org.junitpioneer.jupiter.*;

class AbdaErpBasisVersionTest {

  @Test
  void getDefaultVersionViaCurrentDate() {
    val defaultVersion = AbdaErpBasisVersion.getDefaultVersion();

    // Note: this assertion will break in the future!
    assertEquals(AbdaErpBasisVersion.V1_2_1, defaultVersion);
  }

  @Test
  @ClearSystemProperty(key = "de.abda.erezeptabgabedatenbasis")
  void getDefaultVersionViaSystemProperty() {
    val propertyName = CustomProfiles.ABDA_ERP_BASIS.getName();
    Arrays.stream(AbdaErpBasisVersion.values())
        .forEach(
            version -> {
              System.setProperty(propertyName, version.getVersion());
              val defaultVersion = AbdaErpBasisVersion.getDefaultVersion();
              assertEquals(version, defaultVersion);
            });
  }

  @Test
  void shouldCompareVersions() {
    assertEquals(1, AbdaErpBasisVersion.V1_3_0.compareTo(AbdaErpBasisVersion.V1_2_1));
    assertEquals(0, AbdaErpBasisVersion.V1_2_1.compareTo(AbdaErpBasisVersion.V1_2_1));
    assertEquals(0, AbdaErpBasisVersion.V1_3_0.compareTo(AbdaErpBasisVersion.V1_3_0));
    assertEquals(-1, AbdaErpBasisVersion.V1_2_1.compareTo(AbdaErpBasisVersion.V1_3_0));
  }

  @AfterEach
  void cleanProperties() {
    val propertyName = CustomProfiles.ABDA_ERP_BASIS.getName();
    System.clearProperty(propertyName);
  }
}
