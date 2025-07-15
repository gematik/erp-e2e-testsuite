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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.primsys.data.valuesets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.fhir.valuesets.PayorType;
import java.util.Arrays;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PayorTypeDtoTest {

  @ParameterizedTest
  @EnumSource(PayorTypeDto.class)
  void shouldDecodeFromCode(PayorTypeDto template) {
    val decoded = PayorTypeDto.fromCode(template.name());
    assertEquals(template, decoded);
  }

  @ParameterizedTest
  @EnumSource(PayorTypeDto.class)
  void shouldDecodeFromDisplay(PayorTypeDto template) {
    val decoded = PayorTypeDto.fromCode(template.getDisplay());
    assertEquals(template, decoded);
  }

  @Test
  void shouldHaveAllPayorTypes() {
    Arrays.stream(PayorType.values())
        .forEach(
            vs -> {
              val dto = assertDoesNotThrow(() -> PayorTypeDto.fromCode(vs.getCode()));
              assertEquals(vs.getDisplay(), dto.getDisplay());
            });
  }

  @Test
  void shouldNotHaveAnyExtraPayorTypes() {
    Arrays.stream(PayorTypeDto.values())
        .forEach(
            dto -> {
              val vs = assertDoesNotThrow(() -> PayorType.fromDisplay(dto.getDisplay()));
              assertEquals(vs.getDisplay(), dto.getDisplay());
            });
  }
}
