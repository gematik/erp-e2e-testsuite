/*
 * Copyright (c) 2022 gematik GmbH
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.app.abilities.UseAndroidApp;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.cfg.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.Onboarding;
import de.gematik.test.erezept.exceptions.FeatureNotImplementedException;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SkipOnboardingTest {

  private final String androidUserName = "Alice";
  private UseAndroidApp androidAbility;
  private final String iosUserName = "Bob";
  private UseIOSApp iosAbility; // will be required later, once the feature is implemented on iOS

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
    androidAbility = mock(UseAndroidApp.class);
    when(androidAbility.getPlatformType()).thenReturn(PlatformType.ANDROID);

    iosAbility = mock(UseIOSApp.class);
    when(iosAbility.getPlatformType()).thenReturn(PlatformType.IOS);

    // assemble the screenplay
    val aliceAndroid = OnStage.theActorCalled(androidUserName);
    givenThat(aliceAndroid).can(androidAbility);

    val bobIos = OnStage.theActorCalled(iosUserName);
    givenThat(bobIos).can(iosAbility);
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldProxyToAndroidWithSwipes() {
    val theAppUser = OnStage.theActorCalled(androidUserName);

    theAppUser.attemptsTo(SkipOnboarding.bySwiping());
    verify(androidAbility, times(4)).swipe(any());
    verify(androidAbility, times(1)).tap(Onboarding.SKIP);
  }

  @Test
  void shouldProxyToAndroidWithoutSwipes() {
    val theAppUser = OnStage.theActorCalled(androidUserName);

    theAppUser.attemptsTo(SkipOnboarding.directly());
    verify(androidAbility, times(0)).swipe(any());
    verify(androidAbility, times(1)).tap(Onboarding.SKIP);
  }

  @Test
  void shouldProxyToIosWithSwipes() {
    val theAppUser = OnStage.theActorCalled(iosUserName);
    val task = SkipOnboarding.bySwiping();
    assertThrows(FeatureNotImplementedException.class, () -> task.performAs(theAppUser));
  }

  @Test
  void shouldProxyToIosWithoutSwipes() {
    val theAppUser = OnStage.theActorCalled(iosUserName);
    val task = SkipOnboarding.bySwiping();
    assertThrows(FeatureNotImplementedException.class, () -> task.performAs(theAppUser));
  }
}
