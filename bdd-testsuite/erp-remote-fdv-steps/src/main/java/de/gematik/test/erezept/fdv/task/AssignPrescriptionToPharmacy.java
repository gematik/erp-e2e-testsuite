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

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.fdv.abilities.UseTheRemoteFdVClient;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.remotefdv.client.PatientRequests;
import de.gematik.test.erezept.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.ExchangedCommunication;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor
public class AssignPrescriptionToPharmacy implements Task {
  private final DequeStrategy deque;
  private final Actor pharmacy;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val client = SafeAbility.getAbility(actor, UseTheRemoteFdVClient.class);
    val dmcAbility = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);

    val useSmcb = SafeAbility.getAbility(pharmacy, UseSMCB.class);
    val dmc = deque.chooseFrom(dmcAbility.chooseStack(DmcStack.ACTIVE));
    val telematikId = useSmcb.getTelematikID();

    val communication =
        client.sendRequest(
            PatientRequests.assignToPharmacy(dmc.getTaskId().getValue(), telematikId, "delivery"));
    assertEquals(dmc.getTaskId().getValue(), communication.getExpectedResource().getReference());

    val communications = SafeAbility.getAbility(pharmacy, ManageCommunications.class);
    communications
        .getExpectedCommunications()
        .append(
            ExchangedCommunication.sentBy(actor)
                .to(pharmacy)
                .dispenseRequestBasedOn(PrescriptionId.from(dmc.getTaskId())));
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

    public AssignPrescriptionToPharmacy toPharmacy(Actor pharmacy) {
      return new AssignPrescriptionToPharmacy(deque, pharmacy);
    }
  }
}
