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

package de.gematik.test.erezept.actors;

import static de.gematik.test.erezept.fhir.builder.GemFaker.randomElement;

import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.extensions.erp.CommunicationPayloadType;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.erezept.fhir.valuesets.DeliveryStatus;
import de.gematik.test.erezept.fhir.valuesets.ReadyForCollecting;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@EqualsAndHashCode(callSuper = true)
public class PharmacyActor extends ErpActor {

  public PharmacyActor(String name) {
    super(ActorType.PHARMACY, name);
  }

  public String getCommonName() {
    val useSmcb = SafeAbility.getAbility(this, UseSMCB.class);
    return useSmcb.getSmcB().getOwnerData().getCommonName();
  }

  public TelematikID getTelematikId() {
    val useSmcb = SafeAbility.getAbility(this, UseSMCB.class);
    return TelematikID.from(useSmcb.getTelematikID());
  }

  public CommunicationReplyMessage.CommunicationReplyMessageV3Builder communicationReplayFakerText(
      String text) {
    val builder = new CommunicationReplyMessage.CommunicationReplyMessageV3Builder();
    builder.text(
        Objects.requireNonNullElseGet(text, () -> GemFaker.getFaker().chuckNorris().fact()));
    builder.communicationType(CommunicationPayloadType.TEXT);
    return builder;
  }

  public CommunicationReplyMessage.CommunicationReplyMessageV3Builder communicationReplayFakerLink(
      String text, String link) {
    val builder = new CommunicationReplyMessage.CommunicationReplyMessageV3Builder();
    builder.text(
        Objects.requireNonNullElseGet(text, () -> GemFaker.getFaker().chuckNorris().fact()));
    builder.communicationType(CommunicationPayloadType.LINK);
    builder.url(link);

    return builder;
  }

  public CommunicationReplyMessage.CommunicationReplyMessageV3Builder
      communicationReplayFakerPickupCodeHR(int pickUpCode, String text) {
    val builder = new CommunicationReplyMessage.CommunicationReplyMessageV3Builder();
    builder.text(
        Objects.requireNonNullElseGet(text, () -> GemFaker.getFaker().chuckNorris().fact()));
    builder.communicationType(CommunicationPayloadType.PICKUP_CODE_HR);
    builder.pickupCodeHR(String.valueOf(pickUpCode));
    return builder;
  }

  public CommunicationReplyMessage.CommunicationReplyMessageV3Builder
      communicationReplayFakerPickupCodeDMC(int pickUpCodeDMC, String text) {
    val builder = new CommunicationReplyMessage.CommunicationReplyMessageV3Builder();
    builder.text(
        Objects.requireNonNullElseGet(text, () -> GemFaker.getFaker().chuckNorris().fact()));
    builder.communicationType(CommunicationPayloadType.PICKUP_CODE_DMC);
    builder.pickupCodeDMC(String.valueOf(pickUpCodeDMC));
    return builder;
  }

  public CommunicationReplyMessage.CommunicationReplyMessageV3Builder
      communicationReplayFakerDeliveryStatus(String text, DeliveryStatus deliveryStatus) {
    val builder = new CommunicationReplyMessage.CommunicationReplyMessageV3Builder();
    builder.text(
        Objects.requireNonNullElseGet(text, () -> GemFaker.getFaker().chuckNorris().fact()));
    builder.communicationType(CommunicationPayloadType.DELIVERY_STATUS);
    builder.deliveryStatus(
        deliveryStatus != null
            ? deliveryStatus.getCode()
            : randomElement(DeliveryStatus.values()).getCode());
    return builder;
  }

  public CommunicationReplyMessage.CommunicationReplyMessageV3Builder
      communicationReplayFakerPaymentInfo(
          @Nullable String text,
          int totalAmount,
          @Nullable List<CommunicationReplyMessage.PaymentMethod> paymentMethod) {
    val builder = new CommunicationReplyMessage.CommunicationReplyMessageV3Builder();
    builder.text(
        Objects.requireNonNullElseGet(text, () -> GemFaker.getFaker().chuckNorris().fact()));
    builder.communicationType(CommunicationPayloadType.PAYMENT_INFO);
    builder.totalAmount(totalAmount);

    builder.paymentMethods(paymentMethod);
    return builder;
  }

  public CommunicationReplyMessage.CommunicationReplyMessageV3Builder
      communicationReplayFakerReservationStatus(ReadyForCollecting whenIsAvailable) {
    val builder = new CommunicationReplyMessage.CommunicationReplyMessageV3Builder();
    builder.communicationType(CommunicationPayloadType.RESERVATION_STATUS);
    builder.text(null);
    builder.readyForCollecting(whenIsAvailable.getCode());
    return builder;
  }
}
