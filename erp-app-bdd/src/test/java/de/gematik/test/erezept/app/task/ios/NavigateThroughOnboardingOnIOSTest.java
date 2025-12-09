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

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.app.abilities.HandleAppAuthentication;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.Onboarding;
import de.gematik.test.erezept.app.mobile.elements.OperatingSystem;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NavigateThroughOnboardingOnIOSTest {

  private String userName;

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
    userName = GemFaker.fakerName();
    val app = mock(UseIOSApp.class);
    when(app.getPlatformType()).thenReturn(PlatformType.IOS);

    val appUser = OnStage.theActorCalled((userName));
    givenThat(appUser).can(app);
    givenThat(appUser).can(HandleAppAuthentication.withStrongPassword());
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldBeAbleToNavigateThroughOnboarding() {
    val task = NavigateThroughOnboardingOnIOS.entirely();
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);
    val password = SafeAbility.getAbility(actor, HandleAppAuthentication.class).getPassword();

    assertDoesNotThrow(() -> actor.attemptsTo(task));

    verify(app, times(1)).tap(Onboarding.START_BUTTON);
    verify(app, times(1)).tap(Onboarding.NEXT_BUTTON);
    verify(app, times(2)).tap(Onboarding.PASSWORD_BUTTON);
    verify(app, times(1)).inputPassword(password, Onboarding.PASSWORD_INPUT_FIELD);
    verify(app, times(1)).swipe(SwipeDirection.UP);
    verify(app, times(1)).inputPassword(password, Onboarding.PASSWORD_CONFIRMATION_FIELD);
    verify(app, times(1)).tapIfDisplayed(3, OperatingSystem.LATER);
    verify(app, times(1)).tap(Onboarding.NOT_ACCEPT_ANALYTICS_BUTTON);
    verify(app, times(1)).logEvent("Remove all Tooltips");
    verify(app, times(1)).removeTooltips();

    verifyNoMoreInteractions(app);
  }
}
