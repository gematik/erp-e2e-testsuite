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

import static de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe.GKV;
import static de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe.PKV;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@RequiredArgsConstructor
public class EnsureProfileMatchesInsurance implements Task {

  private final InsuranceTypeDe insuranceType;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);

    app.tap(BottomNav.SETTINGS_BUTTON);
    app.tap(ProfileSelectorElement.forActor(actor).fromSettingsMenu());

    val appConfiguredInsurance = app.getText(Profile.USER_INSURANCE).toLowerCase();

    if (insuranceType.equals(PKV) && appConfiguredInsurance.contains("gkv")) {
      // Note: mark profile as pkv in the debug menu
      app.tap(Profile.LEAVE_BUTTON);
      app.tap(Settings.DEBUG_BUTTON);
      app.swipeIntoView(SwipeDirection.UP, Debug.MARK_AS_PKV_BUTTON);
      app.tap(Debug.MARK_AS_PKV_BUTTON);
      app.tap(Debug.LEAVE_BUTTON);
      app.tap(BottomNav.PRESCRIPTION_BUTTON);
      // Note: sometimes the charge item drawer opens too late
      app.waitUntilElementIsVisible(Mainscreen.CLOSE_CHARGE_ITEM_CONSENT_DRAWER_BUTTON, 60000);
      app.tap(Mainscreen.CLOSE_CHARGE_ITEM_CONSENT_DRAWER_BUTTON);
    }

    if (insuranceType.equals(GKV) && appConfiguredInsurance.contains("pkv")) {
      // Note: delete the current profile and create a new one and log in again
      app.swipeIntoView(SwipeDirection.UP, Profile.DELETE_PROFILE_BUTTON);
      app.tap(Profile.DELETE_PROFILE_BUTTON);
      app.tap(Profile.CONFIRM_PROFILE_DELETION_BUTTON);
      actor.attemptsTo(CreateNewProfile.fromSettingsMenu());
      app.tap(DebugSettings.LEAVE_BUTTON);
      actor.attemptsTo(NavigateThroughCardwall.entirely());
    }
  }

  public static EnsureProfileMatchesInsurance ofType(InsuranceTypeDe insuranceType) {
    return new EnsureProfileMatchesInsurance(insuranceType);
  }
}
