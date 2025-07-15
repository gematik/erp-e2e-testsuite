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

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ChargeItemGetByIdCommand;
import de.gematik.test.erezept.client.usecases.CommunicationPostCommand;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.r4.erp.ChargeItemCommunicationType;
import de.gematik.test.erezept.fhir.r4.erp.CommunicationType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.r4.erp.ICommunicationType;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.erezept.fhir.valuesets.AvailabilityStatus;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.ChargeItemChangeAuthorization;
import de.gematik.test.erezept.screenplay.util.ExchangedCommunication;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.ensure.Ensure;

@Slf4j
public class ResponseOfPostCommunication extends FhirResponseQuestion<ErxCommunication> {

  private final Builder builder;

  private ResponseOfPostCommunication(Builder builder) {
    this.builder = builder;
  }

  public static Builder infoRequest() {
    return new Builder(CommunicationType.INFO_REQ);
  }

  public static Builder representative() {
    return new Builder(CommunicationType.REPRESENTATIVE);
  }

  public static Builder dispenseRequest() {
    return new Builder(CommunicationType.DISP_REQ);
  }

  public static Builder dispenseDiGARequest() {
    return new Builder(CommunicationType.DISP_REQ);
  }

  public static Builder reply() {
    return new Builder(CommunicationType.REPLY);
  }

  public static Builder changeRequest() {
    return new Builder(ChargeItemCommunicationType.CHANGE_REQ);
  }

  public static Builder changeReply() {
    return new Builder(ChargeItemCommunicationType.CHANGE_REPLY);
  }

  public Actor getReceiver() {
    return builder.receiver;
  }

