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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.Mainscreen;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.app.questions.MovingToPrescription;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor
public class DeleteRedeemablePrescription implements Task {
  private static final DmcStack DMC_STACK = DmcStack.ACTIVE;
  private final DequeStrategy deque;

  @SuppressWarnings("java:S2201")
  @Override
  @Step("{0} löscht das #deque ausgestellte E-Rezept")
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseTheApp.class);
    val dmcAbility = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = deque.chooseFrom(dmcAbility.chooseStack(DMC_STACK));

    // first refresh the screen
    app.tap(Mainscreen.REFRESH_BUTTON);

    // java:S2201: return value not required here, because we are expecting the prescription to
    // exist, but don't need the prescriptionbundle to delete via app
    actor
        .asksFor(MovingToPrescription.withTaskId(dmc.getTaskId()))
        .orElseThrow(
            () ->
                new MissingPreconditionError(
                    format("Prescription with TaskID {0} was not found", dmc.getTaskId())));

    app.tap(PrescriptionDetails.DELETE_BUTTON_TOOLBAR);
    app.tap(PrescriptionDetails.DELETE_BUTTON_TOOLBAR_ITEM);
    app.acceptAlert();

    // move the dmc to the deleted stack
    deque.removeFrom(dmcAbility.getDmcs());
    dmcAbility.getDeletedDmcs().append(dmc);
  }

  public static DeleteRedeemablePrescription fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static DeleteRedeemablePrescription fromStack(DequeStrategy deque) {
    return Instrumented.instanceOf(DeleteRedeemablePrescription.class).withProperties(deque);
  }
}
