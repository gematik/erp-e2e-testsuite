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
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.test.erezept.primsys.exceptions.InvalidCodeValueException;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class PatientInsuranceTypeDtoTest {

  @ParameterizedTest
  @ValueSource(strings = {"GKV", "PKV"})
  void shouldCreateFromCode(String code) {
    val pit = assertDoesNotThrow(() -> PatientInsuranceTypeDto.fromCode(code));
    assertEquals(code, pit.getCode());
  }

  @ParameterizedTest
  @ValueSource(strings = {"BG", "UK", "SEL", "xyz"})
  @NullSource
  @EmptySource
  void shouldThrowOnInvalidCode(String code) {
    assertThrows(InvalidCodeValueException.class, () -> PatientInsuranceTypeDto.fromCode(code));
  }
}
