/*
 * Copyright 2024 gematik GmbH
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

import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class DeBasisVersionTest {

  @Test
  void shouldParseDeBasisProfilVersion() {
    val sources0913 = List.of("profiles/de.basisprofil.r4-0.9.13/package/Profile", "0.9.13");

    sources0913.forEach(
        input -> {
          val version = DeBasisVersion.fromString(input);
          assertEquals(DeBasisVersion.V0_9_13, version);
        });

    val sources132 = List.of("profiles/de.basisprofil.r4-1.3.2/package/Profile", "1.3.2");

    sources132.forEach(
        input -> {
          val version = DeBasisVersion.fromString(input);
          assertEquals(DeBasisVersion.V1_3_2, version);
        });
  }

  @Test
  void shouldGetDefaultVersion() {
    val defaultVersion = DeBasisVersion.getDefaultVersion();
    assertEquals(DeBasisVersion.V0_9_13, defaultVersion);
    assertEquals("0.9.13", defaultVersion.getVersion());
  }

  @Test
  void shouldCompareVersions() {
    assertEquals(1, DeBasisVersion.V1_3_2.compareTo(DeBasisVersion.V0_9_13));
    assertEquals(0, DeBasisVersion.V0_9_13.compareTo(DeBasisVersion.V0_9_13));
    assertEquals(0, DeBasisVersion.V1_3_2.compareTo(DeBasisVersion.V1_3_2));
    assertEquals(-1, DeBasisVersion.V0_9_13.compareTo(DeBasisVersion.V1_3_2));
  }
}
