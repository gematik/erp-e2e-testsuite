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

package de.gematik.test.erezept.screenplay.strategy;

import static de.gematik.test.erezept.screenplay.strategy.DequeStrategy.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.exceptions.InvalidStrategyMappingException;
import de.gematik.test.erezept.screenplay.util.ManagedList;
import java.util.Comparator;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class DequeStrategyTest {

  @Test
  void testFromStringLIFO() {
    List<String> testList = List.of("letzte", "Letztes", "Jüngstes", "LETZTEN");
    for (String t : testList) {
      assertEquals(LIFO, fromString(t));
    }
  }

  @Test
  void testFromStringFIFO() {
    val testList = List.of("ERSTE", "Ersten", "ERSTES", "Ältestes");
    testList.forEach(t -> assertEquals(FIFO, fromString(t)));
  }

  @Test
  void failTestFromString() {
    assertThrows(InvalidStrategyMappingException.class, () -> fromString("vorderste"));
    assertThrows(InvalidStrategyMappingException.class, () -> fromString("hinterste"));
  }

  @Test
  void failOnDequeFromEmptyList() {
    val testList = List.of();
    val strategies = List.of(FIFO, LIFO);
    strategies.forEach(
        d -> assertThrows(MissingPreconditionError.class, () -> d.chooseFrom(testList)));
  }

  @Test
  void shouldRemoveFromManagedList() {
    val managed = new ManagedList<>(() -> "empty");
    managed.append("first");
    managed.append("second");

    assertEquals(2, managed.getRawList().size());
    FIFO.removeFrom(managed);
    assertEquals(1, managed.getRawList().size());
    LIFO.removeFrom(managed);
    assertTrue(managed.getRawList().isEmpty());
  }

  @Test
  void failOnRemoveFromEmptyList() {
    val managed = new ManagedList<>(() -> "empty");
    val strategies = List.of(FIFO, LIFO);
    strategies.forEach(
        d -> assertThrows(MissingPreconditionError.class, () -> d.removeFrom(managed)));
  }

  @Test
  void shouldChooseLIFOFromManagedList() {
    val ml = new ManagedList<String>(() -> "empty");
    ml.append("first");
    ml.append("second");
    val chosen = LIFO.chooseFrom(ml);
    assertEquals("second", chosen);
  }

  @Test
  void shouldChooseFIFOFromManagedList() {
    val ml = new ManagedList<String>(() -> "empty");
    ml.append("first");
    ml.append("second");
    val chosen = FIFO.chooseFrom(ml);
    assertEquals("first", chosen);
  }

  @Test
  void shouldChooseFromSorted() {
    val ml = List.of("first", "second");
    val fifoChosen = FIFO.chooseFrom(ml, Comparator.naturalOrder());
    val lifoChosen = LIFO.chooseFrom(ml, Comparator.naturalOrder());
    assertEquals("second", fifoChosen); // with reversed order
    assertEquals("first", lifoChosen); // with reversed order
  }
}