  @Override
  public ErpResponse<ErxCommunication> answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);

    val communication = this.createCommunication(actor);

    log.info(
        "{} is sending a {} to {} basedOn {} (AccessCode {})",
        actor.getName(),
        builder.type,
        builder.receiver.getName(),
        communication.getBasedOnReferenceId(),
        communication.getBasedOnAccessCodeString().orElse("n/a"));

    val cmd = new CommunicationPostCommand(communication);
    return erpClient.request(cmd);
  }

  private ErxCommunication createCommunication(Actor actor) {
    ErxCommunication communication;
    if (builder.type.getClass().equals(CommunicationType.class)) {
      val type = (CommunicationType) builder.type;
      communication = createCommunication(actor, type);
    } else if (builder.type.getClass().equals(ChargeItemCommunicationType.class)) {
      val type = (ChargeItemCommunicationType) builder.type;
      communication = createCommunication(actor, type);
    } else {
      throw new RuntimeException(
          format("Cannot post communication of type {0}", builder.type.getClass().getSimpleName()));
    }

    return communication;
  }

  /**
   * Creates one of the "normal" communications from CommunicationType
   *
   * @param actor to send the communication
   * @param type of communication message to send
   * @return ErxCommunication
   */
  private ErxCommunication createCommunication(Actor actor, CommunicationType type) {
    return switch (type) {
      case INFO_REQ -> createInfoRequestAs(actor);
      case REPRESENTATIVE -> createRepresentativeCommunicationAs(actor);
      case DISP_REQ -> createDispenseRequestAs(actor);
      case REPLY -> createReplyAs(actor);
    };
  }

  /**
   * Creates one of the chargeitem communications from ChargeItemCommunicationType
   *
   * @param actor to send the communication
   * @param type of communication message to send
   * @return ErxCommunication
   */
  private ErxCommunication createCommunication(Actor actor, ChargeItemCommunicationType type) {
    return switch (type) {
      case CHANGE_REQ -> createChangeReqAs(actor);
      case CHANGE_REPLY -> createChangeReplyAs(actor);
    };
  }

  private ErxCommunication createInfoRequestAs(Actor actor) {
    val baseData = SafeAbility.getAbility(actor, ProvidePatientBaseData.class);
    val receiverId = SafeAbility.getAbility(builder.receiver, UseSMCB.class).getTelematikID();

    // first make sure we download all tasks and fetch the corresponding prescription
    val taskBundle = actor.asksFor(DownloadAllTasks.sortedWith(builder.basedOnDequeStrategy));
    val task = taskBundle.getTasks().get(0);
    val prescription = actor.asksFor(FullPrescriptionBundle.forTask(task));

    val erxBuilder =
        ErxCommunicationBuilder.forInfoRequest(builder.message)
            .receiver(receiverId)
            .basedOn(prescription.getTask().getTaskId())
            .supplyOptions(SupplyOptionsType.ON_PREMISE)
            .insurance(baseData.getInsuranceIknr())
            .flowType(task.getFlowType());

    prescription
        .getKbvBundle()
        .ifPresent(kbvErpBundle -> erxBuilder.medication(kbvErpBundle.getMedication()));
    return erxBuilder.build();
  }

  private ErxCommunication createDispenseRequestAs(Actor actor) {
    val receiverId = SafeAbility.getAbility(builder.receiver, UseSMCB.class).getTelematikID();
    val dmcAbility = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);

    // first make sure we download all tasks and fetch the corresponding prescription
    val dmc = builder.basedOnDequeStrategy.chooseFrom(dmcAbility.getDmcs());
    val prescription = actor.asksFor(FullPrescriptionBundle.forTask(dmc.getTaskId()));

    val flowType = prescription.getTask().getFlowType();
    if (flowType == PrescriptionFlowType.FLOW_TYPE_162) {

      val accessCode = prescription.getTask().getAccessCode();
      return ErxCommunicationBuilder.forDiGADispenseRequest()
          .receiver(receiverId)
          .basedOn(prescription.getTask().getTaskId(), accessCode)
          .flowType(prescription.getTask().getFlowType())
          .build();
    } else {
      val message = new CommunicationDisReqMessage(SupplyOptionsType.ON_PREMISE, builder.message);

      val accessCode = prescription.getTask().getAccessCode();
      return ErxCommunicationBuilder.forDispenseRequest(message)
          .receiver(receiverId)
          .basedOn(prescription.getTask().getTaskId(), accessCode)
          .flowType(prescription.getTask().getFlowType())
          .build();
    }
  }

  private ErxCommunication createRepresentativeCommunicationAs(Actor actor) {
    val receiverId =
        SafeAbility.getAbility(builder.receiver, ProvidePatientBaseData.class).getKvnr();

    // pick the DMC by DequeStrategy and download the complete Prescription first
    val dmcs = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class).getDmcList();
    val dmc = builder.basedOnDequeStrategy.chooseFrom(dmcs);
    val prescription = actor.asksFor(DownloadTaskById.withId(dmc.getTaskId()));

    val accessCode = prescription.getTask().getAccessCode();
    return ErxCommunicationBuilder.forRepresentative(builder.message)
        .receiver(receiverId.getValue())
        .basedOn(prescription.getTask().getTaskId(), accessCode)
        .flowType(prescription.getTask().getFlowType())
        .build();
  }

  private ErxCommunication createReplyAs(Actor actor) {
    val receiverId =
        SafeAbility.getAbility(builder.receiver, ProvidePatientBaseData.class).getKvnr();

    val messageForResponse = this.fetchExpectedMessageFromBackend(actor);
    val message = new CommunicationReplyMessage();

    return ErxCommunicationBuilder.asReply(message)
        .receiver(receiverId.getValue())
        .basedOn(messageForResponse.getBasedOnReferenceId())
        .availabilityStatus(AvailabilityStatus.AS_30)
        .supplyOptions(SupplyOptionsType.ON_PREMISE)
        .build();
  }

  private ErxCommunication createChangeReqAs(Actor actor) {
    val receiverId = SafeAbility.getAbility(builder.receiver, UseSMCB.class).getTelematikID();
    val sender = SafeAbility.getAbility(actor, ProvideEGK.class).getKvnr();

    val chargeItemResponse =
        actor.asksFor(
            ResponseOfGetChargeItemBundle.forPrescription(builder.basedOnDequeStrategy)
                .asPatient());
    val chargeItem = chargeItemResponse.getExpectedResource();
    return ErxCommunicationBuilder.forChargeItemChangeRequest(builder.message)
        .basedOn(chargeItem.getChargeItem())
        .receiver(receiverId)
        .sender(sender.getValue())
        .build();
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

    private final ICommunicationType<?> type;
    private DequeStrategy basedOnDequeStrategy;
    private DequeStrategy requestDequeStrategy;
    private Actor receiver;
    private String message;

    private Builder(ICommunicationType<?> type) {
      this.type = type;
    }

    public Builder forPrescriptionFromBackend(String order) {
      return forPrescriptionFromBackend(DequeStrategy.fromString(order));
    }

    public Builder forPrescriptionFromBackend(DequeStrategy dequeStrategy) {
      this.basedOnDequeStrategy = dequeStrategy;
      return this;
    }

    public Builder forChargeItemFromBackend(DequeStrategy dequeStrategy) {
      this.basedOnDequeStrategy = dequeStrategy;
      return this;
    }

    public Builder forCommunicationRequestFromBackend(String order) {
      return forCommunicationRequestFromBackend(DequeStrategy.fromString(order));
    }

    public Builder forCommunicationRequestFromBackend(DequeStrategy dequeStrategy) {
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

    /**
     * In case of a DiGA the message is forbidden
     *
     * @return ResponseOfPostCommunication
     */
    public ResponseOfPostCommunication withoutMessage() {
      return new ResponseOfPostCommunication(this);
    }

    public ResponseOfPostCommunication withRandomMessage() {
      if (type.getClass().equals(CommunicationType.class)) {
        return withMessage(GemFaker.fakerCommunicationMessage((CommunicationType) type));
      } else if (type.getClass().equals(ChargeItemCommunicationType.class)) {
        return withMessage(
            GemFaker.fakerChargeItemCommunicationMessage((ChargeItemCommunicationType) type));
      } else {
        throw new RuntimeException(
            format(
                "Cannot generate random message for communication of type {0}",
                type.getClass().getSimpleName()));
      }
    }

    public ResponseOfPostCommunication withMessage(String message) {
      this.message = message;
      return new ResponseOfPostCommunication(this);
    }
  }
}
