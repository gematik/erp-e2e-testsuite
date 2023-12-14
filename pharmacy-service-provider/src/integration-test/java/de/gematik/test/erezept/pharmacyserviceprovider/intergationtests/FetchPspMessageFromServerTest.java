/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.pharmacyserviceprovider.intergationtests;

import de.gematik.test.erezept.pspwsclient.PSPClient;
import de.gematik.test.erezept.pspwsclient.PharmaServiceProviderWSClient;
import kong.unirest.Unirest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class FetchPspMessageFromServerTest {

  static String SERVER_AUTH;

  static final int WAIT_MILLIS_BEFORE_ASSERT = 300;
  static final int WAIT_MILLIS_BEFORE_SEND = 100;
  static final int WAIT_MILLIS_TO_START_STOP_SERVER = 1000;
  static final String TELEMATIK_ID = "Test2ID";
  static PSPClient clientEndPoint;
  static byte[] body;

  static String URL_WSS_GRIZZLY;
  static String SERVER_URL;

  static void fetchTargets() {
    IntegrationSetupTest.fetchTargets();
    SERVER_AUTH = IntegrationSetupTest.serverAuth;
    URL_WSS_GRIZZLY = IntegrationSetupTest.wssUrl;
    SERVER_URL = IntegrationSetupTest.serverUrl;
  }

  @SneakyThrows
  @BeforeAll
  static void setup() {
    fetchTargets();
    byte[] array = new byte[350 * 30];
    new Random().nextBytes(array);
    body = array;
  }


  @SneakyThrows
  @AfterAll
  static void closeConnection() {
      clientEndPoint.clearQueueOnServer();
      clientEndPoint.clearQueue();
      clientEndPoint.close();
  }


  @SneakyThrows
  private void sendMessages() {
    Unirest.post(SERVER_URL + "/delivery_only/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", TELEMATIK_ID, "transactionID", "11"))
            .header("X-Authorization", SERVER_AUTH)
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_SEND);
    Unirest.post(SERVER_URL + "/local_delivery/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "2", "transactionID", "22"))
            .header("X-Authorization", SERVER_AUTH)
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);
    Unirest.post(SERVER_URL + "/pick_up/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", TELEMATIK_ID, "transactionID", "33"))
        .header("X-Authorization", SERVER_AUTH)
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_SEND);

    Unirest.post(SERVER_URL + "/delivery_only/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", "4", "transactionID", "44"))
        .header("X-Authorization", SERVER_AUTH)
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_SEND);
    Unirest.post(SERVER_URL + "/pspmock/delivery/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", TELEMATIK_ID, "transactionID", "55-tu"))
        .header("X-Authorization", SERVER_AUTH)
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();

    Thread.currentThread().join(WAIT_MILLIS_BEFORE_SEND);
    Unirest.post(SERVER_URL + "/pspmock/abholen/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", "6", "transactionID", "66-tu"))
        .header("X-Authorization", SERVER_AUTH)
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();

    Thread.currentThread().join(WAIT_MILLIS_BEFORE_SEND);
    Unirest.post(SERVER_URL + "/pspmock/abholung/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", TELEMATIK_ID, "transactionID", "77_Ru"))
        .header("X-Authorization", SERVER_AUTH)
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();
  }


  @SneakyThrows
  @Test
  void fetchRecipesFormServer() {
    sendMessages();
    Thread.currentThread().join(WAIT_MILLIS_TO_START_STOP_SERVER);
    if (URL_WSS_GRIZZLY.toLowerCase().startsWith("wss")) {
      clientEndPoint = new PharmaServiceProviderWSClient(URL_WSS_GRIZZLY, TELEMATIK_ID, SERVER_AUTH);
    } else {
      clientEndPoint = new PharmaServiceProviderWSClient(URL_WSS_GRIZZLY, TELEMATIK_ID);
    }
    clientEndPoint.connectBlocking(WAIT_MILLIS_TO_START_STOP_SERVER, TimeUnit.MILLISECONDS);
    assertEquals(0, clientEndPoint.getQueueLength());
    clientEndPoint.callServerStoredMessages();
    int newQueueLength = clientEndPoint.getQueueLength();
    assertEquals(3, newQueueLength);
  }


}
