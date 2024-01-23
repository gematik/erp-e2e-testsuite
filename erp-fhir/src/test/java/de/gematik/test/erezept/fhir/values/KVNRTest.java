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

package de.gematik.test.erezept.fhir.values;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class KVNRTest {

  @ParameterizedTest(name = "[{index}]: KVNR {0} is structurally invalid")
  @NullSource
  @EmptySource
  @ValueSource(
      strings = {
        "k220645129",
        "ö220645129",
        "Ä220645129",
        "1220645129",
        "A2206451290",
        "B22064512",
        "123"
      })
  void shouldCheckInvalidKvnrFormat(String value) {
    val kvnr = KVNR.from(value);
    assertFalse(kvnr.isValid());
  }

  @ParameterizedTest(name = "[{index}]: KVNR {0} has a invalid check number")
  @ValueSource(strings = {"K220645129", "T012345679", "A005000112", "C000500020"})
  void shouldCheckInvalidKvnrCheckDigit(String value) {
    val kvnr = KVNR.from(value);
    assertFalse(kvnr.isValid());
  }

  @ParameterizedTest(name = "[{index}]: KVNR {0} is valid")
  @ValueSource(strings = {"A000500015", "K220645122", "T012345678", "A000500015", "C000500021"})
  void shouldCheckValidKvnr(String value) {
    val kvnr = KVNR.from(value);
    assertTrue(kvnr.isValid());
  }

  @RepeatedTest(5)
  void shouldGenerateRandomValidKvid() {
    val kvnr = KVNR.random();
    assertTrue(kvnr.isValid());
  }
}
