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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CommunicationReplyMessageTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void shouldBuildV1CommunicationReplyMessageCorrectly() {
    var msg =
        CommunicationReplyMessage.forV1()
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
  void shouldSupportUuidAndDefaults() {

    var msg = CommunicationReplyMessage.forV3().build();

    assertNotNull(msg.transactionID());
    assertNotNull(msg.text());
    assertEquals(3, msg.version());
    assertEquals("text", msg.communicationType());
  }

  @Test
  void shouldRespectNullableFields() {

    var msg = CommunicationReplyMessage.forV1().build();

    assertNull(msg.transactionID());
    assertNull(msg.text());
    assertNotNull(msg.info_text());
  }

  @Test
  void shouldBuildV3CommunicationReplyMessageWithTransportFieldsCorrectly() {

    var msg =
        CommunicationReplyMessage.forV3()
            .communicationType("deliveryStatus")
            .deliveryStatus("inTransport")
            .inTransportPosition(new CommunicationReplyMessage.InTransportPosition(52.52, 13.38))
            .inTransportETA(new CommunicationReplyMessage.InTransportETA(1735736400L, 1735741800L))
            .build();

    assertEquals(3, msg.version());
    assertEquals("deliveryStatus", msg.communicationType());
    assertEquals("inTransport", msg.deliveryStatus());

    assertNotNull(msg.inTransportPosition());
    assertEquals(52.52, msg.inTransportPosition().lat());
    assertEquals(13.38, msg.inTransportPosition().lon());

    assertNotNull(msg.inTransportETA());
    assertEquals(1735736400L, msg.inTransportETA().from());
    assertEquals(1735741800L, msg.inTransportETA().to());
  }

  @Test
  void shouldBuildV3CommunicationReplyMessage() {

    var msg =
        CommunicationReplyMessage.forV3()
            .communicationType("deliveryStatus")
            .transactionID(UUID.randomUUID())
            .deliveryStatus("inTransport")
            .readyForCollection("nextDay")
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
}
