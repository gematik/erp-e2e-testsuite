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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class MvoDataTest {

  @Test
  void isValidWithNull() {
    MvoData mvoData = new MvoData();
    assertFalse(mvoData.isValid());
  }

  @ParameterizedTest
  @CsvSource({"100", "-100", "-1", "0", "100000000", "-1100000000"})
  void noValidNumerator(int num) {
    MvoData mvoData = new MvoData();
    mvoData.setNumerator(num);
    assertFalse(mvoData.isValid());
  }

  @ParameterizedTest
  @CsvSource({"100", "-100", "-5", "5", "100000000", "-1100000000"})
  void noValidDemoniator(int num) {
    MvoData mvoData = new MvoData();
    mvoData.setNumerator(num);
    assertFalse(mvoData.isValid());
  }

  @ParameterizedTest
  @CsvSource({"0,3", "1,1", "2,1 ", "3,2", "4,3", "1,0", "100000000, -1100000000", "4,5", "5,4"})
  void noValidNomiDemoniatorCombination(int num, int denom) {
    MvoData mvoData = new MvoData();
    mvoData.setNumerator(num);
    mvoData.setDenominator(denom);
    assertFalse(mvoData.isValid());
  }

  @ParameterizedTest
  @CsvSource({
    "2,2", "1,2", "1,4", "1,3", "2,4 ", "4,4", "3,4", "2,3",
  })
  void validNumDenominatorCombination(int num, int denom) {
    MvoData mvoData = new MvoData();
    mvoData.setNumerator(num);
    mvoData.setDenominator(denom);
    assertTrue(mvoData.isValid());
  }

  public static Stream<Arguments> fakeMvoData() {
    return Stream.of(
        Arguments.of((Predicate<MvoData>) mvoData -> mvoData.getNumerator() > 4),
        Arguments.of((Predicate<MvoData>) mvoData -> mvoData.getNumerator() < 1),
        Arguments.of((Predicate<MvoData>) mvoData -> mvoData.getDenominator() < 2),
        Arguments.of(
            (Predicate<MvoData>) mvoData -> mvoData.getDenominator() < mvoData.getNumerator()),
        Arguments.of((Predicate<MvoData>) mvoData -> mvoData.getDenominator() > 4));
  }

  @ParameterizedTest
  @MethodSource
  void fakeMvoData(Predicate<MvoData> condition) {
    MvoData mvoData = new MvoData();
    mvoData.fakeMvoNumeAndDenomi();
    assertFalse(condition.test(mvoData));
  }
}
