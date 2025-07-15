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

package de.gematik.test.erezept.primsys.mapping;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.primsys.data.MvoDto;
import java.util.Date;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MvoDataMapperTest extends ErpFhirBuildingTest {

  private static final int REPETITIONS = 5;

  @RepeatedTest(value = REPETITIONS)
  void shouldGenerateValidRandomMvo() {
    val mvo = MvoDataMapper.random();
    assertTrue(mvo.isValid());

    val mvoExtension = mvo.convert();
    assertTrue(mvoExtension.isMultiple());
  }

  @RepeatedTest(value = REPETITIONS)
  void shouldGenerateValidRandomMvoWithoutEndDate() {
    val mvo = MvoDataMapper.random();
    mvo.dto.setEndDate(null);
    assertTrue(mvo.isValid());

    val mvoExtension = mvo.convert();
    assertTrue(mvoExtension.isMultiple());
  }

  @RepeatedTest(value = REPETITIONS)
  void shouldNotGenerateValidRandomMvoWithNullNumerator() {
    val mvo = MvoDataMapper.random();
    mvo.dto.setNumerator(null);
    assertFalse(mvo.isValid());
  }

  @RepeatedTest(value = REPETITIONS)
  void shouldNotGenerateValidRandomMvoWithNullDenominator() {
    val mvo = MvoDataMapper.random();
    mvo.dto.setDenominator(null);
    assertFalse(mvo.isValid());
  }

  @RepeatedTest(value = REPETITIONS)
  void shouldNotGenerateValidRandomMvoWithNullStartDate() {
    val mvo = MvoDataMapper.random();
    mvo.dto.setStartDate(null);
    assertFalse(mvo.isValid());
  }

  @RepeatedTest(value = REPETITIONS)
  void shouldAddValidNumerator() {
    val dto = new MvoDto();
    dto.setDenominator(3);
    dto.setStartDate(new Date());
    val mvo = MvoDataMapper.from(dto);
    assertTrue(mvo.isValid());
  }

  @RepeatedTest(value = REPETITIONS)
  void shouldAddValidDenominator() {
    val dto = new MvoDto();
    dto.setNumerator(3);
    dto.setStartDate(new Date());
    val mvo = MvoDataMapper.from(dto);
    assertTrue(mvo.isValid());
  }

  @RepeatedTest(value = REPETITIONS)
  void shouldAddValidStartDate() {
    val dto = new MvoDto();
    dto.setNumerator(3);
    dto.setDenominator(4);
    val mvo = MvoDataMapper.from(dto);
    assertTrue(mvo.isValid());
    assertNotNull(mvo.dto.getStartDate());
  }

  @RepeatedTest(value = REPETITIONS)
  void shouldAddNotFakeEndDate() {
    val dto = new MvoDto();
    dto.setNumerator(3);
    dto.setDenominator(4);
    dto.setStartDate(new Date());
    val mvo = MvoDataMapper.from(dto);
    assertNull(mvo.dto.getEndDate());
  }

  @ParameterizedTest
  @CsvSource({"100", "-100", "-1", "0", "100000000", "-1100000000"})
  void shouldDetectInvalidNumerator(int num) {
    val dto = new MvoDto();
    dto.setNumerator(num);
    dto.setDenominator(4);
    dto.setStartDate(new Date());
    val mvo = MvoDataMapper.from(dto);
    assertFalse(mvo.isValid());
  }

  @ParameterizedTest
  @CsvSource({"100", "-100", "-5", "5", "100000000", "-1100000000"})
  void shouldDetectInvalidDenominator(int denom) {
    val dto = new MvoDto();
    dto.setNumerator(1);
    dto.setDenominator(denom);
    dto.setStartDate(new Date());
    val mvo = MvoDataMapper.from(dto);
    assertFalse(mvo.isValid());
  }

  @ParameterizedTest
  @CsvSource({
    "0,3",
    "1,1",
    "2,1 ",
    "3,2",
    "4,3",
    "1,0",
    "100000000, -1100000000",
    "4,5",
    "5,4",
    "5,5"
  })
  void shouldDetectInvalidRatio(int num, int denom) {
    val dto = new MvoDto();
    dto.setNumerator(num);
    dto.setDenominator(denom);
    dto.setStartDate(new Date());
    val mvo = MvoDataMapper.from(dto);
    assertFalse(mvo.isValid());
  }

  @ParameterizedTest
  @CsvSource({
    "2,2", "1,2", "1,4", "1,3", "2,4 ", "4,4", "3,4", "2,3",
  })
  void shouldDetectValidRatio(int num, int denom) {
    val dto = new MvoDto();
    dto.setNumerator(num);
    dto.setDenominator(denom);
    dto.setStartDate(new Date());
    val mvo = MvoDataMapper.from(dto);
    assertTrue(mvo.isValid());
  }
}
