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

import static de.gematik.test.erezept.screenplay.task.BillingInformationConsent.ConsentAction.GRANT;
import static de.gematik.test.erezept.screenplay.task.BillingInformationConsent.ConsentAction.REVOKE;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.fail;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.screenplay.task.BillingInformationConsent;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@RequiredArgsConstructor
public class HandleChargeItemConsent implements Task {

  private final BillingInformationConsent.ConsentAction consentAction;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);

    if (consentAction.equals(GRANT)) {
      app.tap(BottomNav.SETTINGS_BUTTON);
      app.tap(ProfileSelectorElement.forActor(actor).fromSettingsMenu());
      app.swipeIntoView(SwipeDirection.UP, Profile.CHARGE_ITEM_BUTTON);
      app.tap(Profile.CHARGE_ITEM_BUTTON);
      app.tap(ChargeItemScreen.CONFIRM_GRANT_CONSENT_BUTTON);
      navigateBackToMainScreen(app);
    } else if (consentAction.equals(REVOKE)) {
      app.tap(ChargeItemScreen.THREE_DOT_MENU_BUTTON);
      app.tap(ChargeItemScreen.REVOKE_CONSENT_MENU_ENTRY_BUTTON);
      app.tap(ChargeItemScreen.CONFIRM_REVOKE_CONSENT_BUTTON);
      navigateBackToMainScreen(app);
      app.tapIfDisplayed(Mainscreen.CLOSE_CHARGE_ITEM_CONSENT_DRAWER_BUTTON);
    } else {
      fail(format("Invalid consentAction %s. It has to be either GRANT or REVOKE.", consentAction));
    }
  }

  private void navigateBackToMainScreen(UseTheApp<?> app) {
    app.tap(ChargeItemScreen.LEAVE_BUTTON);
    app.tap(Profile.LEAVE_BUTTON);
    app.tap(BottomNav.PRESCRIPTION_BUTTON);
  }

  public static HandleChargeItemConsent withAction(
      BillingInformationConsent.ConsentAction consentAction) {
    return new HandleChargeItemConsent(consentAction);
  }
}
