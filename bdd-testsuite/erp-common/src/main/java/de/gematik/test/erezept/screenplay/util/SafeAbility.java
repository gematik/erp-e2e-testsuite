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

package de.gematik.test.erezept.screenplay.util;

import de.gematik.test.erezept.exceptions.MissingAbilityException;
import lombok.val;
import net.serenitybdd.screenplay.Ability;
import net.serenitybdd.screenplay.Actor;

public class SafeAbility {

  private SafeAbility() {
    throw new AssertionError();
  }

  /**
   * Get an ability from a given Actor in a safe manner. That means that only an ability is returned
   * if the Actor has this ability
   *
   * @param actor who should have a specific ability
   * @param ability which the actor should have
   * @param <T> the type of the requested ability
   * @return the requested Ability
   * @throws MissingAbilityException if the given actor does not have the required ability
   */
  public static <T extends Ability> T getAbility(Actor actor, Class<T> ability) {
    val ret = actor.abilityTo(ability);
    if (ret == null) {
      throw new MissingAbilityException(actor, ability);
    }
    return ret;
  }

  public static <T extends Ability> T getAbilityThatExtends(Actor actor, Class<T> ability) {
    val ret = actor.getAbilityThatExtends(ability);
    if (ret == null) {
      throw new MissingAbilityException(actor, ability);
    }
    return ret;
  }
}
