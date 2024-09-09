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
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.mobile.elements.Debug;
import de.gematik.test.erezept.app.mobile.elements.Profile;
import de.gematik.test.erezept.app.mobile.elements.ProfileSelectorElement;
import de.gematik.test.erezept.app.mobile.elements.Settings;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@NoArgsConstructor
public class UsedSessionKVNR implements Question<String> {

  @Override
  @Step("{0} liest die KVNR aus dem Benutzerprofil aus")
  public String answeredBy(Actor actor) {
    val app = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);

    ensureSettingsMenuIsOpened(app);
    app.tap(ProfileSelectorElement.forActor(actor).fromSettingsMenu());
    val userKVNR = app.getText(Profile.USER_KVNR);
    app.tap(Debug.LEAVE_BUTTON);
    return userKVNR;
  }

  private void ensureSettingsMenuIsOpened(UseTheApp<?> app) {
    app.tap(BottomNav.SETTINGS_BUTTON);

    // occasionally, clicking the settings button does not open the settings menu
    if (!app.isPresent(Settings.SETTINGS_HEADER_LINE)) {
      // perform one single retry to open the settings menu
      app.tap(BottomNav.SETTINGS_BUTTON);
    }
  }

  public static UsedSessionKVNR fromUserProfile() {
    return Instrumented.instanceOf(UsedSessionKVNR.class).newInstance();
  }
}
