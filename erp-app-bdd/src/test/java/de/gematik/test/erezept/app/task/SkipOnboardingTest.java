/*
 * Copyright 2023 gematik GmbH
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.app.abilities.UseAndroidApp;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.Onboarding;
import de.gematik.test.erezept.exceptions.FeatureNotImplementedException;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SkipOnboardingTest {

  private String androidUserName;
  private String iosUserName;


  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
    val androidAbility = mock(UseAndroidApp.class);
    when(androidAbility.getPlatformType()).thenReturn(PlatformType.ANDROID);

    val iosAbility = mock(UseIOSApp.class);
    when(iosAbility.getPlatformType()).thenReturn(PlatformType.IOS);

    // assemble the screenplay
    androidUserName = GemFaker.fakerName();
    val aliceAndroid = OnStage.theActorCalled(androidUserName);
    givenThat(aliceAndroid).can(androidAbility);

    iosUserName = GemFaker.fakerName();
    val bobIos = OnStage.theActorCalled(iosUserName);
    givenThat(bobIos).can(iosAbility);
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldProxyToAndroidWithSwipes() {
    val actor = OnStage.theActorCalled(androidUserName);
    val app = actor.abilityTo(UseAndroidApp.class);
    
    actor.attemptsTo(SkipOnboarding.bySwiping());
    verify(app, times(4)).swipe(any());
    verify(app, times(1)).tap(Onboarding.SKIP_BUTTON);
  }

  @Test
  void shouldProxyToAndroidWithoutSwipes() {
    val actor = OnStage.theActorCalled(androidUserName);
    val app = actor.abilityTo(UseAndroidApp.class);
    
    actor.attemptsTo(SkipOnboarding.directly());
    verify(app, times(0)).swipe(any());
    verify(app, times(1)).tap(Onboarding.SKIP_BUTTON);
  }

  @Test
  void shouldProxyToIosWithSwipes() {
    val actor = OnStage.theActorCalled(iosUserName);
    val task = SkipOnboarding.bySwiping();
    assertThrows(FeatureNotImplementedException.class, () -> task.performAs(actor));
  }

  @Test
  void shouldProxyToIosWithoutSwipes() {
    val actor = OnStage.theActorCalled(iosUserName);
    val task = SkipOnboarding.bySwiping();
    assertThrows(FeatureNotImplementedException.class, () -> task.performAs(actor));
  }
}
