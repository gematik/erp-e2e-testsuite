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

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.ListPageElement;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@RequiredArgsConstructor
public class HasSentDispReq implements Question<Boolean> {
  private final String oldMessagesButtonValue;
  private final String expectedTaskId;

  @Override
  public Boolean answeredBy(Actor actor) {
    val app = SafeAbility.getAbility(actor, UseIOSApp.class);

    // verify that Messages Button Badge got incremented
    // button value is something like "3 Objekte" -> get only the number
    val oldMessagesButtonNumber = Integer.parseInt(oldMessagesButtonValue.split(" ")[0]);
    val newMessagesButtonNumber =
        Integer.parseInt(app.getText(BottomNav.MESSAGES_BUTTON).split(" ")[0]);

    val isMessagesButtonIncremented = oldMessagesButtonNumber + 1 == newMessagesButtonNumber;

    // verify that message is shown
    app.tap(BottomNav.MESSAGES_BUTTON);

    // tap on latest message
    val messageElement = ListPageElement.forElement(MessageScreen.MESSAGES_LIST, 0);
    app.tap(messageElement);

    // tap on first prescription
    try {
      val prescriptionElement = ListPageElement.forElement(MessageScreen.PRESCRIPTION_LIST, 0);
      app.tap(prescriptionElement);
    } catch (Exception e) {
      app.logEvent(
          "Failed to tap the prescription(s) inside the message. Is the latest message a dispense"
              + " request?");
      return false;
    }

    // check if this has the same task id as the prescription from dispReq
    app.swipeIntoView(SwipeDirection.UP, PrescriptionDetails.TECHNICAL_INFORMATION);
    app.tap(PrescriptionDetails.TECHNICAL_INFORMATION);
    val actualTaskId = app.getText(PrescriptionTechnicalInformation.TASKID);
    app.tap(PrescriptionTechnicalInformation.BACK);

    // navigate back to the main screen
    app.tap(PrescriptionDetails.LEAVE_DETAILS_BUTTON);

    return actualTaskId.equals(expectedTaskId) && isMessagesButtonIncremented;
  }

  public static HasSentDispReq toPharmacy(String oldMessagesButtonValue, String expectedTaskId) {
    return new HasSentDispReq(oldMessagesButtonValue, expectedTaskId);
  }
}
