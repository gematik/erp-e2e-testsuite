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

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.CommunicationPostCommand;
import de.gematik.test.erezept.exceptions.FeatureNotImplementedException;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemCommunicationBuilder;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.valuesets.AvailabilityStatus;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategyEnum;
import de.gematik.test.erezept.screenplay.util.ExchangedCommunication;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseOfPostCommunication extends FhirResponseQuestion<ErxCommunication> {

  private final Builder builder;

  @Override
  public Class<ErxCommunication> expectedResponseBody() {
    return ErxCommunication.class;
  }

  @Override
  public String getOperationName() {
    return format("POST {0}", builder.type);
  }

  public Actor getReceiver() {
    return builder.receiver;
  }

  @Override
  public ErpResponse answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);

    ErxCommunication communication;

    switch (builder.type) {
      case INFO_REQ:
        communication = createInfoRequestAs(actor);
        break;
      case REPRESENTATIVE:
        communication = createRepresentativeCommunicationAs(actor);
        break;
      case DISP_REQ:
        communication = createDispenseRequestAs(actor);
        break;
      case REPLY:
        communication = createReplyAs(actor);
        break;
      case CHANGE_REQ:
        communication = createChangeReqAs(actor);
        break;
      case CHANGE_REPLY:
        communication = createChangeReplyAs(actor);
        break;
      default:
        throw new FeatureNotImplementedException(
            format("Sending Communication of Type {0}", builder.type));
    }

    log.info(
        format(
            "{0} is sending a {1} to {2} basedOn {3} (AccessCode {4})",
            actor.getName(),
            builder.type,
            builder.receiver.getName(),
            communication.getBasedOnReferenceId(),
            communication.getBasedOnAccessCodeString().orElse("n/a")));

    val cmd = new CommunicationPostCommand(communication);
    return erpClient.request(cmd);
  }

  private ErxCommunication createInfoRequestAs(Actor actor) {
    val baseData = SafeAbility.getAbility(actor, ProvidePatientBaseData.class);
    val receiverId = SafeAbility.getAbility(builder.receiver, UseSMCB.class).getTelematikID();

    // first make sure we download all tasks and fetch the corresponding prescription
    val taskBundle = actor.asksFor(DownloadAllTasks.sortedWith(builder.basedOnDequeStrategy));
    val task = taskBundle.getTasks().get(0);
    val prescription = actor.asksFor(FullPrescriptionBundle.forTask(task));

    return ErxCommunicationBuilder.builder()
        .recipient(receiverId)
        .basedOnTaskId(prescription.getTask().getUnqualifiedId())
        .medication(prescription.getKbvBundle().getMedication())
        .supplyOptions(SupplyOptionsType.onPremise())
        .insurance(baseData.getInsuranceIknr())
        .flowType(task.getFlowType())
        .buildInfoReq(builder.message);
  }

  private ErxCommunication createDispenseRequestAs(Actor actor) {
    val receiverId = SafeAbility.getAbility(builder.receiver, UseSMCB.class).getTelematikID();

    // first make sure we download all tasks and fetch the corresponding prescription
    val taskBundle = actor.asksFor(DownloadAllTasks.sortedWith(builder.basedOnDequeStrategy));
    val task = taskBundle.getTasks().get(0);
    val prescription = actor.asksFor(FullPrescriptionBundle.forTask(task));

    val accessCode = prescription.getTask().getAccessCode();
    return ErxCommunicationBuilder.builder()
        .recipient(receiverId)
        .basedOnTask(prescription.getTask().getUnqualifiedId(), accessCode)
        .buildDispReq(builder.message);
  }

  private ErxCommunication createRepresentativeCommunicationAs(Actor actor) {
    val receiverId =
        SafeAbility.getAbility(builder.receiver, ProvidePatientBaseData.class).getKvid();

    // pick the DMC by DequeStrategy and download the complete Prescription first
    val dmcs = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class).getDmcList();
    val dmc = builder.basedOnDequeStrategy.chooseFrom(dmcs);
    val prescription = actor.asksFor(DownloadTaskById.withId(dmc.getTaskId()));

    val accessCode = prescription.getTask().getAccessCode();
    return ErxCommunicationBuilder.builder()
        .recipient(receiverId)
        .basedOnTask(prescription.getTask().getUnqualifiedId(), accessCode)
        .flowType(prescription.getTask().getFlowType())
        .buildRepresentative(builder.message);
  }

  private ErxCommunication createReplyAs(Actor actor) {
    val receiverId =
        SafeAbility.getAbility(builder.receiver, ProvidePatientBaseData.class).getKvid();
    val messageForResponse = this.fetchExpectedMessageFromBackend(actor);
    return ErxCommunicationBuilder.builder()
        .recipient(receiverId)
        .basedOnTaskId(messageForResponse.getBasedOnReferenceId())
        .availabilityStatus(AvailabilityStatus.AS_30)
        .supplyOptions(SupplyOptionsType.onPremise())
        .buildReply(builder.message);
  }

  private ErxCommunication createChangeReqAs(Actor actor) {
    val receiverId = SafeAbility.getAbility(builder.receiver, UseSMCB.class).getTelematikID();

    val chargeItemResponse =
        actor.asksFor(ResponseOfGetChargeItem.forPrescription(builder.basedOnDequeStrategy));
    val chargeItem = chargeItemResponse.getResource(ErxChargeItem.class);
    return ErxChargeItemCommunicationBuilder.builder()
        .basedOnChargeItem(chargeItem)
        .recipient(receiverId)
        .buildReq(builder.message);
  }

  private ErxCommunication createChangeReplyAs(Actor actor) {
    val receiverId =
        SafeAbility.getAbility(builder.receiver, ProvidePatientBaseData.class).getKvid();
    val messageForResponse = this.fetchExpectedMessageFromBackend(actor);
    val chargeItemId = messageForResponse.getBasedOnReferenceId();
    return ErxChargeItemCommunicationBuilder.builder()
        .basedOnChargeItem(chargeItemId)
        .recipient(receiverId)
        .buildReq(builder.message);
  }

  private ErxCommunication fetchExpectedMessageFromBackend(Actor actor) {
    val receiverId =
        SafeAbility.getAbility(builder.receiver, ProvidePatientBaseData.class).getKvid();
    val expectedMessages =
        SafeAbility.getAbility(actor, ManageCommunications.class).getExpectedCommunications();

    val newMessages = actor.asksFor(DownloadNewMessages.fromServer());

    // ..fromSender(receiverId) -> we are replying to a message sent from the "receiver"
    val fromRequestSender = newMessages.getCommunicationsFromSender(receiverId);

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
        .filter(erxCom -> erxCom.getUnqualifiedId().equals(expected.getCommunicationId()))
        .findFirst()
        .orElseThrow(
            () ->
                new MissingPreconditionError(
                    format(
                        "The expected {0} Message with ID {1} from {2} was not found within fetched Communications from Backend",
                        expected.getType(),
                        expected.getCommunicationId(),
                        builder.receiver.getName())));
  }

  public static Builder infoRequest() {
    return new Builder(ErxCommunication.CommunicationType.INFO_REQ);
  }

  public static Builder representative() {
    return new Builder(ErxCommunication.CommunicationType.REPRESENTATIVE);
  }

  public static Builder dispenseRequest() {
    return new Builder(ErxCommunication.CommunicationType.DISP_REQ);
  }

  public static Builder reply() {
    return new Builder(ErxCommunication.CommunicationType.REPLY);
  }

  public static Builder changeRequest() {
    return new Builder(ErxCommunication.CommunicationType.CHANGE_REQ);
  }

  public static Builder changeReply() {
    return new Builder(ErxCommunication.CommunicationType.CHANGE_REPLY);
  }

  public static class Builder {
    private final ErxCommunication.CommunicationType type;
    private DequeStrategyEnum basedOnDequeStrategy;
    private DequeStrategyEnum requestDequeStrategy;
    private Actor receiver;
    private String message;

    private Builder(ErxCommunication.CommunicationType type) {
      this.type = type;
    }

    public Builder forPrescriptionFromBackend(String order) {
      return forPrescriptionFromBackend(DequeStrategyEnum.fromString(order));
    }

    public Builder forPrescriptionFromBackend(DequeStrategyEnum dequeStrategy) {
      this.basedOnDequeStrategy = dequeStrategy;
      return this;
    }

    public Builder forChargeItemFromBackend(String order) {
      return forChargeItemFromBackend(DequeStrategyEnum.fromString(order));
    }

    public Builder forChargeItemFromBackend(DequeStrategyEnum dequeStrategy) {
      this.basedOnDequeStrategy = dequeStrategy;
      return this;
    }

    public Builder forCommunicationRequestFromBackend(String order) {
      return forCommunicationRequestFromBackend(DequeStrategyEnum.fromString(order));
    }

    public Builder forCommunicationRequestFromBackend(DequeStrategyEnum dequeStrategy) {
      this.requestDequeStrategy = dequeStrategy;
      return this;
    }

    public Builder sentTo(Actor receiver) {
      this.receiver = receiver;
      return this;
    }

    public Builder receivedFrom(Actor sender) {
      this.receiver = sender;
      return this;
    }

    public ResponseOfPostCommunication withRandomMessage() {
      return withMessage(GemFaker.fakerCommunicationMessage(type));
    }

    public ResponseOfPostCommunication withMessage(String message) {
      this.message = message;
      return new ResponseOfPostCommunication(this);
    }
  }
}
