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

package de.gematik.test.smartcard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.test.smartcard.exceptions.InvalidSmartcardTypeException;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

class SmartcardTypeTest {

  @Test
  void shouldGetSmartcardTypeFromString() {
    val inputs =
        Map.of(
            "egk",
            SmartcardType.EGK,
            "hba",
            SmartcardType.HBA,
            "smcb",
            SmartcardType.SMC_B,
            "smc-b",
            SmartcardType.SMC_B);

    inputs.forEach((k, v) -> assertEquals(v, SmartcardType.fromString(k)));
  }

  @Test
  void shouldThrowOnInvalidSmartcardType() {
    val inputs = List.of("smc-a", "SMC-D", "egk2");
    inputs.forEach(
        input ->
            assertThrows(
                InvalidSmartcardTypeException.class, () -> SmartcardType.fromString("SMC-D")));
  }
}
