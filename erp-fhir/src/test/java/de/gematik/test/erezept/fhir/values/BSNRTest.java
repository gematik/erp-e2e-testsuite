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

package de.gematik.test.erezept.fhir.values;

import static de.gematik.test.erezept.fhir.valuesets.Wop.fromCode;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.valuesets.Wop;
import org.junit.jupiter.api.Test;

class BSNRTest {

  @Test
  void testRandomBsnrKeyNumber() {
    String testNo = BSNR.random().toString().substring(6, 15);
    String jobKey = testNo.substring(0, 2);
    // "fromCode()" throws Exception if jobKey is invalid for Wop
    fromCode(jobKey);
    assertNotEquals(jobKey, Wop.DUMMY.getCode());
  }

  @Test
  void testRandomBsnrRealisticLength() {
    String testNum = String.valueOf(BSNR.random());
    assertEquals(15, testNum.length());
  }

  @Test
  void testRandomBsnrRealisticBegin() {
    String testNum = BSNR.random().toString().substring(0, 5);
    assertEquals("BSNR:", testNum);
  }

  @Test
  void testRandomFakerBsnrAsInteger() {
    String testLanr = BSNR.random().getValue();
    assertDoesNotThrow(() -> Integer.parseInt(testLanr));
  }
}
