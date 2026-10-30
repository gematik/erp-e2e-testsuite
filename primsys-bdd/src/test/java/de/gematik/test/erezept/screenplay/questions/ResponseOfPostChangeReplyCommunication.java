/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.rest.fd.FhirBResponse;
import de.gematik.test.erezept.client.usecases.ChargeItemGetByIdCommand;
import de.gematik.test.erezept.client.usecases.CommunicationPostCommand;
import de.gematik.test.erezept.common.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.common.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.common.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.common.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.common.screenplay.questions.DownloadNewMessages;
import de.gematik.test.erezept.common.screenplay.questions.ResponseOfPostCommunication;
import de.gematik.test.erezept.common.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.common.screenplay.util.ExchangedCommunication;
import de.gematik.test.erezept.common.screenplay.util.SafeAbility;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.r4.erp.ChargeItemCommunicationType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.ChargeItemChangeAuthorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.ensure.Ensure;

@RequiredArgsConstructor
@Slf4j
public class ResponseOfPostChangeReplyCommunication extends ResponseOfPostCommunication {
  private final Builder builder;

  public static Builder build() {
    return new Builder();
  }

  @Override
  public Actor getReceiver() {
    return builder.receiver;
  }

  @Override
  public FhirBResponse<ErxCommunication> answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val communication = this.createChangeReplyAs(actor);

    log.info(
        "{} is sending a CHANGE_REPLY Communication to {} basedOn {} (AccessCode {})",
        actor.getName(),
        builder.receiver.getName(),
        communication.getBasedOnReferenceId(),
        communication.getBasedOnAccessCodeString().orElse("n/a"));

    val cmd = new CommunicationPostCommand(communication);
    return erpClient.request(cmd);
  }

  private ErxCommunication createChangeReplyAs(Actor actor) {
    val receiverId =
        SafeAbility.getAbility(builder.receiver, ProvidePatientBaseData.class).getKvnr();
    val senderId = SafeAbility.getAbility(actor, UseSMCB.class).getTelematikID();
    val messageForResponse = this.fetchExpectedMessageFromBackend(actor);
    val chargeItemId = messageForResponse.getBasedOnReferenceId();

    storeChargeItemAuthorizedBy(actor, messageForResponse);
    return ErxCommunicationBuilder.forChargeItemChangeReply(builder.message)
        .basedOn(chargeItemId)
        .receiver(receiverId.getValue())
        .sender(senderId)
        .build();
  }

  private void storeChargeItemAuthorizedBy(Actor actor, ErxCommunication communication) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val prescriptionStack = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);

    // fetch the taskID/prescriptionID and AccessCode from reference
    val chargeItemId = communication.getBasedOnReferenceId();
    val prescriptionId = PrescriptionId.from(chargeItemId);
    val accessCode =
        communication
            .getBasedOnAccessCode()
            .orElseThrow(() -> new MissingFieldException(ErxCommunication.class, "AccessCode"));

    // fetch the chargeItem by the ID and store authorization for later use
    val cmd = new ChargeItemGetByIdCommand(prescriptionId, accessCode);
    val response = erpClient.request(cmd);
    val chargeItemBundle = response.getExpectedResource();
    val chargeItem = chargeItemBundle.getChargeItem();

    // check A_22128
    val acQuestion =
        new Question<Boolean>() {
          @Override
          public Boolean answeredBy(Actor actor) {
            return chargeItem.getAccessCode().isEmpty();
          }
        };

    val receiptQuestion =
        new Question<Boolean>() {
          @Override
          public Boolean answeredBy(Actor actor) {
            return chargeItemBundle.getReceipt().isEmpty();
          }
        };

    actor.attemptsTo(Ensure.that("Das ChargeItem keinen AccessCode enthält", acQuestion).isTrue());
    actor.attemptsTo(
        Ensure.that("Das ChargeItem keine Quittung enthält", receiptQuestion).isTrue());

    val authorization = ChargeItemChangeAuthorization.forChargeItem(chargeItem, accessCode);
    prescriptionStack.getChargeItemChangeAuthorizations().append(authorization);
  }

  private ErxCommunication fetchExpectedMessageFromBackend(Actor actor) {
    val receiverId =
        SafeAbility.getAbility(builder.receiver, ProvidePatientBaseData.class).getKvnr();
    val expectedMessages =
        SafeAbility.getAbility(actor, ManageCommunications.class).getExpectedCommunications();

    val newMessages = actor.asksFor(DownloadNewMessages.fromServer());

    // ..fromSender(receiverId) -> we are replying to a message sent from the "receiver"
    val fromRequestSender = newMessages.getCommunicationsFromSender(receiverId.getValue());

    if (expectedMessages.isEmpty() || fromRequestSender.isEmpty()) {
      throw new MissingPreconditionError(
          format(
              "{0} does not have unread/expected Messages from {1} with ID {2}",
              actor.getName(), builder.receiver.getName(), receiverId));
    }

    ExchangedCommunication expected =
        builder.requestDequeStrategy.chooseFrom(expectedMessages.getRawList());

    // map the received messages from Server against the expected message
    return fromRequestSender.stream()
        .filter(erxCom -> erxCom.getType().equals(expected.getType()))
        .filter(
            erxCom ->
                erxCom
                    .getUnqualifiedId()
                    .equals(
                        expected
                            .getCommunicationId()
                            .orElse(
                                "no_id"))) // in case ID is unknown use some default which will be
        // invalid for sure
        .findFirst()
        .orElseThrow(
            () ->
                new MissingPreconditionError(
                    format(
                        "The expected {0} Message with ID {1} from {2} was not found within fetched"
                            + " Communications from Backend",
                        expected.getType(),
                        expected.getCommunicationId(),
                        builder.receiver.getName())));
  }

  public static class Builder {
    private DequeStrategy requestDequeStrategy;
    private Actor receiver;
    private String message;

    public Builder forCommunicationRequestFromBackend(String order) {
      return forCommunicationRequestFromBackend(DequeStrategy.fromString(order));
    }

    public Builder forCommunicationRequestFromBackend(DequeStrategy dequeStrategy) {
      this.requestDequeStrategy = dequeStrategy;
      return this;
    }

    public Builder receivedFrom(Actor sender) {
      this.receiver = sender;
      return this;
    }

    public ResponseOfPostChangeReplyCommunication withoutMessage() {
      return new ResponseOfPostChangeReplyCommunication(this);
    }

    public ResponseOfPostChangeReplyCommunication withRandomMessage() {
      return withMessage(
          GemFaker.fakerChargeItemCommunicationMessage(ChargeItemCommunicationType.CHANGE_REPLY));
    }

    public ResponseOfPostChangeReplyCommunication withMessage(String message) {
      this.message = message;
      return new ResponseOfPostChangeReplyCommunication(this);
    }
  }
}
