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

package de.gematik.test.erezept.app.task.ios;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.mobile.elements.PharmacySearch;
import de.gematik.test.erezept.app.mobile.elements.PharmacySearch.SearchResultEntry;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.screenplay.abilities.ProvideApoVzdInformation;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpenPharmacyViaSearchOnIosTest {
  private String userName;

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});

    val app = mock(UseIOSApp.class);
    when(app.getPlatformType()).thenReturn(PlatformType.IOS);

    // assemble the screenplay
    userName = GemFaker.fakerName();
    val theAppUser = OnStage.theActorCalled(userName);
    givenThat(theAppUser).can(app);
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldOpenPharmacySearchForPharmacyActor() {
    val pharmacy = OnStage.theActorCalled("Pharmacy");
    val apoVzdName = "Pharmacy-TEST-ONLY";
    pharmacy.can(ProvideApoVzdInformation.withName(apoVzdName));
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    val action = OpenPharmacyViaSearchOnIos.named(pharmacy).fromMainscreen();
    assertDoesNotThrow(() -> actor.attemptsTo(action));

    verify(app, times(1)).tap(BottomNav.PHARMACY_SEARCH_BUTTON);
    verify(app, times(1)).input(apoVzdName + "\n", PharmacySearch.SEARCH_FIELD);
    verify(app, times(1)).tap(any(SearchResultEntry.class));
  }

  @Test
  void shouldOpenPharmacySearchForPharmacyActorFromSearchScreen() {
    val pharmacy = OnStage.theActorCalled("Pharmacy");
    val apoVzdName = "Pharmacy-TEST-ONLY";
    pharmacy.can(ProvideApoVzdInformation.withName(apoVzdName));
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    val action = OpenPharmacyViaSearchOnIos.named(pharmacy).fromPharmacySearchscreen();
    assertDoesNotThrow(() -> actor.attemptsTo(action));

    // should have skipped the BottomNav.PHARMACY_SEARCH_BUTTON
    verify(app, times(0)).tap(BottomNav.PHARMACY_SEARCH_BUTTON);
    verify(app, times(1)).input(apoVzdName + "\n", PharmacySearch.SEARCH_FIELD);
    verify(app, times(1)).tap(any(SearchResultEntry.class));
  }
}
