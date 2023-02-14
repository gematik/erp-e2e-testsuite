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

class QualificationTypeTest {

  @Test
  void shouldParseFromCode() {
    val testdata =
        Map.of(
            "00", QualificationType.DOCTOR,
            "01", QualificationType.DENTIST,
            "02", QualificationType.MIDWIFE,
            "03", QualificationType.DOCTOR_IN_TRAINING,
            "04", QualificationType.DOCTOR_AS_REPLACEMENT);

    testdata.forEach((code, expected) -> assertEquals(expected, QualificationType.fromCode(code)));
  }

  @Test
  void shouldThrowOnInvalidCode() {
    val codes = List.of("05", "0", "1", "");
    codes.forEach(
        code ->
            assertThrows(InvalidValueSetException.class, () -> QualificationType.fromCode(code)));
  }

  @Test
  void shouldParseFromValidDisplayValues() {
    val testdata =
        Map.of(
            "Arzt", QualificationType.DOCTOR,
            "arzt", QualificationType.DOCTOR,
            "Zahnarzt", QualificationType.DENTIST,
            "zahnarzt", QualificationType.DENTIST,
            "Hebamme", QualificationType.MIDWIFE,
            "hebamme", QualificationType.MIDWIFE,
            "Arzt in Weiterbildung", QualificationType.DOCTOR_IN_TRAINING,
            "arzt in weiterbildung", QualificationType.DOCTOR_IN_TRAINING,
            "Arzt als Vertreter", QualificationType.DOCTOR_AS_REPLACEMENT,
            "arzt als vertreter", QualificationType.DOCTOR_AS_REPLACEMENT);

    testdata.forEach(
        (display, expected) -> assertEquals(expected, QualificationType.fromDisplay(display)));
  }

  @Test
  void shouldThrowOnInvalidDisplayValues() {
    val displays =
        List.of(
            "arz",
            "artz",
            "Zahnarz",
            "Hebame",
            "Arzt in Fortbildung",
            "Weiterbildung",
            "Vertreter");

    displays.forEach(
        display ->
            assertThrows(
                InvalidValueSetException.class, () -> QualificationType.fromDisplay(display)));
  }
}
