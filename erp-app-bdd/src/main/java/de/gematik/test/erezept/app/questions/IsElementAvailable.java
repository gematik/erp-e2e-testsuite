/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.app.questions;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.PageElement;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import org.openqa.selenium.NoSuchElementException;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IsElementAvailable implements Question<Boolean> {

  private final PageElement pageElement;

  @Override
  public Boolean answeredBy(Actor actor) {

    val driverAbility = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);
    try {
      driverAbility.getWebElement(pageElement);
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  public static IsElementAvailable withName(final PageElement pageElement) {
    return new IsElementAvailable(pageElement);
  }
  // with Text
}