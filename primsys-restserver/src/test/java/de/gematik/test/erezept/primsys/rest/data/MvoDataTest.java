/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.primsys.rest.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class MvoDataTest {

  public static Stream<Arguments> fakeMvoData() {
    return Stream.of(
        Arguments.of((Predicate<MvoData>) mvoData -> mvoData.getNumerator() > 4),
        Arguments.of((Predicate<MvoData>) mvoData -> mvoData.getNumerator() < 1),
        Arguments.of((Predicate<MvoData>) mvoData -> mvoData.getDenominator() < 2),
        Arguments.of(
            (Predicate<MvoData>) mvoData -> mvoData.getDenominator() < mvoData.getNumerator()),
        Arguments.of((Predicate<MvoData>) mvoData -> mvoData.getDenominator() > 4));
  }

  @Test
  void isNotValidWithNull() {
    MvoData mvoData = new MvoData();
    assertFalse(mvoData.isValid());
  }

  @Test
  void isNotValidWithNullNum() {
    MvoData mvoData = new MvoData();
    mvoData.setDenominator(3);
    mvoData.setStartDate(LocalDate.now());
    assertFalse(mvoData.isValid());
  }

  @Test
  void isNotValidWithNullDenom() {
    MvoData mvoData = new MvoData();
    mvoData.setNumerator(2);
    mvoData.setStartDate(LocalDate.now());
    assertFalse(mvoData.isValid());
  }

  @Test
  void isNotValidWithNullStartDate() {
    MvoData mvoData = new MvoData();
    mvoData.setNumerator(1);
    mvoData.setDenominator(3);
    assertFalse(mvoData.isValid());
  }

  @ParameterizedTest
  @CsvSource({"100", "-100", "-1", "0", "100000000", "-1100000000"})
  void noValidNumerator(int num) {
    var mvoWithDates = new MvoData();
    mvoWithDates.setStartDate(LocalDate.now());
    mvoWithDates.setEndDate(LocalDate.now().plusDays(28));
    mvoWithDates.setNumerator(num);
    assertFalse(mvoWithDates.isValid());
  }

  @ParameterizedTest
  @CsvSource({"100", "-100", "-5", "5", "100000000", "-1100000000"})
  void noValidDemoniator(int num) {
    var mvoWithDates = new MvoData();
    mvoWithDates.setStartDate(LocalDate.now());
    mvoWithDates.setEndDate(LocalDate.now().plusDays(28));
    mvoWithDates.setNumerator(num);
    assertFalse(mvoWithDates.isValid());
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
  void noValidNomiDemoniatorCombination(int num, int denom) {
    var mvoWithDates = new MvoData();
    mvoWithDates.setStartDate(LocalDate.now());
    mvoWithDates.setEndDate(LocalDate.now().plusDays(28));
    mvoWithDates.setNumerator(num);
    mvoWithDates.setDenominator(denom);
    assertFalse(mvoWithDates.isValid());
  }

  @ParameterizedTest
  @CsvSource({
    "2,2", "1,2", "1,4", "1,3", "2,4 ", "4,4", "3,4", "2,3",
  })
  void validNumDenominatorCombination(int num, int denom) {
    var mvoWithDates = new MvoData();
    mvoWithDates.setStartDate(LocalDate.now());
    mvoWithDates.setEndDate(LocalDate.now().plusDays(28));
    mvoWithDates.setNumerator(num);
    mvoWithDates.setDenominator(denom);
    assertTrue(mvoWithDates.isValid());
  }

  @ParameterizedTest
  @MethodSource
  void fakeMvoData(Predicate<MvoData> condition) {
    MvoData mvoData = new MvoData();
    mvoData.fakeMvoNumeDenomAndDates();
    assertFalse(condition.test(mvoData));
  }

  @Test
  void hasNoEndDat() {
    var mvoWithStartDates = new MvoData();
    mvoWithStartDates.setStartDate(LocalDate.now());
    assertFalse(mvoWithStartDates.hasEndDate());
    mvoWithStartDates.setEndDate(LocalDate.now().plusDays(5));
    assertTrue(mvoWithStartDates.hasEndDate());
  }

  @SneakyThrows
  @Test
  void checkCorrectTranslationOfMvoDataJasonMapperFullFiled() {
    MvoData mvoData = new MvoData();
    mvoData.setNumerator(3);
    mvoData.setDenominator(2);
    mvoData.setStartDate(LocalDate.now());
    mvoData.setEndDate(LocalDate.now().plusDays(300));

    val mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    val mvoDataJson = mapper.writeValueAsString(mvoData);
    val mvoData2 = mapper.readValue(mvoDataJson, MvoData.class);

    assertEquals(mvoData.getNumerator(), mvoData2.getNumerator());
    assertEquals(mvoData.getDenominator(), mvoData2.getDenominator());

    assertEquals(mvoData.getStartDate(), mvoData2.getStartDate());
    assertEquals(mvoData.getEndDate(), mvoData2.getEndDate());
  }

  @SneakyThrows
  @Test
  void checkCorrectTranslationOfMvoDataJasonMapperNoEndDate() {
    MvoData mvoData = new MvoData();
    mvoData.setNumerator(3);
    mvoData.setDenominator(2);
    mvoData.setStartDate(LocalDate.now());

    val mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    val mvoDataJson = mapper.writeValueAsString(mvoData);
    val mvoData2 = mapper.readValue(mvoDataJson, MvoData.class);

    assertEquals(mvoData.getNumerator(), mvoData2.getNumerator());
    assertEquals(mvoData.getDenominator(), mvoData2.getDenominator());

    assertEquals(mvoData.getStartDate(), mvoData2.getStartDate());
    assertEquals(mvoData.getEndDate(), mvoData2.getEndDate());
  }
}
