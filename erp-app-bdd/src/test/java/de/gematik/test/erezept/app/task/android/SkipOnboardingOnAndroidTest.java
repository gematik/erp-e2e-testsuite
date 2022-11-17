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

package de.gematik.test.erezept.app.task.android;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.app.abilities.UseAndroidApp;
import de.gematik.test.erezept.app.cfg.PlatformType;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.Onboarding;
import java.util.List;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SkipOnboardingOnAndroidTest {

  private UseAndroidApp appAbility;

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
    appAbility = mock(UseAndroidApp.class);
    when(appAbility.getPlatformType()).thenReturn(PlatformType.ANDROID);

    // assemble the screenplay
    val userName = "Alice";
    val theAppUser = OnStage.theActorCalled(userName);
    givenThat(theAppUser).can(appAbility);
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldSwipeAndTapToSkipOnboarding() {
    val theAppUser = OnStage.theActorInTheSpotlight();
    val swipeDirections =
        List.of(SwipeDirection.DOWN, SwipeDirection.UP, SwipeDirection.LEFT, SwipeDirection.RIGHT);
    val skipOnboardong = new SkipOnboardingOnAndroid(swipeDirections);

    theAppUser.attemptsTo(skipOnboardong);
    verify(appAbility, times(swipeDirections.size())).swipe(any());
    verify(appAbility, times(1)).tap(Onboarding.SKIP);
  }
}