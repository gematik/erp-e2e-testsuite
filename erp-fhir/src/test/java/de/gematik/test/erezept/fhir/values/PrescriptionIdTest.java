/*
 * Copyright (c) 2022 gematik GmbH
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

import static org.junit.Assert.*;

import lombok.val;
import org.junit.Test;

public class PrescriptionIdTest {

  @Test
  public void checkValidPrescriptionId() {
    val id = "160.000.000.000.123.76";
    assertTrue(PrescriptionId.checkId(id));
  }

  @Test
  public void checkInvalidPrescriptionId() {
    val id = "160.000.000.000.123.77";
    assertFalse(PrescriptionId.checkId(id));
  }

  @Test
  public void checkRandomPrescriptionId() {
    for (int i = 0; i < 100; i++) {
      // just run a few iterations to really make sure random IDs are okay!
      val r = PrescriptionId.random();
      assertTrue(PrescriptionId.checkId(r));
    }
  }
}
