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

package de.gematik.test.erezept.primsys.data.valuesets;

import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.jupiter.api.Test;

class WopDtoTest {

  @Test
  void shouldBuildFromCode() {
    val value1 = WopDto.fromCode("72");
    val value2 = WopDto.fromCode("Berlin");
    val value3 = WopDto.fromCode("Nordwürttemberg");
    assertEquals(WopDto.BERLIN, value1);
    assertEquals(value1, value2);
    assertEquals(WopDto.NORD_WUERTTEMBERG, value3);
  }

  @Test
  void shouldThrowOnNullCode() {
    assertThrows(NullPointerException.class, () -> WopDto.fromCode(null));
  }
}
