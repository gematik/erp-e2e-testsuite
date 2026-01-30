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

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.exceptions.AppStateMissmatchException;
import de.gematik.test.erezept.app.mobile.ListPageElement;
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.mobile.elements.MessageScreen;
import de.gematik.test.erezept.client.usecases.ChargeItemGetByIdCommand;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@RequiredArgsConstructor
public class VerifyChargeItemInMessages implements Task {

  @Step("{0} verifiziert den Kostenbeleg in den Nachrichten")
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseIOSApp.class);
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val dmcAbility = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = DequeStrategy.FIFO.chooseFrom(dmcAbility.chooseStack(DmcStack.ACTIVE));

    actor.attemptsTo(OpenLatestMessage.fromMainScreen());

    val erpResponse =
        erpClient.request(new ChargeItemGetByIdCommand(dmc.getTaskId().toPrescriptionId()));
    val erxChargeItemBundle = erpResponse.getResourceOptional().orElseThrow();

    var tries = 3;
    var foundChargeItem = app.isDisplayed(MessageScreen.SHOW_CHARGE_ITEM_BUTTON);

    while (!foundChargeItem && tries > 0) {
      if (app.isDisplayed(MessageScreen.BACK_TO_MESSAGE_SCREEN)) {
        app.tap(MessageScreen.BACK_TO_MESSAGE_SCREEN);
        app.tap(BottomNav.PRESCRIPTION_BUTTON);
        app.tap(BottomNav.MESSAGES_BUTTON);
        app.tap(ListPageElement.forElement(MessageScreen.MESSAGES_LIST, 0));
      }

      foundChargeItem = app.isDisplayed(MessageScreen.SHOW_CHARGE_ITEM_BUTTON);
      tries--;
    }

    if (!foundChargeItem) {
      throw new AppStateMissmatchException("No charge item was found in the messages");
    }

    app.tap(MessageScreen.SHOW_CHARGE_ITEM_BUTTON);

    actor.attemptsTo(VerifyChargeItemInformation.forErxChargeItemBundle(erxChargeItemBundle));
  }

  public static VerifyChargeItemInMessages fromMainScreen() {
    return new VerifyChargeItemInMessages();
  }
}
