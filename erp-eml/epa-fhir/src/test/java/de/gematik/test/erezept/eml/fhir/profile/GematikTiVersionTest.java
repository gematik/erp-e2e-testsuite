/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.eml.fhir.profile;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class GematikTiVersionTest {

  @Test
  void shouldHaveExpectedVersionString() {
    assertEquals("1.2.0", GematikTiVersion.V1_2_0.getVersion());
  }

  @Test
  void shouldHaveExpectedName() {
    assertEquals("de.gematik.ti", GematikTiVersion.V1_2_0.getName());
  }

  @Test
  void shouldNotOmitZeroPatch() {
    assertFalse(GematikTiVersion.V1_2_0.omitZeroPatch());
  }

  @Test
  void shouldNotOmitPatch() {
    assertFalse(GematikTiVersion.V1_2_0.omitPatch());
  }

  @Test
  void shouldContainExactlyOneEnumConstant() {
    assertArrayEquals(new GematikTiVersion[] {GematikTiVersion.V1_2_0}, GematikTiVersion.values());
  }
}
