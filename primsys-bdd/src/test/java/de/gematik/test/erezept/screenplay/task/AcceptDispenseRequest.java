/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.resources.erp.CommunicationType;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.questions.GetReceivedCommunication;
import de.gematik.test.erezept.screenplay.questions.ResponseOfAcceptOperation;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategyEnum;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AcceptDispenseRequest implements Task {

  private final DequeStrategyEnum order;
  private final Actor sender;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val konnektor = SafeAbility.getAbility(actor, UseTheKonnektor.class);
    val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);

    val receivedDispenseRequest =
        actor
            .asksFor(GetReceivedCommunication.dispenseRequest().of(order).from(sender))
            .orElseThrow(
                () ->
                    new MissingPreconditionError(
                        format(
                            "No {0} received from {1}",
                            CommunicationType.DISP_REQ, sender.getName())));

    val responseOfAcceptOperation =
        ResponseOfAcceptOperation.forDispenseRequest(receivedDispenseRequest);
    val acceptedResponse = actor.asksFor(responseOfAcceptOperation);
    val acceptedTask =
        acceptedResponse.getResource(responseOfAcceptOperation.expectedResponseBody());
    konnektor.verifyDocument(acceptedTask.getSignedKbvBundle());
    prescriptionManager.appendAcceptedPrescription(acceptedTask);
  }

  public static Builder of(String order) {
    return of(DequeStrategyEnum.fromString(order));
  }

  public static Builder of(DequeStrategyEnum order) {
    return new Builder(order);
  }

  public static class Builder {
    private final DequeStrategyEnum order;

    private Builder(DequeStrategyEnum order) {
      this.order = order;
    }

    public AcceptDispenseRequest from(Actor patient) {
      return new AcceptDispenseRequest(order, patient);
    }
  }
}
