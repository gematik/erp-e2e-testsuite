/*
 * Copyright (Change Date see Readme), gematik GmbH
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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.Environment;
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.task.android.SetUpAndroidDevice;
import de.gematik.test.erezept.app.task.ios.SetUpIosDevice;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.common.screenplay.util.SafeAbility;
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
  private final InsuranceTypeDe insuranceKind;
  private final ErpClientFactory erpClientFactory;

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
            () -> new SetUpAndroidDevice(insuranceKind, erpClientFactory.getSmartcards()),
            () -> new SetUpIosDevice(insuranceKind, erpClientFactory));
    platformTask.performAs(actor);

    app.tap(BottomNav.PRESCRIPTION_BUTTON);
  }

  public static Builder forInsuranceType(String insuranceKind) {
    return forInsuranceType(InsuranceTypeDe.fromCode(insuranceKind));
  }

  public static Builder forInsuranceType(InsuranceTypeDe insuranceType) {
    return new Builder(insuranceType);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final InsuranceTypeDe insuranceKind;

    public SetUpDevice byMappingVirtualEgkFrom(ErpClientFactory erpClientFactory) {
      val environment = Environment.fromString(erpClientFactory.getEnv().getName());

      return Instrumented.instanceOf(SetUpDevice.class)
          .withProperties(environment, insuranceKind, erpClientFactory);
    }
  }
}
