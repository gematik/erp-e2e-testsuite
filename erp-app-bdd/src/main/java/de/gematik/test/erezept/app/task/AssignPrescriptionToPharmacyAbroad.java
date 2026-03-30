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

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.EUFlowElements;
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.mobile.elements.Mainscreen;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.app.questions.MovingToPrescription;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.questions.ResponseOfGetTaskById;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor
public class AssignPrescriptionToPharmacyAbroad implements Task {
  private final String country;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseIOSApp.class);
    val dmcAbility = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = DequeStrategy.FIFO.chooseFrom(dmcAbility.chooseStack(DmcStack.ACTIVE));

    app.logEvent(
        format(
            "{0} überprüft die Darstellung von dem {1} ausgestellten E-Rezept",
            actor.getName(), DequeStrategy.FIFO));
    app.tap(BottomNav.PRESCRIPTION_BUTTON);
    app.tap(Mainscreen.REFRESH_BUTTON);
    // Note: wait 60s to load the newly created prescription
    app.longPauseApp();

    val prescriptionBundle =
        actor
            .asksFor(MovingToPrescription.withTaskId(dmc.getTaskId()))
            .orElseThrow(
                () ->
                    new MissingPreconditionError(
                        format("Prescription with TaskID {0} was not found", dmc.getTaskId())));

    app.tap(PrescriptionDetails.PRESCRIPTION_DETAILS_TOOLBAR);
    app.tap(PrescriptionDetails.START_EU_REDEEM_TOOLBAR_ITEM);
    app.tap(EUFlowElements.GRANT_EU_CONSENT_BUTTON);
    app.tap(EUFlowElements.SELECT_PRESCRIPTIONS_BUTTON);

    // Patch the prescription to be eu redeemable
    val prescriptionName =
        prescriptionBundle.getKbvBundle().orElseThrow().getMedication().getMedicationName();
    val prescriptionEntry = EUFlowElements.forPrescriptionName(prescriptionName);
    app.tap(prescriptionEntry);
    app.tap(EUFlowElements.SELECT_PRESCRIPTIONS_SCREEN_LEAVE_BUTTON);

    // Verify that the prescription is marked as EU redeemable on FD
    val erxTask =
        actor
            .asksFor(ResponseOfGetTaskById.asPatient(DequeStrategy.FIFO))
            .getExpectedResource()
            .getTask();
    assertTrue(
        erxTask.getRedeemableByProperties(),
        "Prescription is not marked as EU redeemable on FD, but should be.");

    // Select the country
    app.tap(EUFlowElements.SELECT_COUNTRY_BUTTON);
    app.input(country, EUFlowElements.COUNTRY_SEARCH_BAR_TEXT_FIELD);
    val countryEntry = EUFlowElements.forCountryName(country);
    app.tap(countryEntry);

    app.tap(EUFlowElements.EU_REDEEM_BUTTON);
    app.tap(EUFlowElements.GENERATE_EU_REDEEM_CODE_BUTTON);

    actor.attemptsTo(EnsureThatTheEUInformation.isDisplayedCorrectly());

    app.tap(BottomNav.PRESCRIPTION_BUTTON);
  }

  public static AssignPrescriptionToPharmacyAbroad in(String country) {
    return new AssignPrescriptionToPharmacyAbroad(country);
  }
}
