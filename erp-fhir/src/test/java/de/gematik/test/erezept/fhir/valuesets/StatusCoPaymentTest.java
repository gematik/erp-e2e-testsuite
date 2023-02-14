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

package de.gematik.test.erezept.fhir.valuesets;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.exceptions.*;
import java.util.*;
import lombok.*;
import org.junit.jupiter.api.*;

class StatusCoPaymentTest {
  @Test
  void shouldParseValidStatusCoPaymentFromCode() {
    val codes = List.of("0", "1", "2");
    val expectedTypes =
        List.of(StatusCoPayment.STATUS_0, StatusCoPayment.STATUS_1, StatusCoPayment.STATUS_2);

    for (var i = 0; i < codes.size(); i++) {
      val actual = StatusCoPayment.fromCode(codes.get(i));
      val expected = expectedTypes.get(i);
      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldThrowExceptionOnInvalidStatusCoPaymentCodes() {
    val codes = List.of("00", "12", "3", "ABCD", "");
    codes.forEach(
        code -> assertThrows(InvalidValueSetException.class, () -> StatusCoPayment.fromCode(code)));
  }
}
