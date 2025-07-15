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

package de.gematik.test.erezept.fhir.valuesets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class QualificationTypeTest {

  @ParameterizedTest
  @MethodSource
  void shouldParseFromCode(String code, QualificationType expected) {
    assertEquals(expected, QualificationType.from(code));
  }

  static Stream<Arguments> shouldParseFromCode() {
    return Map.of(
            "00", QualificationType.DOCTOR,
            "01", QualificationType.DENTIST,
            "02", QualificationType.MIDWIFE,
            "03", QualificationType.DOCTOR_IN_TRAINING,
            "04", QualificationType.DOCTOR_AS_REPLACEMENT)
        .entrySet()
        .stream()
        .map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
  }

  @ParameterizedTest
  @ValueSource(strings = {"05", "0", "1"})
  @EmptySource
  @NullSource
  void shouldThrowOnInvalidCode(String code) {
    assertThrows(InvalidValueSetException.class, () -> QualificationType.from(code));
  }

  @ParameterizedTest
  @MethodSource
  void shouldParseFromValidDisplayValues(String display, QualificationType expected) {
    assertEquals(expected, QualificationType.fromDisplay(display));
  }

  static Stream<Arguments> shouldParseFromValidDisplayValues() {
    return Map.of(
            "Arzt", QualificationType.DOCTOR,
            "arzt", QualificationType.DOCTOR,
            "Zahnarzt", QualificationType.DENTIST,
            "zahnarzt", QualificationType.DENTIST,
            "Hebamme", QualificationType.MIDWIFE,
            "hebamme", QualificationType.MIDWIFE,
            "Arzt in Weiterbildung", QualificationType.DOCTOR_IN_TRAINING,
            "arzt in weiterbildung", QualificationType.DOCTOR_IN_TRAINING,
            "Arzt als Vertreter", QualificationType.DOCTOR_AS_REPLACEMENT,
            "arzt als vertreter", QualificationType.DOCTOR_AS_REPLACEMENT)
        .entrySet()
        .stream()
        .map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
  }

  @ParameterizedTest
  @MethodSource
  void shouldThrowOnInvalidDisplayValues(String display) {
    assertThrows(InvalidValueSetException.class, () -> QualificationType.fromDisplay(display));
  }

  static Stream<Arguments> shouldThrowOnInvalidDisplayValues() {
    return Stream.of(
            "arz", "artz", "Zahnarz", "Hebame", "Arzt in Fortbildung", "Weiterbildung", "Vertreter")
        .map(Arguments::of);
  }
}
