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

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.Settings;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
public class ValidateLegalDuringOnboardingIOS implements Task {

  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseTheApp.class);

    app.tap(Settings.ONBOARDING_TERM_OF_USE);
    app.tap(Settings.USER_TERMS_STAND);
    app.tap(Settings.CLOSE_TERM_OF_USE);
    app.tap(Settings.ONBOARDING_PRIVACY);
    app.tap(Settings.USER_PRIVACY);
  }

  public static ValidateLegalDuringOnboardingIOS insideTheApp() {
    return new ValidateLegalDuringOnboardingIOS();
  }
}
