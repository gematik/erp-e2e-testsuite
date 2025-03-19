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
 */

package de.gematik.test.erezept.app.task.ios;

import static java.text.MessageFormat.format;
import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.elements.Mainscreen;
import de.gematik.test.erezept.app.mobile.elements.PharmacyDetailScreen;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.app.mobile.elements.Utility;
import de.gematik.test.erezept.app.questions.MovingToPrescription;
import de.gematik.test.erezept.app.task.EnsureTheCorrectProfile;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.ExchangedCommunication;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@RequiredArgsConstructor
public class AssignPrescriptionToPharmacyOnIos implements Task {

  private final DequeStrategy deque;
  private final Actor pharmacy;

  @Override
  public <T extends Actor> void performAs(T actor) {
    actor.attemptsTo(EnsureTheCorrectProfile.isChosen());

    val app = SafeAbility.getAbility(actor, UseIOSApp.class);
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

    app.tap(PrescriptionDetails.ASSIGN_TO_PHARMACY_BUTTON);

    when(actor).attemptsTo(OpenPharmacyViaSearchOnIos.named(pharmacy).fromPharmacySearchscreen());
    app.tap(PharmacyDetailScreen.PHARMACY_PICKUP_BUTTON);
    app.tap(PharmacyDetailScreen.REDEEM_ORDER_BUTTON);

    // this is required to wait until the prescription is redeemed and "Erfolgreich eingelöst" label
    // is shown
    app.waitUntilElementIsPresent(PharmacyDetailScreen.SUCCESSFULLY_REDEEMED_LABEL);
    // and now ensure "Zur Startseite" is not stale
    // StaleElementReferenceException results from BACK_TO_HOME and REDEEM_ORDER_BUTTON having the
    // same identifier
    app.waitUntilElementIsVisible(PharmacyDetailScreen.BACK_TO_HOME);
    app.tap(PharmacyDetailScreen.BACK_TO_HOME);

    app.tapIfDisplayed(Utility.LATER); // rate on app-store later

    // now put the expected communication to the stack of the pharmacy
    val communications = SafeAbility.getAbility(pharmacy, ManageCommunications.class);
    communications
        .getExpectedCommunications()
        .append(
            ExchangedCommunication.sentBy(actor)
                .to(pharmacy)
                .dispenseRequestBasedOn(prescriptionBundle.getTask().getPrescriptionId()));
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

    public AssignPrescriptionToPharmacyOnIos toPharmacy(Actor pharmacy) {
      return new AssignPrescriptionToPharmacyOnIos(deque, pharmacy);
    }
  }
}
