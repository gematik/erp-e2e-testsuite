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
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.Mainscreen;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.app.questions.MovingToPrescription;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor
public class EnsureThatThePrescriptionValidity implements Task {

  private final DequeStrategy deque;
  private final int remainingDays;

  @SuppressWarnings("java:S2201")
  @Override
  @Step(
      "{0} überprüft, dass das #deque ausgestellte E-Rezept noch #remainingDays Tage einlösbar ist")
  public <T extends Actor> void performAs(T actor) {
    actor.attemptsTo(EnsureTheCorrectProfile.isChosen());

    val app = SafeAbility.getAbility(actor, UseTheApp.class);
    val dmcAbility = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = deque.chooseFrom(dmcAbility.chooseStack(DmcStack.ACTIVE));

    // first refresh the screen
    app.tap(Mainscreen.REFRESH_BUTTON);
    actor
        .asksFor(MovingToPrescription.withTaskId(dmc.getTaskId()))
        .orElseThrow(
            () ->
                new MissingPreconditionError(
                    format("Prescription with TaskID {0} was not found", dmc.getTaskId())));

    val expectedValidityText = format("Noch {0} Tage einlösbar", remainingDays);
    val actualValidityText = app.getText(PrescriptionDetails.PRESCRIPTION_VALIDITY_TEXT);
    assertEquals(expectedValidityText, actualValidityText);

    // leave prescription details and go back to the main screen
    app.tap(PrescriptionDetails.LEAVE_DETAILS_BUTTON);
  }

  public static Builder fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static Builder fromStack(DequeStrategy deque) {
    return new Builder(deque);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final DequeStrategy deque;

    public EnsureThatThePrescriptionValidity isStillValidForRemainingDays(int remainingDays) {
      return Instrumented.instanceOf(EnsureThatThePrescriptionValidity.class)
          .withProperties(deque, remainingDays);
    }
  }
}
