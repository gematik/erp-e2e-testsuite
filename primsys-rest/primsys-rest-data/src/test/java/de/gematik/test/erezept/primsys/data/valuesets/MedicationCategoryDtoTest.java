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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import java.util.Arrays;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class MedicationCategoryDtoTest {

  @Test
  void shouldDecodeFromCode() {
    val mcd = MedicationCategoryDto.fromCode("00");
    assertEquals(MedicationCategoryDto.C_00, mcd);
  }

  @Test
  void shouldDecodeFromName() {
    val mcd = MedicationCategoryDto.fromCode("C_01");
    assertEquals(MedicationCategoryDto.C_01, mcd);
  }

  @Test
  void shouldDecodeFromDisplay() {
    val mcd = MedicationCategoryDto.fromCode("Thalidomid");
    assertEquals(MedicationCategoryDto.C_02, mcd);
  }

  @ParameterizedTest
  @ValueSource(strings = {"03", "0", "sonstiges", "misc"})
  @NullSource
  @EmptySource
  void shouldNotThrowOnUnknown(String code) {
    val mcd = assertDoesNotThrow(() -> MedicationCategoryDto.fromCode(code));
    assertEquals(MedicationCategoryDto.C_03, mcd);
  }

  @Test
  void shouldHaveAllMedicationCategories() {
    Arrays.stream(MedicationCategory.values())
        .forEach(
            vs -> {
              val dto = assertDoesNotThrow(() -> MedicationCategoryDto.fromCode(vs.getCode()));
              assertEquals(vs.getCode(), dto.getCode());
            });
  }

  @Test
  void shouldNotHaveAnyExtraMedicationCategory() {
    Arrays.stream(MedicationCategoryDto.values())
        .filter(
            dto ->
                dto
                    != MedicationCategoryDto
                        .C_03) // Note: the default currently not available in MedicationCategory
        .forEach(
            dto -> {
              val vs = assertDoesNotThrow(() -> MedicationCategory.fromCode(dto.getCode()));
              assertEquals(vs.getCode(), dto.getCode());
            });
  }
}
