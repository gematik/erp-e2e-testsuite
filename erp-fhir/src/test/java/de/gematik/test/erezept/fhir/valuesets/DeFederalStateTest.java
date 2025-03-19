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
 */

package de.gematik.test.erezept.fhir.valuesets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class DeFederalStateTest {

  @Test
  void shouldParseValidFederalStateFromCode() {
    val codes = List.of("DE-BW", "DE-BY", "DE-BE", "DE-BB");
    val expectedTypes =
        List.of(
            DeFederalState.DE_BW, DeFederalState.DE_BY, DeFederalState.DE_BE, DeFederalState.DE_BB);

    for (var i = 0; i < codes.size(); i++) {
      val actual = DeFederalState.fromCode(codes.get(i));
      val expected = expectedTypes.get(i);
      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldThrowExceptionOnInvalidFederalStateCodes() {
    val codes = List.of("BW", "ABCD", "DE-BAY");
    codes.forEach(
        code -> assertThrows(InvalidValueSetException.class, () -> DeFederalState.fromCode(code)));
  }

  @Test
  void shouldParseValidFederalStateFromDisplay() {
    val displayValues =
        List.of("Baden-Württemberg", "Hamburg", "Sachsen", "Sachsen-Anhalt", "Niedersachsen");
    val expectedTypes =
        List.of(
            DeFederalState.DE_BW,
            DeFederalState.DE_HH,
            DeFederalState.DE_SN,
            DeFederalState.DE_ST,
            DeFederalState.DE_NI);

    for (var i = 0; i < displayValues.size(); i++) {
      val actual = DeFederalState.fromDisplay(displayValues.get(i));
      val expected = expectedTypes.get(i);
      assertEquals(expected, actual);
    }
  }

  @Test
  void shouldThrowExceptionOnInvalidFederalStateDisplayValues() {
    val codes = List.of("Badenwürttemberg", "Mallorca", "ABC", "");
    codes.forEach(
        code -> assertThrows(InvalidValueSetException.class, () -> DeFederalState.fromCode(code)));
  }
}
