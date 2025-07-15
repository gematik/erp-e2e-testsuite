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
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.mobile.elements.Mainscreen;
import de.gematik.test.erezept.app.mobile.elements.NewProfileScreen;
import de.gematik.test.erezept.app.mobile.elements.Settings;
import de.gematik.test.erezept.exceptions.FeatureNotImplementedException;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor
public class CreateNewProfile implements Task {

  private final boolean fromSettingsMenu;
  private final String fromLabel; // NOSONAR; this one is required for reporting the @Step

  @Override
  @Step("{0} erstellt ein neues Benutzerprofil aus #fromLabel")
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseIOSApp.class);

    if (fromSettingsMenu) {
      app.tap(BottomNav.SETTINGS_BUTTON);
      app.tap(Settings.NEW_USER_PROFILE_BUTTON);
      app.input(actor.getName(), NewProfileScreen.NAME_INPUT);
      app.tap(NewProfileScreen.SAVE_PROFILE_BUTTON);
      app.setCurrentUserProfile(actor.getName());
    } else {
      // Note: Simulator might need more time before it can create a profile
      app.pauseApp();

      app.tap(Mainscreen.NEW_USER_PROFILE_BUTTON);
      throw new FeatureNotImplementedException("create new profile from main screen");
    }
  }

  public static CreateNewProfile fromSettingsMenu() {
    return new CreateNewProfile(true, "dem Settings-Menu");
  }

  public static CreateNewProfile fromMainScreen() {
    return new CreateNewProfile(false, "dem Haupt-Screen");
  }
}
