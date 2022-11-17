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

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.resources.erp.ICommunicationType;
import java.util.List;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Communication;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.StringType;

abstract class AbstractCommunicationBuilder<T extends AbstractCommunicationBuilder<T>>
    extends AbstractResourceBuilder<T> {

  protected Communication.CommunicationStatus status = Communication.CommunicationStatus.UNKNOWN;
  protected String recipient;
  protected String sender;

  public T status(@NonNull final String code) {
    return status(Communication.CommunicationStatus.fromCode(code));
  }

  public T status(@NonNull final Communication.CommunicationStatus status) {
    this.status = status;
    return self();
  }

  public T recipient(@NonNull final String recipient) {
    this.recipient = recipient;
    return self();
  }

  public T sender(@NonNull final String sender) {
    this.sender = sender;
    return self();
  }

  protected ErxCommunication build(
      ICommunicationType<?> type, Supplier<CanonicalType> profileSupplier, String message) {
    checkRequiredCommon();
    val com = new ErxCommunication();

    val profile = profileSupplier.get();
    val meta = new Meta().setProfile(List.of(profile));

    // set FHIR-specific values provided by HAPI
    com.setId(this.getResourceId()).setMeta(meta);

    com.setStatus(status);

    val recipientRef = type.getRecipientReference(recipient);
    com.setRecipient(List.of(recipientRef));

    // NOTE: "normal" communications do not necessarily require a sender: why?
    // probably will be added for all Communications in Version 1.2
    if (sender != null) {
      val senderRef = type.getSenderReference(sender);
      com.setSender(senderRef);
    }

    val payload = new Communication.CommunicationPayloadComponent(new StringType(message));
    com.setPayload(List.of(payload));

    return com;
  }

  private void checkRequiredCommon() {
    this.checkRequired(status, "A communication requires a  status");
    this.checkRequired(recipient, "A communication requires a recipient");
  }
}
