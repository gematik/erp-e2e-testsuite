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
 */

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.ChargeItemCommunicationType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.r4.erp.ICommunicationType;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Communication;
import org.hl7.fhir.r4.model.StringType;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ErxCommunicationBuilder<B extends ErxCommunicationBuilder<B>>
    extends ResourceBuilder<ErxCommunication, B> {

  protected final String message;
  protected ErpWorkflowVersion erpWorkflowVersion;
  protected String baseOnReference;
  protected Communication.CommunicationStatus status = Communication.CommunicationStatus.UNKNOWN;
  protected String receiver;
  protected String sender;

  public static ErxComInfoReqBuilder forInfoRequest(String message) {
    return new ErxComInfoReqBuilder(message);
  }

  public static ErxComRepresentativeBuilder forRepresentative(String message) {
    return new ErxComRepresentativeBuilder(message);
  }

  public static ErxComDispReqBuilder forDispenseRequest(CommunicationDisReqMessage message) {
    return forDispenseRequest(message.asJson());
  }

  public static ErxComDispReqBuilder forDispenseRequest(String message) {
    return new ErxComDispReqBuilder(message);
  }

  public static ErxComReplyBuilder asReply(CommunicationReplyMessage message) {
    return asReply(message.asJson());
  }

  public static ErxComReplyBuilder asReply(String message) {
    return new ErxComReplyBuilder(message);
  }

  public static ErxComChargeItemBuilder forChargeItemChangeRequest(String message) {
    return new ErxComChargeItemBuilder(ChargeItemCommunicationType.CHANGE_REQ, message);
  }

  public static ErxComChargeItemBuilder forChargeItemChangeReply(String message) {
    return new ErxComChargeItemBuilder(ChargeItemCommunicationType.CHANGE_REPLY, message);
  }

  public B version(ErpWorkflowVersion version) {
    this.erpWorkflowVersion = version;
    return self();
  }

  public B status(String code) {
    return status(Communication.CommunicationStatus.fromCode(code.toLowerCase()));
  }

  public B status(Communication.CommunicationStatus status) {
    this.status = status;
    return self();
  }

  public B receiver(String receiver) {
    this.receiver = receiver;
    return self();
  }

  public B sender(String sender) {
    this.sender = sender;
    return self();
  }

  protected ErxCommunication buildCommon(
      ICommunicationType<?> type, Supplier<CanonicalType> profileSupplier) {
    checkRequiredCommon();
    val com = this.createResource(ErxCommunication::new, profileSupplier.get());

    com.setStatus(status);
    val recipientRef = type.getRecipientReference(this.erpWorkflowVersion, this.receiver);
    com.setRecipient(List.of(recipientRef));

    // NOTE: "normal" communications do not necessarily require a sender: why?
    // probably will be added for all Communications in Version 1.2
    Optional.ofNullable(this.sender)
        .ifPresent(
            s -> {
              val senderRef = type.getSenderReference(this.erpWorkflowVersion, s);
              com.setSender(senderRef);
            });

    val payload = new Communication.CommunicationPayloadComponent(new StringType(message));
    com.setPayload(List.of(payload));

    return com;
  }

  protected void checkRequiredCommon() {
    this.checkRequired(baseOnReference, "A communication requires a basedOn-Reference");
    this.checkRequired(status, "A communication requires a  status");
    this.checkRequired(receiver, "A communication requires a recipient");
  }
}
