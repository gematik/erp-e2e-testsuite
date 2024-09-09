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

package de.gematik.test.erezept.screenplay.task;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.exceptions.MissingNegativeBehaviourError;
import de.gematik.test.erezept.exceptions.TestcaseAbortedException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ThatNotAllowedToAskTest {

  @BeforeAll
  static void setUp() {
    OnStage.setTheStage(Cast.ofStandardActors());
  }

  @AfterAll
  static void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldDetectInvalidQuestionNegation() {
    val actor = OnStage.theActor("Marty");

    val dummyQuestion = new ThrowingDummyQuestion(new TestcaseAbortedException("test"));
    val negatedQuestion = ThatNotAllowedToAsk.the(dummyQuestion).with(RuntimeException.class);
    assertThrows(RuntimeException.class, () -> negatedQuestion.performAs(actor));
  }

  @Test
  void shouldDetectInvalidQuestionNegationWithCausingExceptions() {
    val actor = OnStage.theActor("Marty");

    val dummyQuestion =
        new ThrowingDummyQuestion(
            new AssertionError("test", new TestcaseAbortedException("abort")));
    val negatedQuestion =
        ThatNotAllowedToAsk.the(dummyQuestion).with(TestcaseAbortedException.class);
    // Negate will catch the AssertionError because of the cause; no throw expected
    assertDoesNotThrow(() -> negatedQuestion.performAs(actor));
  }

  @Test
  void shouldDetectNonThrowingQuestionNegation() {
    val actor = OnStage.theActor("Emmet");

    val dummyQuestion = new NonThrowingDummyQuestion();
    val negatedQuestion =
        ThatNotAllowedToAsk.the(dummyQuestion).with(TestcaseAbortedException.class);
    assertThrows(MissingNegativeBehaviourError.class, () -> negatedQuestion.performAs(actor));
  }

  @Test
  void shouldDetectCorrectlyThrowingTaskNegation() {
    val actor = OnStage.theActor("Einstein");

    val dummyQuestion = new ThrowingDummyQuestion(new TestcaseAbortedException("test"));
    val negatedQuestion =
        ThatNotAllowedToAsk.the(dummyQuestion).with(TestcaseAbortedException.class);

    var hasThrown = false;
    try {
      negatedQuestion.performAs(actor);
    } catch (MissingNegativeBehaviourError mnbe) {
      hasThrown = true;
    } finally {
      assertFalse(hasThrown);
    }
  }

  @RequiredArgsConstructor
  private static class ThrowingDummyQuestion implements Question<Boolean> {

    private final Throwable exception;

    @SneakyThrows
    @Override
    public Boolean answeredBy(Actor actor) {
      throw this.exception;
    }
  }

  private static class NonThrowingDummyQuestion implements Question<Boolean> {

    @Override
    public Boolean answeredBy(Actor actor) {
      return true;
    }
  }
}
