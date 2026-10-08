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

package de.gematik.test.erezept.fhir.values.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.extensions.erp.CommunicationPayloadType;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@JsonInclude(Include.NON_EMPTY)
public record CommunicationReplyMessage(
    int version,
    String supplyOptionsType,
    String communicationType,
    String transactionID,

    // v3
    String text,

    // v1
    String info_text,
    String url,
    String readyForCollection,
    String deliveryStatus,
    InTransportPosition inTransportPosition,
    InTransportETA inTransportETA,
    Integer totalAmount,
    List<PaymentMethod> paymentMethods,

    // V1
    String pickUpCodeHR,
    String pickUpCodeDMC,

    // V3
    String pickupCodeHR,
    String pickupCodeDMC)
    implements CommunicationStructuredMessage {

  public static CommunicationReplyMessageV1Builder forV1() {
    return new CommunicationReplyMessageV1Builder();
  }

  public static class CommunicationReplyMessageV1Builder {

    private int version = 1;

    private String supplyOptionsType =
        GemFaker.randomElement(SupplyOptionsType.values()).getLabel();

    private String infoText = GemFaker.fakerCommunicationReplyMessage();
    private String url;

    private String pickUpCodeHR;
    private String pickUpCodeDMC;

    public CommunicationReplyMessageV1Builder version(int version) {
      this.version = version;
      return this;
    }

    public CommunicationReplyMessageV1Builder supplyOptionsType(
        SupplyOptionsType supplyOptionsType) {
      this.supplyOptionsType = supplyOptionsType.getLabel();
      return this;
    }

    public CommunicationReplyMessageV1Builder supplyOptionsType(String supplyOptionsType) {
      this.supplyOptionsType = supplyOptionsType;
      return this;
    }

    public CommunicationReplyMessageV1Builder infoText(String infoText) {
      this.infoText = infoText;
      return this;
    }

    public CommunicationReplyMessageV1Builder url(String url) {
      this.url = url;
      return this;
    }

    public CommunicationReplyMessageV1Builder pickUpCodeHR(String pickUpCodeHR) {
      this.pickUpCodeHR = pickUpCodeHR;
      return this;
    }

    public CommunicationReplyMessageV1Builder pickUpCodeDMC(String pickUpCodeDMC) {
      this.pickUpCodeDMC = pickUpCodeDMC;
      return this;
    }

    public CommunicationReplyMessage build() {
      return new CommunicationReplyMessage(
          version,
          supplyOptionsType,
          null,
          null,
          null,
          infoText,
          url,
          null,
          null,
          null,
          null,
          null,
          null,
          pickUpCodeHR,
          pickUpCodeDMC,
          null,
          null);
    }
  }

  public static CommunicationReplyMessageV3Builder forV3() {
    return new CommunicationReplyMessageV3Builder();
  }

  public static class CommunicationReplyMessageV3Builder {
    private int version = 3;

    private String communicationType = "text";
    private String transactionID = UUID.randomUUID().toString();

    private String text;

    private String url;
    private String readyForCollection;
    private String deliveryStatus;
    private InTransportPosition inTransportPosition;
    private InTransportETA inTransportETA;
    private Integer totalAmount;
    private List<PaymentMethod> paymentMethods;

    private String pickupCodeHR;
    private String pickupCodeDMC;

    public CommunicationReplyMessageV3Builder version(int version) {
      this.version = version;
      return this;
    }

    public CommunicationReplyMessageV3Builder communicationType(String communicationType) {
      this.communicationType = communicationType;
      return this;
    }

    public CommunicationReplyMessageV3Builder communicationType(
        CommunicationPayloadType communicationPayloadType) {
      this.communicationType = communicationPayloadType.getLabel();
      return this;
    }

    public CommunicationReplyMessageV3Builder transactionID(UUID transactionID) {
      return this.transactionID(transactionID.toString());
    }

    public CommunicationReplyMessageV3Builder transactionID(String transactionID) {
      this.transactionID = transactionID;
      return this;
    }

    public CommunicationReplyMessageV3Builder text(String text) {
      this.text = text;
      return this;
    }

    public CommunicationReplyMessageV3Builder url(String url) {
      this.url = url;
      return this;
    }

    public CommunicationReplyMessageV3Builder pickupCodeHR(String pickupCodeHR) {
      this.pickupCodeHR = pickupCodeHR;
      return this;
    }

    public CommunicationReplyMessageV3Builder pickupCodeDMC(String pickupCodeDMC) {
      this.pickupCodeDMC = pickupCodeDMC;
      return this;
    }

    public CommunicationReplyMessageV3Builder readyForCollecting(String readyForCollection) {
      this.readyForCollection = readyForCollection;
      return this;
    }

    public CommunicationReplyMessageV3Builder deliveryStatus(String deliveryStatus) {
      this.deliveryStatus = deliveryStatus;
      return this;
    }

    public CommunicationReplyMessageV3Builder totalAmount(Integer totalAmount) {
      this.totalAmount = totalAmount;
      return this;
    }

    public CommunicationReplyMessageV3Builder paymentMethods(List<PaymentMethod> paymentMethods) {
      this.paymentMethods = paymentMethods;
      return this;
    }

    public CommunicationReplyMessageV3Builder inTransportPosition(InTransportPosition pos) {
      this.inTransportPosition = pos;
      return this;
    }

    public CommunicationReplyMessageV3Builder inTransportETA(InTransportETA eta) {
      this.inTransportETA = eta;
      return this;
    }

    public CommunicationReplyMessage build() {

      if ("reservationStatus".equals(communicationType)) {
        // Note: Not allowed when type is reservationStatus
        this.text = null;
      }

      // Note: Error log when an integration test depends on the Faker-generated text property and
      // doesn't pass it by itself for communicationType=text or communicationType=link
      val isTextOrLink = communicationType.equals("text") || communicationType.equals("link");

      if (version == 3 && text == null && isTextOrLink) {
        log.error(
            "Error missing text property in V3 Communication Reply for communicationType: {}.",
            communicationType);
      }

      return new CommunicationReplyMessage(
          version,
          null,
          communicationType,
          transactionID,
          text,
          null,
          url,
          readyForCollection,
          deliveryStatus,
          inTransportPosition,
          inTransportETA,
          totalAmount,
          paymentMethods,
          null,
          null,
          pickupCodeHR,
          pickupCodeDMC);
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record InTransportPosition(Double lat, Double lon) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record InTransportETA(Long from, Long to) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record PaymentMethod(String method, String displayName, String url) {}
}
