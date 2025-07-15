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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.mobile.elements.PharmacySearch;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class OpenPharmacyViaSearchOnIos implements Task {

  private final String pharmacyName;
  private final boolean alreadyOnSearchScreen;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseTheApp.class);

    if (!alreadyOnSearchScreen) {
      app.tap(BottomNav.PHARMACY_SEARCH_BUTTON);
    }

    // append \n to simulate Keys.ENTER event
    val searchInput = format("{0}\n", pharmacyName);
    app.input(searchInput, PharmacySearch.SEARCH_FIELD);

    // Note: Rendering the pharmacy list can take more time on simulators
    app.pauseApp();

    // we will always open only the first element to avoid expensive search through the whole list!
    app.tap(PharmacySearch.forPharmacyEntry(pharmacyName));
  }

  public static Builder named(Actor pharmacy) {
    val name =
        SafeAbility.getAbility(pharmacy, UseSMCB.class).getSmcB().getOwnerData().getCommonName();

    return named(name);
  }

  public static Builder named(String name) {
    return new Builder(name);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final String pharmacyName;

    public OpenPharmacyViaSearchOnIos fromMainscreen() {
      return new OpenPharmacyViaSearchOnIos(pharmacyName, false);
    }

    public OpenPharmacyViaSearchOnIos fromPharmacySearchscreen() {
      return new OpenPharmacyViaSearchOnIos(pharmacyName, true);
    }
  }
}
