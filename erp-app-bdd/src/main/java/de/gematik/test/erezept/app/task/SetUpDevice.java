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

import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.Environment;
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.task.android.SetUpAndroidDevice;
import de.gematik.test.erezept.app.task.ios.SetUpIosDevice;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor
public class SetUpDevice implements Task {

  private final Environment environment; // required for @Step-Annotation!!
  private final EnvironmentConfiguration environmentConfiguration;
  private final VersicherungsArtDeBasis insuranceKind;
  private final SmartcardArchive sca;

  @Override
  @Step("{0} installiert als #insuranceKind Patient/in die E-Rezept App für #environment")
  public <T extends Actor> void performAs(final T actor) {
    val app = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);

    app.logEvent(
        format(
            "Setup {0} Device for {1} Patient {2}",
            app.getPlatformType(), insuranceKind, actor.getName()));

    val platformTask =
        PlatformScreenplayUtil.chooseTaskForPlatform(
            app.getPlatformType(),
            () -> new SetUpAndroidDevice(insuranceKind, sca),
            () -> new SetUpIosDevice(environmentConfiguration, insuranceKind, sca));
    platformTask.performAs(actor);

    app.tap(BottomNav.PRESCRIPTION_BUTTON);
  }

  public static Builder forEnvironment(EnvironmentConfiguration environment) {
    return new Builder(environment);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final EnvironmentConfiguration environmentConfiguration;
    private VersicherungsArtDeBasis insuranceKind;

    public Builder withInsuranceType(String insuranceKind) {
      return withInsuranceType(VersicherungsArtDeBasis.fromCode(insuranceKind));
    }

    public Builder withInsuranceType(VersicherungsArtDeBasis insuranceKind) {
      this.insuranceKind = insuranceKind;
      return this;
    }

    public SetUpDevice byMappingVirtualEgkFrom(SmartcardArchive sca) {
      val environment = Environment.fromString(environmentConfiguration.getName());
      return Instrumented.instanceOf(SetUpDevice.class)
          .withProperties(environment, environmentConfiguration, insuranceKind, sca);
    }
  }
}
