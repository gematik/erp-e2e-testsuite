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

package de.gematik.test.erezept.app.task.ios;

import de.gematik.test.erezept.app.abilities.HandleAppAuthentication;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.Onboarding;
import de.gematik.test.erezept.app.mobile.elements.OperatingSystem;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@RequiredArgsConstructor
public class NavigateThroughOnboardingOnIOS implements Task {

  public static NavigateThroughOnboardingOnIOS entirely() {
    return new NavigateThroughOnboardingOnIOS();
  }

  @Override
  @Step("{0} durchläuft das Onboarding")
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseIOSApp.class);

    // Welcome Screen
    app.tap(Onboarding.START_BUTTON);

    // Data & Usage Screen
    app.tap(Onboarding.NEXT_BUTTON);

    // App Safety Screen
    app.tap(Onboarding.PASSWORD_BUTTON);
    val password = SafeAbility.getAbility(actor, HandleAppAuthentication.class).getPassword();
    app.inputPassword(password, Onboarding.PASSWORD_INPUT_FIELD);
    app.swipe(SwipeDirection.UP);
    app.inputPassword(password, Onboarding.PASSWORD_CONFIRMATION_FIELD);
    app.tap(Onboarding.PASSWORD_BUTTON);

    // System Password Manager Dialog
    app.tapIfDisplayed(3, OperatingSystem.LATER);

    // Tracking Screen
    app.tap(Onboarding.NOT_ACCEPT_ANALYTICS_BUTTON);

    // Tooltips on Mainscreen
    app.logEvent("Remove all Tooltips");
    app.removeTooltips();
  }
}
