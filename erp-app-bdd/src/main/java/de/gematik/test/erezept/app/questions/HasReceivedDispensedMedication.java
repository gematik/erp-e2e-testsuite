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

package de.gematik.test.erezept.app.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.ScrollDirection;
import de.gematik.test.erezept.app.mobile.elements.Mainscreen;
import de.gematik.test.erezept.app.mobile.elements.MedicationDispenseDetails;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.client.usecases.MedicationDispenseSearchCommand;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@RequiredArgsConstructor
public class HasReceivedDispensedMedication implements Question<Boolean> {

  private final DequeStrategy deque;

  @Override
  public Boolean answeredBy(Actor actor) {
    val app = SafeAbility.getAbility(actor, UseIOSApp.class);
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val receivedDrugs = SafeAbility.getAbility(actor, ReceiveDispensedDrugs.class);
    val prescriptionId = deque.chooseFrom(receivedDrugs.getDispensedDrugsList());

    // first refresh the screen
    app.tap(Mainscreen.REFRESH_BUTTON);
    app.scrollIntoView(ScrollDirection.DOWN, Mainscreen.PRESCRIPTION_ARCHIVE);
    app.tap(Mainscreen.PRESCRIPTION_ARCHIVE);
    actor
        .asksFor(MovingToPrescription.withTaskId(prescriptionId.getValue()))
        .orElseThrow(
            () ->
                new MissingPreconditionError(
                    format("Unable to find Prescription {0} in Archive", prescriptionId)));

    // open the medication overview
    app.tap(PrescriptionDetails.PRESCRIPTION_MEDICATION);

    val medReq =
        erpClient
            .request(new MedicationDispenseSearchCommand(prescriptionId))
            .getResourceOptional()
            .orElseThrow(
                () ->
                    new MissingPreconditionError(
                        format(
                            "Unable to find MedicationDispense {0} in Backend", prescriptionId)));
    val sizeFromBackend = medReq.getMedicationDispenses().size();
    val sizeFromApp = app.getWebElementListLen(MedicationDispenseDetails.DISPENSED);

    return sizeFromBackend == sizeFromApp;
  }

  public static Question<Boolean> fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static Question<Boolean> fromStack(DequeStrategy deque) {
    return new HasReceivedDispensedMedication(deque);
  }
}
