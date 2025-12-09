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

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.task.ios.NavigateThroughOnboardingOnIOS;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NavigateThroughOnboarding implements Task {

  private final Supplier<Task> androidTaskSupplier;
  private final Supplier<Task> iOSTaskSupplier;

  @Override
  public <T extends Actor> void performAs(final T actor) {
    val driverAbility = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);
    val platformTask =
        PlatformScreenplayUtil.chooseTaskForPlatform(
            driverAbility.getPlatformType(), androidTaskSupplier, iOSTaskSupplier);
    platformTask.performAs(actor);
  }

  public static NavigateThroughOnboarding entirely() {
    return new NavigateThroughOnboarding(() -> null, NavigateThroughOnboardingOnIOS::entirely);
  }
}
