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
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.util.Arrays;
import lombok.val;
import org.junit.jupiter.api.Test;

class StandardSizeDtoTest {

  @Test
  void shouldDecodeFromCode() {
    val result = StandardSizeDto.fromCode("KTP");
    val result2 = StandardSizeDto.fromCode("Normgröße 1");
    val result3 = StandardSizeDto.fromCode("SONSTIGES");
    assertEquals(StandardSizeDto.KTP, result);
    assertEquals(StandardSizeDto.N1, result2);
    assertEquals(StandardSizeDto.SONSTIGES, result3);
  }

  @Test
  void shouldThrowOnNullCode() {
    assertThrows(NullPointerException.class, () -> StandardSizeDto.fromCode(null));
  }

  @Test
  void shouldHaveAllStandardsizes() {
    Arrays.stream(StandardSize.values())
        .forEach(
            vs -> {
              val dto = assertDoesNotThrow(() -> StandardSizeDto.fromCode(vs.getCode()));
              assertEquals(vs.getCode(), dto.getCode());
              assertEquals(vs.getDisplay(), dto.getDisplay());
            });
  }

  @Test
  void shouldNotHaveAnyExtraStandardsizes() {
    Arrays.stream(StandardSizeDto.values())
        .forEach(
            dto -> {
              val vs = assertDoesNotThrow(() -> StandardSize.fromCode(dto.getCode()));
              assertEquals(vs.getCode(), dto.getCode());
              assertEquals(vs.getDisplay(), dto.getDisplay());
            });
  }
}
