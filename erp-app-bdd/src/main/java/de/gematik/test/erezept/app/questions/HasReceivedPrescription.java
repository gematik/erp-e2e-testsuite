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

package de.gematik.test.erezept.app.questions;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.questions.android.HasReceivedPrescriptionOnAndroid;
import de.gematik.test.erezept.app.questions.ios.HasReceivedPrescriptionOnIos;
import de.gematik.test.erezept.app.task.PlatformScreenplayUtil;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class HasReceivedPrescription implements Question<Boolean> {
  @Override
  public Boolean answeredBy(Actor actor) {
    val driverAbility = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);
    val platformTask =
        PlatformScreenplayUtil.chooseQuestionForPlatform(
            driverAbility.getPlatformType(),
            HasReceivedPrescriptionOnAndroid::new,
            HasReceivedPrescriptionOnIos::new);

    return platformTask.answeredBy(actor);
  }

  // TODO: refactor name of this factory-method
  public static Question<Boolean> withSomeStrategy() {
    return new HasReceivedPrescription();
  }
}
