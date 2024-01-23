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

package de.gematik.test.erezept.app.steps;

import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.app.task.NavigateThroughOnboarding;
import de.gematik.test.erezept.app.task.ValidateLegal;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

@Slf4j
public class SettingsSteps {

  @Wenn("^(?:der|die) Versicherte das Impressum während des Onboarding überprüfen möchte$")
  public void userCanFinishTheOnboardingUntilSecurityScreen() {
    val theAppUser = OnStage.theActorInTheSpotlight();
    when(theAppUser).attemptsTo(NavigateThroughOnboarding.untilTermsAndPrivacy());
  }

  @Wenn("^(?:der|die) Versicherte das Impressum nach dem Onboarding überprüfen möchte$")
  public void userCanFinishTheOnboardingAfterTOUScreen() {
    val theAppUser = OnStage.theActorInTheSpotlight();
    when(theAppUser)
        .attemptsTo(NavigateThroughOnboarding.byFinishingTheEntireOnboardingSuccessfully());
  }

  @Dann("^sind die Nutzungsbedingungen und den Datenschutz sichtbar$")
  public void dieNutzungsbedingungenUndDenDatenschutzSindSichtbar() {
    val theAppUser = OnStage.theActorInTheSpotlight();
    when(theAppUser).attemptsTo(ValidateLegal.duringOnboarding());
  }

  @Dann("sind die Nutzungsbedingungen, der Datenschutz, das Impressum und die Lizenzen sichtbar")
  public void sindDieNutzungsbedingungenDerDatenschutzDasImpressumUndDieLizenzenSichtbar() {
    val theAppUser = OnStage.theActorInTheSpotlight();
    when(theAppUser).attemptsTo(ValidateLegal.insideSettingsMenu());
  }
}
