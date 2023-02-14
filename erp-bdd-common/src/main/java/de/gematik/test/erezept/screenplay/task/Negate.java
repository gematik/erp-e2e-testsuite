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

package de.gematik.test.erezept.screenplay.task;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

/**
 * This Decorator-Task wraps a Task which is expected to throw a specific Exception. On perform of
 * this Decorator-Task the expected Exception will be caught
 */
@Slf4j
public class Negate extends AbstractNegationTask {

  private final Task decorated;

  private Negate(Task decorated, Class<? extends Throwable> expected) {
    super(expected);
    this.decorated = decorated;
  }

  @Override
  public <T extends Actor> void performAs(final T actor) {
    this.negateTask(actor, this.decorated);
  }

  public static NegateBuilder the(Task task) {
    return new NegateBuilder(task);
  }

  public static class NegateBuilder {
    private final Task decorated;

    private NegateBuilder(Task decorated) {
      this.decorated = decorated;
    }

    public Negate with(Class<? extends Throwable> expected) {
      return new Negate(this.decorated, expected);
    }
  }
}
