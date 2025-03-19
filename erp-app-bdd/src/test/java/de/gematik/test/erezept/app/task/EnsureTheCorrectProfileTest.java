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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.ProfileSelectorElement;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnsureTheCorrectProfileTest {

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
  void shouldNotTapAnythingIfProfileIsSelected() {
    val task = EnsureTheCorrectProfile.isChosen();
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);
    when(app.getCurrentUserProfile()).thenReturn(userName);

    assertDoesNotThrow(() -> actor.attemptsTo(task));

    verify(app, times(0)).tap(any(ProfileSelectorElement.class));
    verify(app, times(0)).setCurrentUserProfile(anyString());
  }

  @Test
  void shouldSwitchProfileIfProfileIsNotSelected() {
    val task = EnsureTheCorrectProfile.isChosen();
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);
    when(app.getCurrentUserProfile()).thenReturn("random user profile name");

    assertDoesNotThrow(() -> actor.attemptsTo(task));

    verify(app, times(1)).tap(any(ProfileSelectorElement.class));
    verify(app, times(1)).setCurrentUserProfile(anyString());
  }
}
