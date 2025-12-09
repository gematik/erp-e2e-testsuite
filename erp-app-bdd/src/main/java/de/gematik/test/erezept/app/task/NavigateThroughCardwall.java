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

package de.gematik.test.erezept.app.task;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.mobile.elements.CardWall;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor
public class NavigateThroughCardwall implements Task {

  public static NavigateThroughCardwall entirely() {
    return new NavigateThroughCardwall();
  }

  @Override
  @Step("{0} meldet sich mit seiner/ihrer eGK von der #insuranceKind in der E-Rezept App an")
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseTheApp.class);

    app.tap(BottomNav.PRESCRIPTION_BUTTON);
    app.tap(CardWall.GKV_INSURED_BUTTON);

    app.tap(CardWall.ADD_HEALTH_CARD_BUTTON);
    app.inputPassword("123123", CardWall.CAN_INPUT_FIELD);
    app.tap(CardWall.CAN_ACCEPT_BUTTON);
    app.inputPassword("123456", CardWall.PIN_INPUT_FIELD);
    app.tap(CardWall.PIN_ACCEPT_BUTTON);
    app.tap(CardWall.DONT_SAVE_CREDENTIAL_BUTTON);
    app.tap(CardWall.CONTINUE_AFTER_BIOMETRY_CHECK_BUTTON);

    app.tap(CardWall.START_NFC_READOUT_BUTTON);
    app.waitUntilElementIsVisible(
        BottomNav.SETTINGS_BUTTON, 60000); // wait until the pairing finished
  }
}
