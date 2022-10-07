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

import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.Reference;

public class ErxChargeItemCommunicationBuilder
    extends AbstractCommunicationBuilder<ErxChargeItemCommunicationBuilder> {

  private String chargeItemReference;

  private ErxChargeItemCommunicationBuilder() {}

  public static ErxChargeItemCommunicationBuilder builder() {
    return new ErxChargeItemCommunicationBuilder();
  }

  public ErxChargeItemCommunicationBuilder basedOnChargeItem(@NonNull final String chargeItemId) {
    this.chargeItemReference =
        chargeItemId.startsWith("ChargeItem/") ? chargeItemId : "ChargeItem/" + chargeItemId;
    return self();
  }

  public ErxChargeItemCommunicationBuilder basedOnChargeItem(
      @NonNull final ErxChargeItem chargeItem) {
    return basedOnChargeItem(chargeItem.getPrescriptionId().getValue());
  }

  public ErxCommunication buildReq(String message) {
    checkRequiredForChargeItemCommunication();
    val com = build(ErxCommunication.CommunicationType.CHANGE_REQ, message);
    com.setBasedOn(List.of(new Reference(chargeItemReference)));

    return com;
  }

  public ErxCommunication buildReply(String message) {
    checkRequiredForChargeItemCommunication();
    val com = build(ErxCommunication.CommunicationType.CHANGE_REPLY, message);
    com.setBasedOn(List.of(new Reference(chargeItemReference)));

    return com;
  }

  private void checkRequiredForChargeItemCommunication() {
    this.checkRequired(chargeItemReference, "A ChargeItem Communication requires a ChargeItem");

    // Note: in current version 1.1.1 of profiles not required, but will be added in 1.2
    this.checkRequired(
        sender,
        "A ChargeItem Communication requires a Sender"); // why do others do not require a sender?
  }
}
