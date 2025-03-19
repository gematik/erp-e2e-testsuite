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

package de.gematik.test.erezept.screenplay.util;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import lombok.val;
import org.junit.Test;

public class ManagedListTest {

  @Test
  public void shouldDetectEmptyManagedList() {
    val ml = new ManagedList<String>(() -> "nothing here yet");
    assertTrue(ml.isEmpty());
    assertEquals(0, ml.getRawList().size());
  }

  @Test
  public void shouldDetectNonEmptyManagedList() {
    val ml = new ManagedList<String>(() -> "nothing here yet");
    ml.append("Hello World");
    assertFalse(ml.isEmpty());
    assertEquals(1, ml.getRawList().size());
  }

  @Test
  public void shouldGetLastFirstWithoutConsumeFromManagedList() {
    val ml = new ManagedList<String>(() -> "nothing here yet");
    ml.append("first");
    ml.append("last");

    assertEquals("first", ml.getFirst());
    assertEquals("first", ml.getFirst()); // try a second time to ensure, element was not consumed
    assertEquals("last", ml.getLast());
    assertEquals("last", ml.getLast());
    assertFalse(ml.isEmpty());
    assertEquals(2, ml.getRawList().size());
  }

  @Test
  public void shouldConsumeFirstLastFromManagedList() {
    val ml = new ManagedList<String>(() -> "nothing here yet");
    ml.append("first");
    ml.append("last");

    assertEquals("first", ml.consumeFirst());
    assertEquals(1, ml.getRawList().size());

    assertEquals("last", ml.consumeLast());
    assertTrue(ml.isEmpty());
  }

  @Test
  public void shouldConsumeLastFirstFromManagedList() {
    val ml = new ManagedList<String>(() -> "nothing here yet");
    ml.append("first");
    ml.append("last");

    assertEquals("last", ml.consumeLast());
    assertEquals(1, ml.getRawList().size());

    assertEquals("first", ml.consumeFirst());
    assertTrue(ml.isEmpty());
  }

  @Test
  public void shouldPrependOnManagedList() {
    val ml = new ManagedList<String>(() -> "nothing here yet");
    ml.append("last");
    ml.prepend("first");

    assertEquals("first", ml.getFirst());
    assertEquals("last", ml.getLast());
  }

  @Test
  public void shouldThrowOnEmptyManagedList() {
    val ml = new ManagedList<String>(() -> "nothing here yet");
    assertTrue(ml.isEmpty());
    assertThrows(MissingPreconditionError.class, ml::getFirst);
    assertThrows(MissingPreconditionError.class, ml::getLast);
  }
}
