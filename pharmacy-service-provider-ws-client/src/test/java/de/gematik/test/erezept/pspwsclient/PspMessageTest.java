/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.pspwsclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.test.erezept.pspwsclient.dataobjects.DeliveryOption;
import de.gematik.test.erezept.pspwsclient.dataobjects.PspMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PspMessageTest {
  static DeliveryOption deliverOption;
  static String telematikId;
  static String transactionId;
  static byte[] blob;
  static PspMessage pspMessage;

  @BeforeAll
  static void setup() {
    deliverOption = DeliveryOption.SHIPMENT;
    telematikId = "testTelemID";
    transactionId = "testTranacID";
    blob =
        "senselessUnididentifiedAndExtraLongNeedlessStringTo build an sensless unpracticable byteArray with a number : 1"
            .getBytes();
    pspMessage = PspMessage.create(deliverOption, telematikId, transactionId, blob, null);
  }

  @BeforeEach
  void create() {
    pspMessage = PspMessage.create(deliverOption, telematikId, transactionId, blob, null);
  }

  @Test
  void crateTest() {
    PspMessage pspMessage2 =
        PspMessage.create(deliverOption, telematikId, transactionId, blob, "lovely tests ;-)");
    assertNotEquals(pspMessage, pspMessage2);
  }

  @Test
  void generateByteBlobCorrectTelemID() {
    assertEquals(telematikId, pspMessage.getClientId());
  }

  @Test
  void generateByteBlobCorrectTransactionId() {
    assertEquals(transactionId, pspMessage.getTransactionId());
  }

  @Test
  void generateByteBlobCorrectApoURL() {
    assertEquals(deliverOption, pspMessage.getDeliveryOption());
  }

  @Test
  void generateByteBlobCorrectByteBlob() {
    assertEquals(blob, pspMessage.getBlob());
  }

  @Test
  void throwsNotNullAtTelematikId() {
    assertThrows(
        NullPointerException.class,
        () -> {
          PspMessage.create(deliverOption, null, transactionId, blob, "lovely tests ;-)");
        });
  }

  @Test
  void throwsNotNullAtBlob() {
    assertThrows(
        NullPointerException.class,
        () -> {
          PspMessage.create(deliverOption, telematikId, transactionId, null, "lovely tests ;-)");
        });
  }
}
