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

package de.gematik.test.erezept.app.task;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.Environment;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.DebugSettings;
import de.gematik.test.erezept.app.mobile.elements.Settings;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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

  @ParameterizedTest
  @EnumSource(value = Environment.class)
  void shouldFollowTheCorrectSwitchFlowInDebugMenu(Environment env) {
    val task = ChangeTheEnvironment.bySwitchInTheDebugMenuTo(env);
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    assertDoesNotThrow(() -> actor.attemptsTo(task));

    verify(app, times(1)).tap(Settings.DEBUG_BUTTON);
    verify(app, times(1)).tap(DebugSettings.ENVIRONMENT_SELECTOR);
    verify(app, times(1)).tap(env.getPageElement());
  }
}
