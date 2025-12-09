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

import static de.gematik.test.erezept.app.cfg.MedicationParser.compareMedicationNames;
import static de.gematik.test.erezept.app.cfg.MedicationParser.getMedicationName;
import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.Mainscreen;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.app.questions.MovingToPrescription;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@RequiredArgsConstructor
public class EnsureThatThePharmaceuticalPrescription implements Task {

  private final DequeStrategy deque;

  @Override
  @Step("{0} überprüft die Darstellung von dem #deque ausgestellten E-Rezept")
  public <T extends Actor> void performAs(T actor) {
    actor.attemptsTo(EnsureTheCorrectProfile.isChosen());

    val app = SafeAbility.getAbility(actor, UseTheApp.class);
    val dmcAbility = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = deque.chooseFrom(dmcAbility.chooseStack(DmcStack.ACTIVE));

    app.logEvent(
        format(
            "{0} überprüft die Darstellung von dem {1} ausgestellten E-Rezept",
            actor.getName(), deque));
    app.tap(Mainscreen.REFRESH_BUTTON);

    val prescriptionBundle =
        actor
            .asksFor(MovingToPrescription.withTaskId(dmc.getTaskId()))
            .orElseThrow(
                () ->
                    new MissingPreconditionError(
                        format("Prescription with TaskID {0} was not found", dmc.getTaskId())));

    val kbvBundle =
        prescriptionBundle
            .getKbvBundle()
            .orElseThrow(
                () ->
                    new MissingFieldException(
                        ErxPrescriptionBundle.class, KbvItaErpStructDef.BUNDLE));

    val medication = kbvBundle.getMedication();
    val medicationRequest = kbvBundle.getMedicationRequest();

    app.swipeIntoView(SwipeDirection.DOWN, PrescriptionDetails.PRESCRIPTION_TITLE);

    val actualMedicationName = app.getText(PrescriptionDetails.PRESCRIPTION_TITLE);
    val expectedMedicationName = getMedicationName(medication);

    assertTrue(
        compareMedicationNames(expectedMedicationName, actualMedicationName),
        String.format(
            "Medication names were not equal. Name from FD: %s, name from app: %s.",
            expectedMedicationName, actualMedicationName));

    if (kbvBundle.getFlowType().isDirectAssignment()) {
      assertTrue(
          app.isPresent(PrescriptionDetails.DIRECT_ASSIGNMENT_BADGE),
          "Missing 'Direktzuweisung'-Label");
    } else {
      actor.attemptsTo(VerifyStatusInfo.forInput(prescriptionBundle));
    }

    val expectedZuzahlung =
        medicationRequest
            .getCoPaymentStatus()
            .map(
                status ->
                    switch (status) {
                      case STATUS_0 -> "Ja";
                      case STATUS_1 -> "Nein";
                      case STATUS_2 -> "Teilweise";
                    })
            .orElse("Keine Angabe");
    val actualZuzahlung = app.getText(PrescriptionDetails.PRESCRIPTION_ADDITIONAL_PAYMENT);
    assertEquals(expectedZuzahlung, actualZuzahlung, "Expected Zuzahlung to be");

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

    public EnsureThatThePharmaceuticalPrescription isShownCorrectly() {
      return Instrumented.instanceOf(EnsureThatThePharmaceuticalPrescription.class)
          .withProperties(deque);
    }
  }
}
