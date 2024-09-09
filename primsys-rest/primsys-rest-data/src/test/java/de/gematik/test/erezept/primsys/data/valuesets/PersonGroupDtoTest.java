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

package de.gematik.test.erezept.primsys.data.valuesets;

import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.jupiter.api.Test;

class PersonGroupDtoTest {

  @Test
  void shouldBuildFromCode() {
    val value1 = PersonGroupDto.fromCode("04");
    val value2 = PersonGroupDto.fromCode("SOZ");
    val value3 = PersonGroupDto.fromCode("Nicht gesetzt");
    assertEquals(PersonGroupDto.SOZ, value1);
    assertEquals(value1, value2);
    assertEquals(PersonGroupDto.NOT_SET, value3);
  }

  @Test
  void shouldThrowOnNullCode() {
    assertThrows(NullPointerException.class, () -> PersonGroupDto.fromCode(null));
  }
}
