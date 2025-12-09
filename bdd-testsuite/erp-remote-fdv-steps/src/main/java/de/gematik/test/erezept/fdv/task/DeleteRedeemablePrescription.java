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

package de.gematik.test.erezept.fdv.task;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.erezept.remotefdv.api.model.Prescription;
import de.gematik.test.erezept.fdv.abilities.UseTheRemoteFdVClient;
import de.gematik.test.erezept.fdv.questions.ReadPrescription;
import de.gematik.test.erezept.remotefdv.client.PatientRequests;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor
public class DeleteRedeemablePrescription implements Task {
  private static final DmcStack DMC_STACK = DmcStack.ACTIVE;
  private final DequeStrategy deque;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val client = SafeAbility.getAbility(actor, UseTheRemoteFdVClient.class);
    val dmcAbility = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = deque.chooseFrom(dmcAbility.chooseStack(DMC_STACK));

    val response = actor.asksFor(ReadPrescription.withTaskId(dmc.getTaskId()));
    val optionalPres = response.getResourceOptional();
    assertTrue(optionalPres.isPresent());
    val prescription = optionalPres.get();

    assertNotEquals(Prescription.StatusEnum.CANCELLED, prescription.getStatus());

    client.sendRequest(PatientRequests.deletePrescriptionById(dmc.getTaskId().getValue()));
    val deletedPres =
        actor.asksFor(ReadPrescription.withTaskId(dmc.getTaskId())).getResourceOptional();
    assertTrue(deletedPres.isEmpty());

    dmcAbility.moveToDeleted(dmc);
  }

  public static DeleteRedeemablePrescription fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static DeleteRedeemablePrescription fromStack(DequeStrategy deque) {
    return Instrumented.instanceOf(DeleteRedeemablePrescription.class).withProperties(deque);
  }
}
