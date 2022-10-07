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

package de.gematik.test.erezept.fhir.valuesets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

class PerformerTypeTest {

  @Test
  void fromValidCodes() {
    val validCodes =
        Map.of(
            "urn:oid:1.2.276.0.76.4.32",
            PerformerType.PHARMACIST,
            "urn:oid:1.2.276.0.76.4.54",
            PerformerType.PUBLIC_PHARMACY);

    validCodes.forEach(
        (code, expected) -> {
          val actual = PerformerType.fromCode(code);
          assertEquals(expected, actual);
        });
  }

  @Test
  void fromInvalidCode() {
    assertThrows(InvalidValueSetException.class, () -> PerformerType.fromCode("invalid"));
  }

  @Test
  void fromDisplays() {
    val validDisplays =
        Map.of(
            "Apotheke",
            PerformerType.PHARMACIST,
            "Öffentliche Apotheke",
            PerformerType.PUBLIC_PHARMACY);

    validDisplays.forEach(
        (code, expected) -> {
          val actual = PerformerType.fromDisplay(code);
          assertEquals(expected, actual);
        });
  }

  @Test
  void fromInvalidDisplay() {
    assertThrows(InvalidValueSetException.class, () -> PerformerType.fromDisplay("Zahnarzt"));
  }
}
