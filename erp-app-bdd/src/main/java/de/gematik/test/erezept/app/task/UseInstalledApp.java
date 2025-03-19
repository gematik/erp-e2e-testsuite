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

package de.gematik.test.erezept.app.task;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.app.abilities.UseConfigurationData;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.mobile.elements.DebugSettings;
import de.gematik.test.erezept.app.mobile.elements.Mainscreen;
import de.gematik.test.erezept.app.mobile.elements.Settings;
import de.gematik.test.erezept.app.task.ios.SetVirtualEgkOnIOS;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor
public class UseInstalledApp implements Task {

  private final Actor deviceOwner;
  private final EnvironmentConfiguration environment;
  private final InsuranceTypeDe insuranceKind;
  private final SmartcardArchive sca;

  @Override
  @Step("{0} erstellt sich ein Benutzerprofil in der App von #deviceOwner")
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(deviceOwner, UseIOSApp.class);
    val userConfig = SafeAbility.getAbility(actor, UseConfigurationData.class);
    app.logEvent(format("Erstellen eines zweiten Benutzerprofils für {0}", actor.getName()));

    actor.can(app); // new the actor should be able to use the app of the device owner
    actor.attemptsTo(CreateNewProfile.fromSettingsMenu());

    app.tap(Settings.DEBUG_BUTTON);
    // co-user of an App will always use their virtual eGK
    actor.attemptsTo(SetVirtualEgkOnIOS.withEgk(sca.getEgkByICCSN(userConfig.getEgkIccsn())));
    app.tap(DebugSettings.LEAVE_BUTTON);

    actor.attemptsTo(
        NavigateThroughCardwall.forEnvironment(environment)
            .byMappingVirtualEgkFrom(sca, insuranceKind));

    // NOTE: once navigated through cardwall and logged in: waste some time here and tap refresh a
    // few times!!
    for (var i = 0; i < 5; i++) {
      // this hack is required to enforce a login, otherwise we won't find any prescriptions later
      // on
      app.tap(BottomNav.PRESCRIPTION_BUTTON);
      app.tap(Mainscreen.REFRESH_BUTTON);
    }
  }

  public static Builder ownedBy(Actor deviceOwner) {
    return new Builder(deviceOwner);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {

    private final Actor deviceOwner;
    private EnvironmentConfiguration environment;
    private InsuranceTypeDe insuranceKind;

    public Builder withInsuranceType(String insuranceKind) {
      return withInsuranceType(InsuranceTypeDe.fromCode(insuranceKind));
    }

    public Builder withInsuranceType(InsuranceTypeDe insuranceKind) {
      this.insuranceKind = insuranceKind;
      return this;
    }

    public Builder forEnvironment(EnvironmentConfiguration environment) {
      this.environment = environment;
      return this;
    }

    public UseInstalledApp byMappingVirtualEgkFrom(SmartcardArchive sca) {
      return Instrumented.instanceOf(UseInstalledApp.class)
          .withProperties(deviceOwner, environment, insuranceKind, sca);
    }
  }
}
