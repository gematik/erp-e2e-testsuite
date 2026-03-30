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

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.mobile.elements.Debug;
import de.gematik.test.erezept.app.mobile.elements.FeatureFlagScreen;
import de.gematik.test.erezept.app.mobile.elements.Settings;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor
public class EnsureThatTheEUFeature implements Task {
  private final boolean shouldBeActive;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);

    app.tap(BottomNav.SETTINGS_BUTTON);
    app.tap(Settings.DEBUG_BUTTON);
    app.swipeIntoView(SwipeDirection.UP, Debug.FEATURE_FLAG_BUTTON);
    app.tap(Debug.FEATURE_FLAG_BUTTON);
    val isToggledOn = app.getText(FeatureFlagScreen.ENABLE_EU_REDEEM_FEATURE_SWITCH).equals("1");
    val shouldBeTapped = (shouldBeActive && !isToggledOn) || (!shouldBeActive && isToggledOn);

    if (shouldBeTapped) {
      app.tap(FeatureFlagScreen.ENABLE_EU_REDEEM_FEATURE_SWITCH);
    }

    app.tap(FeatureFlagScreen.LEAVE_BUTTON);
    app.tap(Debug.LEAVE_BUTTON);
  }

  public static EnsureThatTheEUFeature isActive() {
    return new EnsureThatTheEUFeature(true);
  }

  public static EnsureThatTheEUFeature isInActive() {
    return new EnsureThatTheEUFeature(false);
  }
}
