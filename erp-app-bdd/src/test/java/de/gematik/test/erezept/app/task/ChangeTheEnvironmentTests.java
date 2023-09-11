/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.app.task;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.Environment;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.mobile.elements.DebugSettings;
import de.gematik.test.erezept.app.mobile.elements.Settings;
import de.gematik.test.erezept.app.mobile.elements.XpathPageElement;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChangeTheEnvironmentTests {

  private String userName;

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
    val app = mock(UseIOSApp.class);
    when(app.getPlatformType()).thenReturn(PlatformType.IOS);

    userName = GemFaker.fakerName();
    val aliceIos = OnStage.theActorCalled((userName));
    givenThat(aliceIos).can(app);
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldFollowTheCorrectSwitchFlowInDebugMenuForRU() {
    val task = ChangeTheEnvironment.bySwitchInTheDebugMenuTo(Environment.RU);
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    assertDoesNotThrow(() -> actor.attemptsTo(task));

    verify(app, times(1)).tap(BottomNav.SETTINGS_BUTTON);
    verify(app, times(1)).tap(Settings.DEBUG_BUTTON);

    verify(app, times(1)).tap(DebugSettings.ENVIRONMENT_SELECTOR);
    verify(app, times(1)).tap(DebugSettings.RU_ENVIRONMENT);
    verify(app, times(1)).tap(DebugSettings.LEAVE_BUTTON);
  }

  @Test
  void shouldFollowTheCorrectSwitchFlowInDebugMenuForTU() {
    val task = ChangeTheEnvironment.bySwitchInTheDebugMenuTo(Environment.TU);
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    assertDoesNotThrow(() -> actor.attemptsTo(task));

    verify(app, times(1)).tap(BottomNav.SETTINGS_BUTTON);
    verify(app, times(1)).tap(Settings.DEBUG_BUTTON);
    verify(app, times(1)).tap(DebugSettings.LEAVE_BUTTON);
  }

  @Test
  void shouldFollowTheCorrectSwitchFlowInDebugMenuForRU_DEV() {
    val task = ChangeTheEnvironment.bySwitchInTheDebugMenuTo(Environment.RU_DEV);
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    assertDoesNotThrow(() -> actor.attemptsTo(task));

    verify(app, times(1)).tap(BottomNav.SETTINGS_BUTTON);
    verify(app, times(1)).tap(Settings.DEBUG_BUTTON);

    verify(app, times(1)).tap(DebugSettings.ENVIRONMENT_SELECTOR);
    verify(app, times(1)).tap(DebugSettings.RU_DEV_ENVIRONMENT);
    verify(app, times(1)).tap(DebugSettings.LEAVE_BUTTON);
  }

  @Test
  void shouldNotChangeEnvironment() {
    val task = ChangeTheEnvironment.bySwitchInTheDebugMenuTo(Environment.RU_DEV);
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);
    //when(iosAbility.isElementPresent("//*[@label='RU_DEV' and (./preceding-sibling::* | ./following-sibling::*)[@label='Current']]")).thenReturn(true);
    when(app.isPresent(XpathPageElement.xPathPageElement("//*[@label='RU_DEV' and (./preceding-sibling::* | ./following-sibling::*)[@label='Current']]"))).thenReturn(true);
    assertDoesNotThrow(() -> actor.attemptsTo(task));

    verify(app, times(1)).tap(BottomNav.SETTINGS_BUTTON);
    verify(app, times(1)).tap(Settings.DEBUG_BUTTON);
    verify(app, times(1)).tap(DebugSettings.LEAVE_BUTTON);
  }
}
