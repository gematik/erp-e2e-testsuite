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
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.questions.ResponseOfAcceptOperation;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
public class AcceptPrescription implements Task {

  private final DequeStrategy strategy;

  private AcceptPrescription(DequeStrategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public <T extends Actor> void performAs(final T actor) {
    val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val konnektor = SafeAbility.getAbility(actor, UseTheKonnektor.class);

    val prescriptionToAccept = this.strategy.chooseFrom(prescriptionManager.getAssignedList());

    val acceptedResponse =
        actor.asksFor(ResponseOfAcceptOperation.forPrescription(prescriptionToAccept));

    val acceptedTask = acceptedResponse.getExpectedResource();
    konnektor.verifyDocument(acceptedTask.getSignedKbvBundle());
    prescriptionManager.appendAcceptedPrescription(acceptedTask);
  }

  public static AcceptPrescription fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static AcceptPrescription fromStack(DequeStrategy dequeue) {
    return new AcceptPrescription(dequeue);
  }
}
