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

package de.gematik.test.erezept.actors;

import static java.text.MessageFormat.format;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Getter
@Slf4j
public abstract class ErpActor extends Actor {

  protected final ActorType type;

  protected ErpActor(ActorType type, String name) {
    super(name);
    this.type = type;
  }

  public final <A> A performs(Question<A> question) {
    return this.asksFor(question);
  }

  @Override
  public String toString() {
    return format("{0} {1}", type, this.getName());
  }
}
