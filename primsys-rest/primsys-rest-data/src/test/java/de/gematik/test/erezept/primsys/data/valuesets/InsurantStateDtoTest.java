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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.fhir.valuesets.VersichertenStatus;
import java.util.Arrays;
import lombok.val;
import org.junit.jupiter.api.Test;

class InsurantStateDtoTest {

  @Test
  void shouldGetFromCode() {
    val value = InsurantStateDto.fromCode("1");
    assertEquals(InsurantStateDto.MEMBERS, value);
  }

  @Test
  void shouldGetFromDisplay() {
    val value = InsurantStateDto.fromCode("Rentner");
    assertEquals(InsurantStateDto.PENSIONER, value);
  }

  @Test
  void shouldGetFromName() {
    val value = InsurantStateDto.fromCode("Family_Members");
    val value2 = InsurantStateDto.fromCode("Family members");
    assertEquals(InsurantStateDto.FAMILY_MEMBERS, value);
    assertEquals(value, value2);
  }

  @Test
  void shouldHaveAllInsuranceStates() {
    Arrays.stream(VersichertenStatus.values())
        .forEach(
            vs -> {
              val dto = assertDoesNotThrow(() -> InsurantStateDto.fromCode(vs.getCode()));
              assertEquals(vs.getCode(), dto.getCode());
              assertEquals(vs.getDisplay(), dto.getDisplay());
            });
  }

  @Test
  void shouldNotHaveAnyExtraInsuranceStates() {
    Arrays.stream(InsurantStateDto.values())
        .forEach(
            dto -> {
              val vs = assertDoesNotThrow(() -> VersichertenStatus.fromCode(dto.getCode()));
              assertEquals(vs.getCode(), dto.getCode());
              assertEquals(vs.getDisplay(), dto.getDisplay());
            });
  }
}
