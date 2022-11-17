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

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.task.android.SetUpAndroidDevice;
import de.gematik.test.erezept.app.task.ios.SetUpIosDevice;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SetUpDevice implements Task {

  private final VersicherungsArtDeBasis insuranceKind;

  @Override
  public <T extends Actor> void performAs(final T actor) {
    val driverAbility = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);
    val platformTask =
        PlatformScreenplayUtil.chooseTaskForPlatform(
            driverAbility.getPlatformType(),
            () -> new SetUpAndroidDevice(insuranceKind),
            () -> new SetUpIosDevice(insuranceKind));
    platformTask.performAs(actor);
  }

  public static SetUpDevice withInsuranceType(String insuranceKind) {
    return withInsuranceType(VersicherungsArtDeBasis.fromCode(insuranceKind));
  }

  public static SetUpDevice withInsuranceType(VersicherungsArtDeBasis insuranceKind) {
    return new SetUpDevice(insuranceKind);
  }
}
