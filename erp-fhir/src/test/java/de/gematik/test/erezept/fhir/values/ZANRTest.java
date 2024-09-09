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

package de.gematik.test.erezept.fhir.values;

import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ZANRTest {

  @Test
  void testFakerZanrLength() {
    String testNum = ZANR.random().getValue();
    assertEquals(9, testNum.length());
  }

  @Test
  void testLastToDigits() {
    String testNum = ZANR.random().toString().substring(6, 15);
    String docCategori = testNum.substring(7, 9);
    boolean testresult = docCategori.equals("50") | docCategori.equals("91");
    assertTrue(testresult);
  }

  @Test
  void testRandomFakerZanrRealisticEnd() {
    String testLanr = LANR.random().getValue();
    assertDoesNotThrow(() -> Integer.parseInt(testLanr));
  }

  @ParameterizedTest(name = "[{index}]: ZANR {0} is valid")
  @ValueSource(
      strings = {"444444401", "444444499", "999999900", "555555560", "000000000", "999999991"})
  void additionalNumbersShouldBeValid(String value) {
    val zanr = new ZANR(value);
    assertTrue(zanr.isValid());
  }
}
