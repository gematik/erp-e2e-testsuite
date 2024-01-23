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

package de.gematik.test.erezept.app.task.ios;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.ScrollDirection;
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.mobile.elements.DebugSettings;
import de.gematik.test.erezept.app.mobile.elements.Settings;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
public class ValidateLegalInsideSettingsIOS implements Task {

  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseTheApp.class);

    app.tap(BottomNav.SETTINGS_BUTTON);
    app.scrollIntoView(ScrollDirection.DOWN, Settings.LEGAL_TEXT);
    assertTrue(
        app.isPresent(Settings.LEGAL_TEXT),
        format("Unable to find {0}", Settings.LEGAL_TEXT.getElementName()));

    app.tap(Settings.IMPRINT_BUTTON);
    app.isPresent(Settings.IMPRINT_LEGEND);
    app.tap(DebugSettings.LEAVE_BUTTON);

    app.tap(Settings.TERMS_BUTTON);
    app.tap(Settings.TERMS_OF_USE_TITLE);
    app.tap(DebugSettings.LEAVE_BUTTON);

    app.tap(Settings.PRIVACY_BUTTON);
    app.tap(Settings.DATA_PROTECTION_TITLE);
    app.tap(DebugSettings.LEAVE_BUTTON);

    app.tap(Settings.OPEN_SOURCE_BUTTON);
    app.isPresent(Settings.OPEN_SOURCE_LEGEND);
    app.tap(DebugSettings.LEAVE_BUTTON);
  }

  public static ValidateLegalInsideSettingsIOS insideTheApp() {
    return new ValidateLegalInsideSettingsIOS();
  }
}
