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

package de.gematik.test.erezept.cli.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.*;
import org.junit.jupiter.api.*;

class NameWrapperTest {

  @Test
  void shouldGenerateRandomName() {
    val nw = NameWrapper.randomName();

    assertNotNull(nw.getFirstName());
    assertFalse(nw.getFirstName().isBlank());
    assertFalse(nw.getFirstName().isEmpty());

    assertNotNull(nw.getLastName());
    assertFalse(nw.getLastName().isBlank());
    assertFalse(nw.getLastName().isEmpty());
  }

  @Test
  void shouldGenerateRandomNameOnEmpty() {
    val nw = NameWrapper.fromFullName("");

    assertNotNull(nw.getFirstName());
    assertFalse(nw.getFirstName().isBlank());
    assertFalse(nw.getFirstName().isEmpty());

    assertNotNull(nw.getLastName());
    assertFalse(nw.getLastName().isBlank());
    assertFalse(nw.getLastName().isEmpty());
  }

  @Test
  void shouldGenerateRandomNameOnNull() {
    val nw = NameWrapper.fromFullName(null);

    assertNotNull(nw.getFirstName());
    assertFalse(nw.getFirstName().isBlank());
    assertFalse(nw.getFirstName().isEmpty());

    assertNotNull(nw.getLastName());
    assertFalse(nw.getLastName().isBlank());
    assertFalse(nw.getLastName().isEmpty());
  }

  @Test
  void shouldGenerateLastName() {
    val nw = NameWrapper.fromFullName("Bernd");

    assertEquals("Bernd", nw.getFirstName());

    assertNotNull(nw.getLastName());
    assertFalse(nw.getLastName().isBlank());
    assertFalse(nw.getLastName().isEmpty());
  }

  @Test
  void shouldSplitFullName() {
    val nw = NameWrapper.fromFullName("Bernd Claudius");

    assertEquals("Bernd", nw.getFirstName());
    assertEquals("Claudius", nw.getLastName());
  }
}
