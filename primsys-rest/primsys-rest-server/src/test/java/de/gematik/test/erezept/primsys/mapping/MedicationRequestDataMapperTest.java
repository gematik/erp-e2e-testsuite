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

package de.gematik.test.erezept.primsys.mapping;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.primsys.data.MedicationRequestDto;
import lombok.val;
import org.junit.jupiter.api.Test;

class MedicationRequestDataMapperTest {

  @Test
  void shouldValidMvoOnNull() {
    val mapper = MedicationRequestDataMapper.from(new MedicationRequestDto()).forMedication(null);
    assertTrue(mapper.isMvoValid());
  }

  @Test
  void shouldValidMvo() {
    val mvoDto = MvoDataMapper.randomDto();
    val medReqDto = new MedicationRequestDto();
    medReqDto.setMvo(mvoDto);
    val mapper = MedicationRequestDataMapper.from(medReqDto).forMedication(null);
    assertTrue(mapper.isMvoValid());
  }

  @Test
  void shouldBeInvalidMvo() {
    val mvoDto = MvoDataMapper.randomDto();
    mvoDto.setNumerator(7);
    val medReqDto = new MedicationRequestDto();
    medReqDto.setMvo(mvoDto);
    val mapper = MedicationRequestDataMapper.from(medReqDto).forMedication(null);
    assertFalse(mapper.isMvoValid());
  }
}