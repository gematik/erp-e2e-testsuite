/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.app.task;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.mobile.elements.Mainscreen;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.app.mobile.elements.Receipt;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

import static de.gematik.test.erezept.app.mobile.elements.Receipt.REDEEMABLE_PRESCRIPTION_CARD_BUTTON;
import static java.text.MessageFormat.format;

@Slf4j
public class DeleteBatchRedeemablePrescriptions implements Task {
  public static DeleteBatchRedeemablePrescriptions insideTheApp() {
    return new DeleteBatchRedeemablePrescriptions();
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val driver = SafeAbility.getAbility(actor, UseTheApp.class);
    driver.tap(BottomNav.PRESCRIPTION_BUTTON);
    driver.tap(Mainscreen.REFRESH_BUTTON);

    if (driver.isPresent(Receipt.REDEEM_PRESCRIPTION_BTN)) {
      val elements = driver.getWebElements(REDEEMABLE_PRESCRIPTION_CARD_BUTTON);
      log.info(format("Found {0} receipt(s) to be deleted", elements.size()));
      for (int i = 0; i < elements.size(); i++) {
        log.info(format("Deleting {0}/{1} receipt(s)", i, elements.size()));
        driver.tap(Receipt.REDEEMABLE_PRESCRIPTION_CARD_BUTTON);
        driver.tap(PrescriptionDetails.DELETE_BUTTON_TOOLBAR);
        driver.tap(PrescriptionDetails.DELETE_PRESCRIPTION_ITEM_BUTTON);
        driver.acceptAlert();
        driver.tap(Mainscreen.REFRESH_BUTTON);
      }
    }
  }
}
