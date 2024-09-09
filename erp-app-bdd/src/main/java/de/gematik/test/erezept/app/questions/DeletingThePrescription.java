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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.app.mobile.elements.Settings;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DeletingThePrescription implements Question<Boolean> {
  private final DequeStrategy deque;

  @Override
  public Boolean answeredBy(Actor actor) {
    val driver = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);
    val dmcAbility = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = deque.chooseFrom(dmcAbility.chooseStack(DmcStack.ACTIVE));

    actor
        .asksFor(MovingToPrescription.withTaskId(dmc.getTaskId()))
        .orElseThrow(
            () ->
                new MissingPreconditionError(
                    format("Prescription with TaskID {0} was not found", dmc.getTaskId())));

    driver.tap(PrescriptionDetails.DELETE_BUTTON_TOOLBAR);
    driver.tap(PrescriptionDetails.DELETE_PRESCRIPTION_ITEM_BUTTON);

    val message = driver.isPresent(PrescriptionDetails.PRESCRIPTION_CANNOT_BE_DELETED_INFO);

    driver.acceptAlert();
    driver.tap(Settings.LEAVE_BUTTON);
    return message;
  }

  public static DeletingThePrescription canNotBePerformedFor(String order) {
    return canNotBePerformedFor(DequeStrategy.fromString(order));
  }

  public static DeletingThePrescription canNotBePerformedFor(DequeStrategy deque) {
    return new DeletingThePrescription(deque);
  }
}
