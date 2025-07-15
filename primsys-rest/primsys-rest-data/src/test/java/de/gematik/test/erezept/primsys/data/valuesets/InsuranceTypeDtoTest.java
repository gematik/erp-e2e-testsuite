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

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.primsys.exceptions.InvalidCodeValueException;
import java.util.Arrays;
import lombok.val;
import org.junit.jupiter.api.Test;

class InsuranceTypeDtoTest {

  @Test
  void shouldBuildFromCode() {
    val value1 = InsuranceTypeDto.fromCode("GKV");
    val value2 = InsuranceTypeDto.fromCode("gkv");
    assertEquals(InsuranceTypeDto.GKV, value1);
    assertEquals(value1, value2);
  }

  @Test
  void shouldThrowOnInvalidCode() {
    assertThrows(InvalidCodeValueException.class, () -> InsuranceTypeDto.fromCode("abc"));
  }

  @Test
  void shouldHaveAllInsuranceTypes() {
    Arrays.stream(InsuranceTypeDe.values())
        .forEach(
            vs -> {
              val dto = assertDoesNotThrow(() -> InsuranceTypeDto.fromCode(vs.getCode()));
              assertEquals(vs.getCode(), dto.getCode());
              assertEquals(vs.getDisplay(), dto.getDisplay());
            });
  }

  @Test
  void shouldNotHaveAnyExtraInsuranceTypes() {
    Arrays.stream(InsuranceTypeDto.values())
        .forEach(
            dto -> {
              val vs = assertDoesNotThrow(() -> InsuranceTypeDe.fromCode(dto.getCode()));
              assertEquals(vs.getCode(), dto.getCode());
              assertEquals(vs.getDisplay(), dto.getDisplay());
            });
  }
}
