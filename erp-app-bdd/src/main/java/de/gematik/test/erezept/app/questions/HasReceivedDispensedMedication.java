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
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.Mainscreen;
import de.gematik.test.erezept.app.mobile.elements.MedicationDispenseDetails;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.client.usecases.MedicationDispenseSearchByIdCommand;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@RequiredArgsConstructor
public class HasReceivedDispensedMedication implements Question<Boolean> {

  private final DequeStrategy deque;

  public static Question<Boolean> fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static Question<Boolean> fromStack(DequeStrategy deque) {
    return new HasReceivedDispensedMedication(deque);
  }

  @Override
  public Boolean answeredBy(Actor actor) {
    val app = SafeAbility.getAbility(actor, UseIOSApp.class);
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val receivedDrugs = SafeAbility.getAbility(actor, ReceiveDispensedDrugs.class);
    val dispensationInformation = deque.chooseFrom(receivedDrugs.getDispensedDrugsList());
    val prescriptionId = dispensationInformation.prescriptionId();

    // first refresh the screen
    app.tap(Mainscreen.REFRESH_BUTTON);

    // and after that make only sure we have reached the archive button
    app.swipeIntoView(SwipeDirection.UP, Mainscreen.PRESCRIPTION_ARCHIVE);
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
            .request(new MedicationDispenseSearchByIdCommand(prescriptionId))
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
}
