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

package de.gematik.test.erezept.app.steps;

import static net.serenitybdd.screenplay.GivenWhenThen.*;
import static org.junit.Assert.assertTrue;

import de.gematik.test.erezept.app.mobile.elements.Mainscreen;
import de.gematik.test.erezept.app.questions.IsElementAvailable;
import io.cucumber.java.de.Dann;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

public class OnboardingSteps {

  @Dann("sieht der User den Mainscreen")
  public void thenUserCanSeeTheMainscreen() {
    val theAppUser = OnStage.theActorInTheSpotlight();
    assertTrue(
        "Onboarding wurde erfolgreich durchlaufen und wir prüfen, ob wir uns auf dem Mainscreen der"
            + " App befinden",
        then(theAppUser).asksFor(IsElementAvailable.withName(Mainscreen.LOGIN_BUTTON)));
  }
}
