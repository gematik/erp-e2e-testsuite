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
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.ScrollDirection;
import de.gematik.test.erezept.app.mobile.elements.Mainscreen;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.app.questions.MovingToPrescription;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
public class EnsureThatThePrescription implements Task {

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

    app.scrollIntoView(ScrollDirection.UP, PrescriptionDetails.PRESCRIPTION_TITLE);
    val actualTitle = app.getText(PrescriptionDetails.PRESCRIPTION_TITLE);
    assertEquals(medication.getMedicationName(), actualTitle);

    if (!kbvBundle.getFlowType().isDirectAssignment()) {
      // direct assignments have no validity information because these are theoretically already
      // assigned to a pharmacy
      val expectedValidityText =
          this.calculateValidityText(prescriptionBundle.getTask(), kbvBundle);
      val actualValidityText = app.getText(PrescriptionDetails.PRESCRIPTION_VALIDITY_TEXT);
      assertEquals(expectedValidityText, actualValidityText);
    } else {
      assertTrue(
          app.isPresent(PrescriptionDetails.DIRECT_ASSIGNMENT_BADGE),
          "Missing 'Direktzuweisung'-Label");
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

  private String calculateValidityText(ErxTask task, KbvErpBundle kbvBundle) {
    val dc = DateConverter.getInstance();
    val now = LocalDate.now();

    LocalDate expiry;
    if (kbvBundle.isMultiple()) {
      expiry =
          kbvBundle
              .getMedicationRequest()
              .getMvoEnd()
              .map(dc::dateToLocalDate)
              .orElseGet(() -> dc.dateToLocalDate(task.getExpiryDate()));
    } else {
      expiry = dc.dateToLocalDate(task.getAcceptDate());
    }

    // Note: .minusDays(1) is required because the expiryDate is the first day when the prescription
    // is no longer valid!
    val remainingDays =
        Duration.between(now.atStartOfDay(), expiry.minusDays(1).atStartOfDay()).toDays();

    val start =
        kbvBundle
            .getMedicationRequest()
            .getMvoStart()
            .map(dc::dateToLocalDate)
            .orElse(dc.dateToLocalDate(task.getAuthoredOn()));

    if (start.isEqual(now) || start.isBefore(now)) {
      return format("Noch {0} Tage einlösbar", remainingDays);
    } else {
      return format("Einlösbar ab {0}", start.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
    }
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

    public EnsureThatThePrescription isShownCorrectly() {
      return Instrumented.instanceOf(EnsureThatThePrescription.class).withProperties(deque);
    }
  }
}
