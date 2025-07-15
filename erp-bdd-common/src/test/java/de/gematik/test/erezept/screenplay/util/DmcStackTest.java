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

package de.gematik.test.erezept.screenplay.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.test.erezept.exceptions.MissingStackException;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class DmcStackTest {

  @Test
  void shouldGetActiveStackFromString() {
    val values = List.of("Ausgestellte Verordnung", "Ausgestellt", "Verordnung ausgestellt");
    values.forEach(input -> assertEquals(DmcStack.ACTIVE, DmcStack.fromString(input)));
  }

  @Test
  void shouldGetDeletedStackFromString() {
    val values = List.of("Gelöschte Verordnung", "Gelöscht", "Verordnung gelöscht");
    values.forEach(input -> assertEquals(DmcStack.DELETED, DmcStack.fromString(input)));
  }

  @Test
  void shouldThrowOnInvalidStack() {
    val values = List.of("Verordnung", "Rezept", "DMC", "Data Matrix Code");
    values.forEach(
        input -> assertThrows(MissingStackException.class, () -> DmcStack.fromString(input)));
  }
}
