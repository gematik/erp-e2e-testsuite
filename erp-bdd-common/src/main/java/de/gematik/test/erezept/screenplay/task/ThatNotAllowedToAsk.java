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

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
public class ThatNotAllowedToAsk extends AbstractNegationTask {

  private final Question<?> decorated;

  private ThatNotAllowedToAsk(Question<?> decorated, Class<? extends Throwable> expected) {
    super(expected);
    this.decorated = decorated;
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    this.negateQuestion(actor, this.decorated);
  }

  public static Builder the(Question<?> question) {
    return new Builder(question);
  }

  public static class Builder {
    private final Question<?> decorated;

    private Builder(Question<?> decorated) {
      this.decorated = decorated;
    }

    public ThatNotAllowedToAsk with(Class<? extends Throwable> expected) {
      return new ThatNotAllowedToAsk(this.decorated, expected);
    }
  }
}
