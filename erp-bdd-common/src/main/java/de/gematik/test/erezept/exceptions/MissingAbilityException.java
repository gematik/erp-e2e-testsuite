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

package de.gematik.test.erezept.exceptions;

import static java.text.MessageFormat.format;

import net.serenitybdd.core.exceptions.TestCompromisedException;
import net.serenitybdd.screenplay.Actor;

public class MissingAbilityException extends TestCompromisedException {

  public MissingAbilityException(Actor actor, Class<?> expectedAbility) {
    super(
        format(
            "The Actor {0} is missing the Ability to {1}",
            actor.getName(), expectedAbility.getSimpleName()));
  }
}
