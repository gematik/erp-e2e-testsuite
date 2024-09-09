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

import de.gematik.test.erezept.primsys.exceptions.InvalidCodeValueException;
import lombok.val;
import org.junit.jupiter.api.Test;

class MedicationTypeDtoTest {
  @Test
  void shouldDecodeFromCode() {
    val result = MedicationTypeDto.fromCode("PZN");
    val result2 = MedicationTypeDto.fromCode("INGREDIENT");
    assertEquals(MedicationTypeDto.PZN, result);
    assertEquals(MedicationTypeDto.INGREDIENT, result2);
  }

  @Test
  void shouldThrowOnInvalidCode() {
    assertThrows(InvalidCodeValueException.class, () -> MedicationTypeDto.fromCode("invalidCode"));
  }

  @Test
  void shouldThrowOnNullCode() {
    assertThrows(NullPointerException.class, () -> MedicationTypeDto.fromCode(null));
  }
}
