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

package de.gematik.test.erezept.pspwsclient.dataobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PspMessageTest {
  static PspMessage pspMessage;
  static DeliveryOption deliverOption;
  static String telematikId;
  static String transactionId;
  static byte[] blob;

  @BeforeAll
  static void setup() {
    pspMessage = new PspMessage();
    deliverOption = DeliveryOption.SHIPMENT;
    telematikId = "testTelemID";
    transactionId = "testTranacID";
    blob =
        "senselessUnidentifiedAndExtraLongNeedlessStringTo build an senseless unpracticed byteArray with a number : 1"
            .getBytes();
    pspMessage = PspMessage.create(deliverOption, telematikId, transactionId, blob, null);
  }

  @Test
  void generateByteBlobCorrectTelemID() {
    assertEquals(telematikId, pspMessage.getClientId());
  }

  @Test
  void generateByteBlobCorrectTelemIDIsNull() {
    assertThrows(
        NullPointerException.class,
        () -> {
          PspMessage.create(deliverOption, null, transactionId, blob, "");
        });
  }

  @Test
  void generateNullMessage() {
    PspMessage message = PspMessage.create(deliverOption, telematikId, transactionId, blob);
    assertNull(message.getNote());
  }

  @Test
  void generateByteBlobCorrectTransactionIdIsNull() {
    PspMessage recipedata = PspMessage.create(deliverOption, telematikId, null, blob, "");
    assertNull(recipedata.getTransactionId());
  }

  @Test
  void generateByteBlobCorrectApoURLIsNull() {
    PspMessage recipedata = PspMessage.create(null, telematikId, transactionId, blob, "");
    assertNull(recipedata.getDeliveryOption());
  }

  @Test
  void generateByteBlobCorrectByteBlobIsNull() {
    assertThrows(
        NullPointerException.class,
        () -> {
          PspMessage.create(deliverOption, telematikId, transactionId, null, "");
        });
  }
}
