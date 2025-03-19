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
 */

package de.gematik.test.erezept.app.task.ios;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.app.abilities.UseConfigurationData;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.Environment;
import de.gematik.test.erezept.app.mobile.elements.DebugSettings;
import de.gematik.test.erezept.app.task.ChangeTheEnvironment;
import de.gematik.test.erezept.app.task.CreateNewProfile;
import de.gematik.test.erezept.app.task.NavigateThroughCardwall;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@RequiredArgsConstructor
public class SetUpIosDevice implements Task {

  private final EnvironmentConfiguration environment;
  private final InsuranceTypeDe insuranceKind;
  private final SmartcardArchive sca;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);
    val userConfig = SafeAbility.getAbility(actor, UseConfigurationData.class);

    actor.attemptsTo(NavigateThroughOnboardingOnIOS.entirely());
    actor.attemptsTo(CreateNewProfile.fromSettingsMenu());
    actor.attemptsTo(
        ChangeTheEnvironment.bySwitchInTheDebugMenuTo(
            Environment.fromString(environment.getName())));

    if (userConfig.useVirtualEgk()) {
      val egk = sca.getEgkByICCSN(userConfig.getEgkIccsn());
      actor.attemptsTo(SetVirtualEgkOnIOS.withEgk(egk));
    }

    app.tap(DebugSettings.LEAVE_BUTTON);
    actor.attemptsTo(
        NavigateThroughCardwall.forEnvironment(environment)
            .byMappingVirtualEgkFrom(sca, insuranceKind));
  }
}
