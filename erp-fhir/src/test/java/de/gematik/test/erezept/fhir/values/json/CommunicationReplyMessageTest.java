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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.extensions.erp.CommunicationPayloadType;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import java.util.List;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;

class CommunicationReplyMessageTest {

  @Test
  void shouldBuildV1CommunicationReplyMessageCorrectly() {
    val msg =
        CommunicationReplyMessage.forV1()
            .version(1) // simply for coverage
            .supplyOptionsType(SupplyOptionsType.ON_PREMISE)
            .infoText("info-text")
            .url("http://test.url")
            .pickUpCodeHR("HR123")
            .pickUpCodeDMC("DMC123")
            .build();

    assertEquals(1, msg.version());
    assertEquals(SupplyOptionsType.ON_PREMISE.getLabel(), msg.supplyOptionsType());
    assertEquals("info-text", msg.info_text());
    assertEquals("http://test.url", msg.url());
    assertEquals("HR123", msg.pickUpCodeHR());
    assertEquals("DMC123", msg.pickUpCodeDMC());
  }

  @Test
  void shouldBuildV3CommunicationReplyMessageCorrectly() {
    val msg =
        CommunicationReplyMessage.forV3()
            .version(3) // simply for coverage
            .communicationType(CommunicationPayloadType.PICKUP_CODE_DMC)
            .text("info-text")
            .transactionID(UUID.randomUUID())
            .pickupCodeDMC("DMC123")
            .build();

    assertEquals(3, msg.version());
    assertEquals(CommunicationPayloadType.PICKUP_CODE_DMC.getLabel(), msg.communicationType());
    assertEquals("info-text", msg.text());
    assertEquals("DMC123", msg.pickupCodeDMC());
  }

  @Test
  void shouldBuildV3CommunicationPickUpHRReplyMessageCorrectly() {
    val msg =
        CommunicationReplyMessage.forV3()
            .communicationType(CommunicationPayloadType.PICKUP_CODE_HR)
            .text("info-text")
            .transactionID(UUID.randomUUID())
            .pickupCodeHR("0815")
            .build();

    assertEquals(3, msg.version());
    assertEquals(CommunicationPayloadType.PICKUP_CODE_HR.getLabel(), msg.communicationType());
    assertEquals("info-text", msg.text());
    assertEquals("0815", msg.pickupCodeHR());
  }

  @Test
  void shouldSupportUuidAndDefaults() {

    val msg = CommunicationReplyMessage.forV3().build();

    assertNotNull(msg.transactionID());
    assertNull(msg.text());
    assertEquals(3, msg.version());
  }

  @Test
  void shouldRespectNullableFields() {

    val msg = CommunicationReplyMessage.forV1().build();

    assertNull(msg.transactionID());
    assertNull(msg.text());
    assertNotNull(msg.info_text());
  }

  @Test
  void shouldBuildV3CommunicationReplyMessageWithTransportFieldsCorrectly() {

    val msg =
        CommunicationReplyMessage.forV3()
            .communicationType("deliveryStatus")
            .deliveryStatus("inTransport")
            .inTransportPosition(new CommunicationReplyMessage.InTransportPosition(52.52, 13.38))
            .inTransportETA(new CommunicationReplyMessage.InTransportETA(1735736400L, 1735741800L))
            .text("Der Bote is jetzt unterwegs!")
            .build();

    assertEquals(3, msg.version());
    assertEquals("deliveryStatus", msg.communicationType());
    assertEquals("inTransport", msg.deliveryStatus());
    assertEquals("Der Bote is jetzt unterwegs!", msg.text());

    assertNotNull(msg.inTransportPosition());
    assertEquals(52.52, msg.inTransportPosition().lat());
    assertEquals(13.38, msg.inTransportPosition().lon());

    assertNotNull(msg.inTransportETA());
    assertEquals(1735736400L, msg.inTransportETA().from());
    assertEquals(1735741800L, msg.inTransportETA().to());
  }

  @Test
  void shouldBuildV3CommunicationReplyMessage() {

    val msg =
        CommunicationReplyMessage.forV3()
            .communicationType(CommunicationPayloadType.DELIVERY_STATUS)
            .transactionID(UUID.randomUUID())
            .deliveryStatus("inTransport")
            .readyForCollecting("nextDay")
            .totalAmount(12345)
            .paymentMethods(
                List.of(
                    new CommunicationReplyMessage.PaymentMethod(
                        "creditcard", "Visa", "https://pay.test")))
            .url("https://example.com")
            .text("custom-text")
            .build();

    assertEquals(3, msg.version());
    assertEquals("deliveryStatus", msg.communicationType());
    assertEquals("inTransport", msg.deliveryStatus());
    assertEquals("nextDay", msg.readyForCollection());
    assertEquals(12345, msg.totalAmount());
    assertEquals("https://example.com", msg.url());
    assertEquals("custom-text", msg.text());
    assertNotNull(msg.paymentMethods());
  }

  @Test
  void shouldNotHaveTextInReservationStatus() {
    val msg =
        CommunicationReplyMessage.forV3()
            .communicationType(CommunicationPayloadType.RESERVATION_STATUS)
            .transactionID(UUID.randomUUID())
            .readyForCollecting("immediately")
            .text("custom-text") // text should be ignored for reservation status
            .build();

    assertEquals(3, msg.version());
    assertEquals("reservationStatus", msg.communicationType());
    assertEquals("immediately", msg.readyForCollection());
    assertNull(msg.text(), "Text should be null for reservation status messages");
  }

  @Test
  void shouldNotHaveTextInTextOrLinkNoTextIsGiven() {
    val textMsg =
        CommunicationReplyMessage.forV3().communicationType(CommunicationPayloadType.TEXT).build();
    assertNull(textMsg.text());
    val linkMsg =
        CommunicationReplyMessage.forV3().communicationType(CommunicationPayloadType.LINK).build();
    assertNull(linkMsg.text());
  }
}
