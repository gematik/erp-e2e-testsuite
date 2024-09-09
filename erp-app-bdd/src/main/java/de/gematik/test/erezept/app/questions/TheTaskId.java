/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.app.questions;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.app.mobile.elements.Receipt;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TheTaskId implements Question<String> {

  @Override
  public String answeredBy(Actor actor) {
    val driverAbility = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);

    driverAbility.tap(Receipt.PRESCRIPTION_STATUS_LABEL);
    driverAbility.tap(Receipt.TECHNICAL_INFORMATION);
    val screenTaskId = driverAbility.getText(Receipt.TASKID);
    driverAbility.tap(Receipt.GOBACK_TO_DETAILS);
    driverAbility.tap(PrescriptionDetails.LEAVE_DETAILS_BUTTON);
    log.info("receipt found, retrieving taskId: " + screenTaskId);
    return screenTaskId;
  }

  public static TheTaskId ofTheLastPrescriptionInTheMainScreen() {
    return new TheTaskId();
  }
}
