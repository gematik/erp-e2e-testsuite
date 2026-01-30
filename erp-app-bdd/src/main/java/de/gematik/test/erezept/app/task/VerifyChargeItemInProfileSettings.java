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
import de.gematik.test.erezept.app.mobile.elements.ChargeItemDrawer;
import de.gematik.test.erezept.app.mobile.elements.ChargeItemScreen;
import de.gematik.test.erezept.client.usecases.ChargeItemGetByIdCommand;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor
public class VerifyChargeItemInProfileSettings implements Task {
  private final boolean fromChargeItemDrawer;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseIOSApp.class);
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val dmcAbility = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = DequeStrategy.FIFO.chooseFrom(dmcAbility.chooseStack(DmcStack.ACTIVE));

    if (fromChargeItemDrawer) {
      app.tap(ChargeItemDrawer.OPEN_CHARGE_ITEM_OVERVIEW_BUTTON);
    }

    app.waitUntilElementIsVisible(ChargeItemScreen.CHARGE_ITEM_LIST_HEADER_TEXT_FIELD);
    app.tap(ChargeItemScreen.CHARGE_ITEM_LIST_ELEMENT);

    val erpResponse =
        erpClient.request(new ChargeItemGetByIdCommand(dmc.getTaskId().toPrescriptionId()));
    val erxChargeItemBundle = erpResponse.getResourceOptional().orElseThrow();
    actor.attemptsTo(VerifyChargeItemInformation.forErxChargeItemBundle(erxChargeItemBundle));
  }

  public static VerifyChargeItemInProfileSettings fromChargeItemDrawer() {
    return new VerifyChargeItemInProfileSettings(true);
  }
}
