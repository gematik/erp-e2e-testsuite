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

package de.gematik.test.erezept.pspwsclient.dataobjects;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class WsMessageHandlerTest {

  static PspMessage pspMessage;
  static DeliveryOption deliverOption;
  static String telematikId;
  static String transactionId;
  static byte[] blob;

  @BeforeAll
  static void setup() {
    pspMessage = new PspMessage();
    deliverOption = DeliveryOption.ON_PREMISE;
    telematikId = "testID";
    transactionId = "tranacID";
    blob =
        "senselessUnidentifiedAndExtraLongNeedlessStringTo build an senseless unpracticed byteArray with a number : 1"
            .getBytes();
    pspMessage = PspMessage.create(deliverOption, telematikId, transactionId, blob, null);
  }

  @Test
  void encodeToJson() {
    WsMessageHandler wsMessageHandler = new WsMessageHandler();
    var testString = wsMessageHandler.encodeToJson(pspMessage);
    PspMessage result = null;
    try {
      result = new ObjectMapper().readValue(testString, PspMessage.class);
    } catch (JsonProcessingException e) {

    }
    assertEquals(new String(blob), new String(result.getBlob()));
  }
}
