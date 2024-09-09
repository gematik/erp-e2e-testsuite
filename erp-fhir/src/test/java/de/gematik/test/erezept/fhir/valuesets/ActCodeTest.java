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

package de.gematik.test.erezept.fhir.valuesets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class ActCodeTest {

  @Test
  void shouldParseValidActCodeFromCode() {
    val codes = List.of("OPTIN", "OPTINR", "OPTOUT", "OPTOUTE");
    val expectedTypes = List.of(ActCode.OPTIN, ActCode.OPTINR, ActCode.OPTOUT, ActCode.OPTOUTE);

    for (var i = 0; i < codes.size(); i++) {
      val actual = ActCode.fromCode(codes.get(i));
      val expected = expectedTypes.get(i);
      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldThrowExceptionOnInvalidActCodeCodes() {
    val codes = List.of("OPT", "ABCD", "OUT");
    codes.forEach(
        code -> assertThrows(InvalidValueSetException.class, () -> ActCode.fromCode(code)));
  }
}
