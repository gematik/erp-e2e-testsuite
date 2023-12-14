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

import de.gematik.test.erezept.client.usecases.CommunicationPostCommand;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.resources.erp.CommunicationType;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.erezept.fhir.valuesets.AvailabilityStatus;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.questions.GetReceivedCommunication;
import de.gematik.test.erezept.screenplay.questions.ResponseOfAcceptOperation;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

import static java.text.MessageFormat.format;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AcceptDispenseRequest implements Task {

  private final DequeStrategy order;
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
    val acceptedTask = acceptedResponse.getExpectedResource();
    konnektor.verifyDocument(acceptedTask.getSignedKbvBundle());
    prescriptionManager.appendAcceptedPrescription(acceptedTask);

    // Required for App
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val eGK = SafeAbility.getAbility(sender, ProvideEGK.class);
    val smcb = SafeAbility.getAbility(actor, UseSMCB.class);
    val response =
        ErxCommunicationBuilder.builder()
            .basedOnTaskId(acceptedTask.getTaskId())
            .recipient(eGK.getKvnr().getValue())
            .sender(smcb.getTelematikID())
            .availabilityStatus(AvailabilityStatus.AS_30)
            .supplyOptions(SupplyOptionsType.SHIPMENT)
            .buildReply(new CommunicationReplyMessage());
    val comResponse = new CommunicationPostCommand(response);
    erpClient.request(comResponse);
  }

  public static Builder of(String order) {
    return of(DequeStrategy.fromString(order));
  }

  public static Builder of(DequeStrategy order) {
    return new Builder(order);
  }

  public static class Builder {
    private final DequeStrategy order;

    private Builder(DequeStrategy order) {
      this.order = order;
    }

    public AcceptDispenseRequest from(Actor patient) {
      return new AcceptDispenseRequest(order, patient);
    }
  }
}
