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

package de.gematik.test.erezept.screenplay.task;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.exceptions.MissingNegativeBehaviourError;
import de.gematik.test.erezept.exceptions.TestcaseAbortedException;
import lombok.SneakyThrows;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NegateTest {

  @BeforeAll
  static void setUp() {
    OnStage.setTheStage(new OnlineCast());
  }

  @AfterAll
  static void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldDetectInvalidTaskNegation() {
    val actor = OnStage.theActor("Marty");

    val dummyTask = new ThrowingDummyTask(new TestcaseAbortedException("test"));
    val negatedTask = Negate.the(dummyTask).with(RuntimeException.class);
    assertThrows(RuntimeException.class, () -> negatedTask.performAs(actor));
  }

  @Test
  void shouldDetectInvalidTaskNegationWithCausingExceptions() {
    val actor = OnStage.theActor("Marty");

    val dummyTask =
        new ThrowingDummyTask(new AssertionError("test", new TestcaseAbortedException("abort")));
    val negatedTask = Negate.the(dummyTask).with(TestcaseAbortedException.class);
    // Negate will catch the AssertionError because of the cause; no throw expected
    assertDoesNotThrow(() -> negatedTask.performAs(actor));
  }

  @Test
  void shouldDetectNonThrowingTaskNegation() {
    val actor = OnStage.theActor("Emmet");

    val dummyTask = new NonThrowingDummyTask();
    val negatedTask = Negate.the(dummyTask).with(TestcaseAbortedException.class);
    assertThrows(MissingNegativeBehaviourError.class, () -> negatedTask.performAs(actor));
  }

  @Test
  void shouldDetectCorrectlyThrowingTaskNegation() {
    val actor = OnStage.theActor("Einstein");

    val dummyTask = new ThrowingDummyTask(new TestcaseAbortedException("test"));
    val negatedTask = Negate.the(dummyTask).with(TestcaseAbortedException.class);

    var hasThrown = false;
    try {
      negatedTask.performAs(actor);
    } catch (MissingNegativeBehaviourError mnbe) {
      hasThrown = true;
    } finally {
      assertFalse(hasThrown);
    }
  }

  private static class ThrowingDummyTask implements Task {

    private final Throwable exception;

    public ThrowingDummyTask(Throwable exception) {
      this.exception = exception;
    }

    @SneakyThrows
    @Override
    public <T extends Actor> void performAs(T t) {
      throw exception;
    }
  }

  private static class NonThrowingDummyTask implements Task {

    @Override
    public <T extends Actor> void performAs(T t) {
      // just pass
    }
  }
}
