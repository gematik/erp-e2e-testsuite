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

package de.gematik.test.erezept.app.task.ios;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.elements.EVDGAInsuranceDetails;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@RequiredArgsConstructor
public class SelectEVDGAInsuranceOnIOS implements Task {

  private final String ktrName;

  @Override
  @Step("{0} wählt eine Versicherung für die DIGA aus")
  public <T extends Actor> void performAs(T actor) {

    val app = SafeAbility.getAbility(actor, UseIOSApp.class);

    app.input(ktrName, EVDGAInsuranceDetails.INSURANCE_SEARCH_BAR);

    val ktrEntry = EVDGAInsuranceDetails.forInsuranceEntry(ktrName);
    app.waitUntilElementIsVisible(ktrEntry);
    app.tap(ktrEntry);
  }

  public static SelectEVDGAInsuranceOnIOS fromListNamed(Actor ktr) {
    return fromListNamed(ktr.getName());
  }

  public static SelectEVDGAInsuranceOnIOS fromListNamed(String ktrName) {
    return Instrumented.instanceOf(SelectEVDGAInsuranceOnIOS.class).withProperties(ktrName);
  }
}
