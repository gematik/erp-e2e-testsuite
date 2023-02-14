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

package de.gematik.test.erezept.fhir.values;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
}
