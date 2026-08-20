/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.fhir.valuesets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ReadyForCollectingTest {

  @ParameterizedTest
  @CsvSource({
    "IMMEDIATELY, immediately",
    "SAME_DAY, sameDay",
    "NEXT_DAY, nextDay",
    "NEXT_DAY_AM, nextDayAM",
    "NEXT_DAY_PM, nextDayPM",
    "UNKNOWN, unknown",
    "NOT_AVAILABLE, notAvailable"
  })
  void shouldHaveExpectedCodes(ReadyForCollecting readyForCollecting, String expectedCode) {
    assertEquals(expectedCode, readyForCollecting.getCode());
  }

  @Test
  void shouldParseFromCode() {
    val values = Arrays.asList(ReadyForCollecting.values());
    values.forEach(v -> assertEquals(v, ReadyForCollecting.fromCode(v.getCode())));
  }

  @Test
  void shouldThrowOnInvalidCode() {
    assertThrows(IllegalArgumentException.class, () -> ReadyForCollecting.fromCode("invalid"));
  }

  @Test
  void codeAndDisplayShouldWorkCorrect() {
    assertEquals("Immediately", ReadyForCollecting.IMMEDIATELY.getDisplay());
    assertEquals("Not available", ReadyForCollecting.NOT_AVAILABLE.getDisplay());
  }
}
