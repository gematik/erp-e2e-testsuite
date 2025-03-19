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

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.app.abilities.HandleAppAuthentication;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.mobile.elements.Settings;
import de.gematik.test.erezept.app.task.ValidateLegal;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.*;

class ValidateLegalDuringOnboardingIOSTest {

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
    givenThat(theAppUser).can(HandleAppAuthentication.withStrongPassword());
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void validateLegalInTheOnboardingTest() {
    val actor = OnStage.theActorCalled(userName);
    actor.attemptsTo(ValidateLegal.duringOnboarding());

    val app = actor.abilityTo(UseIOSApp.class);
    verify(app, times(1)).tap(Settings.ONBOARDING_TERM_OF_USE);
    verify(app, times(1)).tap(Settings.TERMS_OF_USE_TITLE);
    verify(app, times(1)).tap(Settings.CLOSE_TERM_OF_USE);
    verify(app, times(1)).tap(Settings.ONBOARDING_PRIVACY);
    verify(app, times(1)).tap(Settings.DATA_PROTECTION_TITLE);
  }

  @Test
  void validateLegalAfterTheOnboardingTest() {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    when(app.isPresent(Settings.LEGAL_TEXT)).thenReturn(true);

    actor.attemptsTo(ValidateLegal.insideSettingsMenu());

    verify(app, times(1)).tap(BottomNav.SETTINGS_BUTTON);
    verify(app, times(1)).tap(Settings.IMPRINT_BUTTON);
    verify(app, times(1)).tap(Settings.TERMS_BUTTON);
    verify(app, times(1)).tap(Settings.PRIVACY_BUTTON);
    verify(app, times(1)).tap(Settings.OPEN_SOURCE_BUTTON);
  }
}
