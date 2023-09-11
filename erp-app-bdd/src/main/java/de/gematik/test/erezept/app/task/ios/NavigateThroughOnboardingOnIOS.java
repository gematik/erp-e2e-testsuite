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
package de.gematik.test.erezept.app.task.ios;

import static de.gematik.test.erezept.app.mobile.OnboardingScreen.*;

import de.gematik.test.erezept.app.abilities.HandleAppAuthentication;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.OnboardingScreen;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.Onboarding;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NavigateThroughOnboardingOnIOS implements Task {
  private final OnboardingScreen onboardingScreen;

  public static NavigateThroughOnboardingOnIOS toScreen(OnboardingScreen onboardingScreen) {
    return new NavigateThroughOnboardingOnIOS(onboardingScreen);
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val driver = SafeAbility.getAbility(actor, UseIOSApp.class);

    if (onboardingScreen.getiOSOrdinal() >= WELCOME_SCREEN.getiOSOrdinal()) {
      driver.tap(Onboarding.NEXT_BUTTON);
    }

    if (onboardingScreen.getiOSOrdinal() >= TERMS_AND_PRIVACY_SCREEN.getiOSOrdinal()) {
      driver.tap(Onboarding.CHECK_PRIVACY_AND_TOU_BUTTON);
      driver.tap(Onboarding.ACCEPT_PRIVACY_AND_TOU_BUTTON);
    }

    if (onboardingScreen.getiOSOrdinal() >= SECURITY_SCREEN.getiOSOrdinal()) {
      val password = SafeAbility.getAbility(actor, HandleAppAuthentication.class).getPassword();
      driver.inputPassword(password, Onboarding.PASSWORD_INPUT_FIELD);
      driver.swipe(SwipeDirection.UP);
      driver.inputPassword(password, Onboarding.PASSWORD_CONFIRMATION_FIELD);

      driver.waitUntilElementIsVisible(Onboarding.ACCEPT_PASSWORD_BUTTON);
      driver.tap(Onboarding.ACCEPT_PASSWORD_BUTTON);
    }

    if (onboardingScreen.getiOSOrdinal() >= ANALYTICS_SCREEN.getiOSOrdinal()) {
      driver.tap(Onboarding.CONTINUE_ANALYTICS_SCREEN_BUTTON);
      driver.tap(Onboarding.NOT_ACCEPT_ANALYTICS_BUTTON);
    }

    if (onboardingScreen.getiOSOrdinal() >= FINISH_ALL.getiOSOrdinal()) {
      driver.tapIfDisplayed(Onboarding.HIDE_SUGGESTION_PIN_SELECTION_BUTTON);
      driver.tapIfDisplayed(Onboarding.ACCEPT_SUGGESTION_PIN_SELECTION_BUTTON);
    }
  }
}
