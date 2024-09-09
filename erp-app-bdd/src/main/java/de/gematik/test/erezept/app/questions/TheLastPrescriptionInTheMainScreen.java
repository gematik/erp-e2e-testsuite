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

package de.gematik.test.erezept.app.questions;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.app.mobile.elements.XpathPageElement;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TheLastPrescriptionInTheMainScreen implements Question<Boolean> {

  private final String xpath;

  @Override
  public Boolean answeredBy(Actor actor) {
    boolean isPresent = false;
    val driverAbility = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);

    driverAbility.tap(BottomNav.PRESCRIPTION_BUTTON);
    for (int i = 0; i < 3; i++) {
      driverAbility.tap(Mainscreen.REFRESH_BUTTON);

      if (driverAbility.isPresent(XpathPageElement.xPathPageElement(this.xpath))) {
        log.info("Found element: " + this.xpath);
        isPresent = true;
        break;
      }
    }
    return isPresent;
  }

  public static TheLastPrescriptionInTheMainScreen isPresent(
      String prescription, String validityDate, String status) {
    String xPathLabel = "//*[@label='" + prescription + ", " + validityDate + ", " + status + "']";
    return new TheLastPrescriptionInTheMainScreen(xPathLabel);
  }

  public static TheLastPrescriptionInTheMainScreen waitTillIsGone() {
    return new TheLastPrescriptionInTheMainScreen("fake_fake");
  }
}
