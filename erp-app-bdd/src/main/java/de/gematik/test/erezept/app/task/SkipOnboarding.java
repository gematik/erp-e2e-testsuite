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

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.task.android.SkipOnboardingOnAndroid;
import de.gematik.test.erezept.app.task.ios.SkipOnboardingOnIOS;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SkipOnboarding implements Task {

  private final List<SwipeDirection> swipeDirections;

  @Override
  public <T extends Actor> void performAs(final T actor) {
    val driverAbility = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);
    val platformTask =
        PlatformScreenplayUtil.chooseTaskForPlatform(
            driverAbility.getPlatformType(),
            () -> new SkipOnboardingOnAndroid(swipeDirections),
            () -> new SkipOnboardingOnIOS(swipeDirections));
    platformTask.performAs(actor);
  }

  public static SkipOnboarding directly() {
    return new SkipOnboarding(List.of());
  }

  public static SkipOnboarding bySwiping() {
    List<SwipeDirection> swipeDirections = new ArrayList<>();
    swipeDirections.add(SwipeDirection.RIGHT);
    swipeDirections.add(SwipeDirection.RIGHT);
    swipeDirections.add(SwipeDirection.LEFT);
    swipeDirections.add(SwipeDirection.LEFT);
    return new SkipOnboarding(swipeDirections);
  }
}
