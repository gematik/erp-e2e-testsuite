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

package de.gematik.test.erezept.screenplay.task;

import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.questions.ResponseOfGetTaskById;
import de.gematik.test.erezept.screenplay.strategy.ActorRole;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

public class GetPrescriptionById implements Task {
  private final ActorRole role;
  private final DequeStrategy deque;

  public GetPrescriptionById(ActorRole role, DequeStrategy deque) {
    this.role = role;
    this.deque = deque;
  }

  public static GetPrescriptionById asPharmacy(String deque) {
    return new GetPrescriptionById(ActorRole.PHARMACY, DequeStrategy.fromString(deque));
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val acceptBundleResp = actor.asksFor(ResponseOfGetTaskById.withActorRole(role, deque));
    val erxAcceptBundle = acceptBundleResp.getExpectedResource();
    val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);

    // toDo reduce after check if ErxTaskBundle and ErxAcceptBundle AND ErxAcceptBundle from
    // get/Task/{id}?=ac "TaskGetByIdAsAcceptBundleCommand()" are similar
    val dmc = deque.chooseFrom(prescriptionManager.getAssignedList());
    erxAcceptBundle.getTask().addIdentifier(dmc.getAccessCode().asIdentifier());

    val konnektor = SafeAbility.getAbility(actor, UseTheKonnektor.class);
    konnektor.verifyDocument(erxAcceptBundle.getSignedKbvBundle());
    prescriptionManager.appendAcceptedPrescription(erxAcceptBundle);
  }
}
