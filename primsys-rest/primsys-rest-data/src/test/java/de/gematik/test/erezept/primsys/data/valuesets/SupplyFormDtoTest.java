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

package de.gematik.test.erezept.primsys.data.valuesets;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import java.util.Arrays;
import lombok.val;
import org.junit.jupiter.api.Test;

class SupplyFormDtoTest {

  @Test
  void shouldBuildFromCode() {
    val value1 = SupplyFormDto.fromCode("AUS");
    val value2 = SupplyFormDto.fromCode("Augensalbe");
    assertEquals(SupplyFormDto.AUS, value1);
    assertEquals(value1, value2);
  }

  @Test
  void shouldThrowOnNullCode() {
    assertThrows(NullPointerException.class, () -> SupplyFormDto.fromCode(null));
  }

  @Test
  void shouldHaveAllDarreichungsformen() {
    Arrays.stream(Darreichungsform.values())
        .forEach(
            vs -> {
              val dto = assertDoesNotThrow(() -> SupplyFormDto.fromCode(vs.getCode()));
              assertEquals(vs.getCode(), dto.getCode());
              assertEquals(vs.getDisplay(), dto.getDisplay());
            });
  }

  @Test
  void shouldNotHaveAnyExtraSupplyForms() {
    Arrays.stream(SupplyFormDto.values())
        .forEach(
            dto -> {
              val vs = assertDoesNotThrow(() -> Darreichungsform.fromCode(dto.getCode()));
              assertEquals(vs.getCode(), dto.getCode());
              assertEquals(vs.getDisplay(), dto.getDisplay());
            });
  }
}
