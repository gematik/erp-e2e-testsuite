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

import static net.serenitybdd.screenplay.GivenWhenThen.and;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.mobile.elements.PharmacySearch;
import de.gematik.test.erezept.app.questions.ThePharmacyProvidesDeliveryOptions;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.ensure.Ensure;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class WalkThroughMultiplePharmacies implements Task {

  private final List<Map<String, String>> pharmacyData;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseTheApp.class);

    pharmacyData.forEach(
        dataRow -> {
          val pharmacyName = dataRow.get("Name");
          givenThat(actor)
              .attemptsTo(OpenPharmacyViaSearchOnIos.named(pharmacyName).fromMainscreen());
          and(actor)
              .attemptsTo(
                  Ensure.that(ThePharmacyProvidesDeliveryOptions.givenFrom(dataRow)).isTrue());

          // after that simply go back for next iteration
          app.tap(PharmacySearch.PHARMACY_SEARCH_BACK_BUTTON);
          app.tap(PharmacySearch.CANCEL_SEARCH);
          app.tap(BottomNav.PRESCRIPTION_BUTTON);
        });
  }

  public static WalkThroughMultiplePharmacies givenFrom(List<Map<String, String>> pharmacyData) {
    return new WalkThroughMultiplePharmacies(pharmacyData);
  }
}
