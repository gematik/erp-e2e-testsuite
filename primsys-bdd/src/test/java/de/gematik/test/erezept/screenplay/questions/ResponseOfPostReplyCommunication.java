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

import de.gematik.bbriccs.rest.fd.FhirBResponse;
import de.gematik.test.erezept.client.usecases.CommunicationPostCommand;
import de.gematik.test.erezept.common.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.common.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.common.screenplay.questions.DownloadNewMessages;
import de.gematik.test.erezept.common.screenplay.questions.ResponseOfPostCommunication;
import de.gematik.test.erezept.common.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.common.screenplay.util.ExchangedCommunication;
import de.gematik.test.erezept.common.screenplay.util.SafeAbility;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.r4.erp.CommunicationType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.erezept.fhir.valuesets.AvailabilityStatus;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor
@Slf4j
public class ResponseOfPostReplyCommunication extends ResponseOfPostCommunication {
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
    val communication = this.createReplyAs(actor);

    log.info(
        "{} is sending a REPLY Communication to {} basedOn {} (AccessCode {})",
        actor.getName(),
        builder.receiver.getName(),
        communication.getBasedOnReferenceId(),
        communication.getBasedOnAccessCodeString().orElse("n/a"));

    val cmd = new CommunicationPostCommand(communication);
    return erpClient.request(cmd);
  }

  private ErxCommunication createReplyAs(Actor actor) {
    val receiverId =
        SafeAbility.getAbility(builder.receiver, ProvidePatientBaseData.class).getKvnr();

    val messageForResponse = this.fetchExpectedMessageFromBackend(actor);
    val message =
        CommunicationReplyMessage.forV1()
            .infoText(GemFaker.fakerCommunicationReplyMessage())
            .build();

    return ErxCommunicationBuilder.asReply(message)
        .receiver(receiverId.getValue())
        .basedOn(messageForResponse.getBasedOnReferenceId())
        .availabilityStatus(AvailabilityStatus.AS_30)
        .supplyOptions(SupplyOptionsType.ON_PREMISE)
        .build();
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

    public ResponseOfPostReplyCommunication withoutMessage() {
      return new ResponseOfPostReplyCommunication(this);
    }

    public ResponseOfPostReplyCommunication withRandomMessage() {
      return withMessage(GemFaker.fakerCommunicationMessage(CommunicationType.REPLY));
    }

    public ResponseOfPostReplyCommunication withMessage(String message) {
      this.message = message;
      return new ResponseOfPostReplyCommunication(this);
    }
  }
}
