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

package de.gematik.test.erezept.app.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.app.task.EnsureTheCorrectProfile;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@RequiredArgsConstructor
public class MovingToEVDGAPrescription implements Question<ErxPrescriptionBundle> {

  private final DequeStrategy deque;

  @Override
  @Step("{0} navigiert zu dem E-Rezept #taskId")
  public ErxPrescriptionBundle answeredBy(Actor actor) {
    actor.attemptsTo(EnsureTheCorrectProfile.isChosen());

    val app = SafeAbility.getAbility(actor, UseIOSApp.class);
    val dmcAbility = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = deque.chooseFrom(dmcAbility.chooseStack(DmcStack.ACTIVE));

    // Wir befinden uns auf dem Mainscreen und stellen sicher, das die DIGA heruntergeladen ist
    app.tap(Mainscreen.REFRESH_BUTTON);

    // finde die DIGA
    app.logEvent(
        format("{0} sucht nach dem {1} ausgestellten EVDGA-Rezept", actor.getName(), deque));
    // navigiere zum DIGA Detailscreen
    return actor
        .asksFor(MovingToPrescription.withTaskId(dmc.getTaskId()))
        .orElseThrow(
            () ->
                new MissingPreconditionError(
                    format("DIGA with TaskID {0} was not found", dmc.getTaskId())));
  }

  public static MovingToEVDGAPrescription with(DequeStrategy deque) {
    return Instrumented.instanceOf(MovingToEVDGAPrescription.class).withProperties(deque);
  }
}
