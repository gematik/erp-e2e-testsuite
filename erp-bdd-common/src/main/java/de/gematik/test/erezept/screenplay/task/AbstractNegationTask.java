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

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.exceptions.MissingNegativeBehaviourError;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.Task;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@SuppressWarnings({"java:S1181"}) // if not the expected one, we'll rethrow it anyways!
public abstract class AbstractNegationTask implements Task {

  private final Class<? extends Throwable> expected;

  protected void negateTask(Actor actor, Task task) {
    var hasThrownCorrectly = false;
    try {
      task.performAs(actor);
    } catch (Throwable e) {
      hasThrownCorrectly = hasThrownCorrectly(e);
    }

    checkMissingNegativeBehaviour(
        hasThrownCorrectly,
        task.getClass().getSimpleName(),
        () -> new MissingNegativeBehaviourError(task, expected));
  }

  protected void negateQuestion(Actor actor, Question<?> question) {
    var hasThrownCorrectly = false;
    try {
      question.answeredBy(actor);
    } catch (Throwable e) {
      hasThrownCorrectly = hasThrownCorrectly(e);
    }

    checkMissingNegativeBehaviour(
        hasThrownCorrectly,
        question.getClass().getSimpleName(),
        () -> new MissingNegativeBehaviourError(question, expected));
  }

  @SneakyThrows
  private boolean hasThrownCorrectly(Throwable e) {
    log.info(
        format(
            "Well done, negated Action threw an Exception of Type {0}",
            e.getClass().getSimpleName()));
    if (!isExpectedError(e)) {
      log.error("Unexpected Exception", e);
      throw e; // rethrow if an unexpected one, which prevents swallowing unexpected throwables
    }
    return true;
  }

  private void checkMissingNegativeBehaviour(
      boolean thrownCorrectly,
      String actionName,
      Supplier<MissingNegativeBehaviourError> errorSupplier) {
    if (!thrownCorrectly) {
      // well decorated Task has not thrown the expected Exception!
      log.warn(
          format(
              "Negated Action {0} did not throw any Exceptions although {1} was expected",
              actionName, expected.getSimpleName()));
      throw errorSupplier.get();
    } else {
      log.info(
          format(
              "Passed negated Action: {0} has thrown the correct Exception of type {1}",
              actionName, expected.getSimpleName()));
    }
  }

  private boolean isExpectedError(Throwable error) {
    return error.getClass().equals(expected)
        || (error.getCause() != null && error.getCause().getClass().equals(expected));
  }
}
