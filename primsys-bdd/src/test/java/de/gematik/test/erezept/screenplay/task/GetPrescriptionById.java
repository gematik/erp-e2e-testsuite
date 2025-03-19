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

package de.gematik.test.erezept.screenplay.task;

import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.questions.ResponseOfGetTaskById;
import de.gematik.test.erezept.screenplay.questions.VerifyDocumentResponse;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.ensure.Ensure;

public class GetPrescriptionById implements Task {

  private final DequeStrategy deque;

  public GetPrescriptionById(DequeStrategy deque) {
    this.deque = deque;
  }

  public static GetPrescriptionById asPharmacy(String deque) {
    return new GetPrescriptionById(DequeStrategy.fromString(deque));
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);

    val response = actor.asksFor(ResponseOfGetTaskById.asPharmacy(deque));
    val acceptBundle = response.getExpectedResource();

    // Note: A_24179 on "erneuter Abruf Verordnung" the FD does not send the AccessCode again
    val dmc = deque.chooseFrom(prescriptionManager.getAssignedList());
    acceptBundle.getTask().addIdentifier(dmc.getAccessCode().asIdentifier());

    actor.attemptsTo(
        Ensure.that(
                "the verifyDocument for the signed KBV-Bundle",
                VerifyDocumentResponse.forGivenDocument(acceptBundle.getSignedKbvBundle()))
            .isTrue());

    prescriptionManager.appendAcceptedPrescription(acceptBundle);
  }
}
