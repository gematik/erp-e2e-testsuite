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

package de.gematik.test.erezept.remotefdv.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.remotefdv.abilities.UseTheRemoteFdVClient;
import de.gematik.test.erezept.remotefdv.client.PatientRequests;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@RequiredArgsConstructor
public class DeletingThePrescription implements Question<Boolean> {
  private final DequeStrategy deque;

  @Override
  @Step("{0} überprüft ob die Prescription gelöscht wurde")
  public Boolean answeredBy(Actor actor) {
    val client = SafeAbility.getAbility(actor, UseTheRemoteFdVClient.class);
    val dmcAbility = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = deque.chooseFrom(dmcAbility.chooseStack(DmcStack.ACTIVE));
    val prescriptionId = dmc.getTaskId();
    boolean wasDeleted = false;

    actor
        .asksFor(ReadPrescription.withTaskId(dmc.getTaskId()))
        .orElseThrow(
            () ->
                new MissingPreconditionError(
                    format("Prescription with TaskID {0} was not found", dmc.getTaskId())));

    val response =
        client.sendRequest(PatientRequests.deletePrescriptionById(prescriptionId.getValue()));
    if (response.getResourceOptional().isPresent()) {
      wasDeleted = true;
    }
    return wasDeleted;
  }

  public static DeletingThePrescription wasSuccessful(String order) {
    return wasSuccessful(DequeStrategy.fromString(order));
  }

  public static DeletingThePrescription wasSuccessful(DequeStrategy deque) {
    return new DeletingThePrescription(deque);
  }
}
