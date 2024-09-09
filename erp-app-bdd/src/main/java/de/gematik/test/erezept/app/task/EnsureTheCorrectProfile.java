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

package de.gematik.test.erezept.app.task;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.elements.ProfileSelectorElement;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

public class EnsureTheCorrectProfile implements Task {

  @Override
  @Step("{0} vergewissert sich, dass sein/ihr Profil aktiviert ist")
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseIOSApp.class);
    if (!actor.getName().equals(app.getCurrentUserProfile())) {
      app.logEvent(format("Der Benutzer {0} wechselt zu seinem Profil", actor.getName()));
      app.tap(ProfileSelectorElement.forActor(actor).fromMainScreen());
      app.waitUntilElementIsSelected(ProfileSelectorElement.forActor(actor).fromMainScreen());
      app.setCurrentUserProfile(actor.getName());
    }
  }

  public static EnsureTheCorrectProfile isChosen() {
    return new EnsureTheCorrectProfile();
  }
}
