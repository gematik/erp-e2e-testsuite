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

package de.gematik.test.erezept.app.task;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NavigateThroughCardwallTest {
  private String userName;

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
    val app = mock(UseIOSApp.class);
    when(app.getPlatformType()).thenReturn(PlatformType.IOS);

    userName = GemFaker.fakerName();
    val testActor = OnStage.theActorCalled((userName));
    givenThat(testActor).can(app);
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldHaveTheCorrectCardwallBehavior() {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    when(app.isDisplayed(Mainscreen.LOGIN_BUTTON)).thenReturn(true);

    val task = NavigateThroughCardwall.entirely();

    assertDoesNotThrow(() -> actor.attemptsTo(task));

    verify(app, times(1)).tap(CardWall.GKV_INSURED_BUTTON);

    verify(app, times(1)).tap(CardWall.ADD_HEALTH_CARD_BUTTON);
    verify(app, times(1)).inputPassword("123123", CardWall.CAN_INPUT_FIELD);
    verify(app, times(1)).tap(CardWall.CAN_ACCEPT_BUTTON);
    verify(app, times(1)).inputPassword("123456", CardWall.PIN_INPUT_FIELD);
    verify(app, times(1)).tap(CardWall.PIN_ACCEPT_BUTTON);

    verify(app, times(1)).tap(CardWall.DONT_SAVE_CREDENTIAL_BUTTON);
    verify(app, times(1)).tap(CardWall.CONTINUE_AFTER_BIOMETRY_CHECK_BUTTON);

    verify(app, times(1)).tap(CardWall.START_NFC_READOUT_BUTTON);
    verify(app, times(1)).waitUntilElementIsVisible(BottomNav.SETTINGS_BUTTON, 60000);
  }
}
