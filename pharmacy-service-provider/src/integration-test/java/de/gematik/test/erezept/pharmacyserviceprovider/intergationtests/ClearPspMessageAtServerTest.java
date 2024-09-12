/*
 * Copyright 2024 gematik GmbH
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

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.pspwsclient.PSPClient;
import de.gematik.test.erezept.pspwsclient.PharmaServiceProviderWSClient;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import kong.unirest.core.Unirest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
class ClearPspMessageAtServerTest {

  static final int WAIT_MILLIS_BEFORE_ASSERT = 300;
  static final int WAIT_MILLIS_TO_START_STOP_SERVER = 1000;
  static final String TELEMATIK_ID = "ABC123";
  // todo löschen
  static String SERVER_AUTH;
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
    body =
        "MIAGCyqGSIb3DQEJEAEXoIAwgAIBADGCAgMwggH/AgEAMIGmMIGaMQswCQYDVQQGEwJERTEfMB0GA1UECgwWZ2VtYXRpayBHbWJIIE5PVC1WQUxJRDFIMEYGA1UECww/SW5zdGl0dXRpb24gZGVzIEdlc3VuZGhlaXRzd2VzZW5zLUNBIGRlciBUZWxlbWF0aWtpbmZyYXN0cnVrdHVyMSAwHgYDVQQDDBdHRU0uU01DQi1DQTI0IFRFU1QtT05MWQIHAXLewUJXxjBNBgkqhkiG9w0BAQcwQKAPMA0GCWCGSAFlAwQCAQUAoRwwGgYJKoZIhvcNAQEIMA0GCWCGSAFlAwQCAQUAog8wDQYJKoZIhvcNAQEJBAAEggEATCCqlBorem3qG5w8Qbp5rJtI2Nm7juP/WDuJKmtKC6x9q+F4oN+5JOdqsYYgAlfpn+E/YBrirluWjZT4fkhRQ81xL6Ikt9hn+FeeuLhb8n/WszyxH6B0mrtRoaECqYCR2dxV8VqdICIq/LwgNXTyMtMivHALROh/ZuBRwq3GUZXyzjAwEN9Y1vmvAYuFcZOlCbo1JLHeH2FnZ6M6hBrYUiB6I3mIZerYp6NwbqppKq1g6BS73ZbLlneVz0eDR4xtDUhfU2kr68QP5buRHoqg6ZISdnGYdjWee+X9Da4Ag5qZiBVy3K2ac4BCX/DJ0e7vxeIMwTVDhea9/0qUKR4BOjCABgkqhkiG9w0BBwEwHgYJYIZIAWUDBAEuMBEEDHkFM+m1GukxWKWrFQIBEICCAbrnJ6ioDSFs5wrZtbsl9cR+vPbhNswk8peX88aihDecfZ/DaaJqhiYcaDZsRrWjwn/ukscAngZIjO41VuJDJEeGsuzFwQheC70uN6LEGQ8IwJyrpOmUjm0LiVr3UJSVzxvMVG02r/nCZjtNfFiEdvPxeQBTrRNJy32c/EGhSjvdPLOwYnd2kq0PKXyHJGhzKDaQnftWeyYjHvh+jiJ8mbnyOHiY/F6j27WSKIgJfml74NnQd7WqGnh+rCR7b6VTtBcLZ1t21zu94F3ZRtp6Fq/jbtjKx37vyfJPfSKCLFTs3vDsuRfAIH+sZacFedzDVQCmOF9jC7GBwqyB+bAnyxfJbS72+OZPqxaV3Yvtp9RjNYBxT0EEVEK9/PrFrmB5WizGg3sgh4jZOATL8boEjJgvapYlV9gd8l44NR7j79IzgkX5x7+vStZELLB48nQY+cRegT4b8l/flrYIEzvl7gYNqqF2rlWots3wcC2owCltQ+7+wOe0mhirWnjFdsvi1nOw2AVFqZRmBZGw5xqc2bhmN5fQYV5jo+WXKKxU+OtYX/OO8gWB5ICxDysToi4+777HKbpBEcfnpHvkAAAEENONKUWWIfpH19sRyry//AEAAAAAAAA="
            .getBytes();
  }

  @SneakyThrows
  @AfterAll
  static void closeConnection() {
    clientEndPoint.clearQueueOnServer();
    clientEndPoint.clearQueue();
    clientEndPoint.close();
  }

  static void sendMessages() {
    Unirest.post(SERVER_URL + "/delivery_only/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", TELEMATIK_ID, "transactionID", "11"))
        .header("X-Authorization", SERVER_AUTH)
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();
    Unirest.post(SERVER_URL + "/local_delivery/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", "2", "transactionID", "22"))
        .header("X-Authorization", SERVER_AUTH)
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();
    Unirest.post(SERVER_URL + "/pick_up/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", TELEMATIK_ID, "transactionID", "33"))
        .header("X-Authorization", SERVER_AUTH)
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();
    Unirest.post(SERVER_URL + "/delivery_only/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", "4", "transactionID", "44"))
        .header("X-Authorization", SERVER_AUTH)
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();
    Unirest.post(SERVER_URL + "/delivery_only/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", TELEMATIK_ID, "transactionID", "15"))
        .header("X-Authorization", SERVER_AUTH)
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();
    Unirest.post(SERVER_URL + "/pspmock/pick_up/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", TELEMATIK_ID, "transactionID", "55-tu"))
        .header("X-Authorization", SERVER_AUTH)
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();

    Unirest.post(SERVER_URL + "/pspmock/apo_amFlugahefen/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", "6", "transactionID", "66-tu"))
        .header("X-Authorization", SERVER_AUTH)
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();

    Unirest.post(SERVER_URL + "/pspmock/delivery_only/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", TELEMATIK_ID, "transactionID", "77_Ru"))
        .header("X-Authorization", SERVER_AUTH)
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();
  }

  @SneakyThrows
  @Test()
  void fetchRecipesFromServer() {
    sendMessages();
    log.info(
        "\n serverAuth: "
            + SERVER_AUTH
            + " \nServer_Url: "
            + SERVER_URL
            + " \n ws_URL: "
            + URL_WSS_GRIZZLY);
    if (URL_WSS_GRIZZLY.toLowerCase().startsWith("wss")) {
      clientEndPoint =
          new PharmaServiceProviderWSClient(URL_WSS_GRIZZLY, TELEMATIK_ID, SERVER_AUTH);
    } else {
      clientEndPoint = new PharmaServiceProviderWSClient(URL_WSS_GRIZZLY, TELEMATIK_ID);
    }
    clientEndPoint.connectBlocking(WAIT_MILLIS_TO_START_STOP_SERVER, TimeUnit.MILLISECONDS);
    clientEndPoint.callServerStoredMessages();
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);
    assertTrue(clientEndPoint.getQueueLength() > 1);
    log.info(format(" messages.length WS-ClientSide: {0} ", clientEndPoint.getQueueLength()));
    clientEndPoint.clearQueue();
    clientEndPoint.clearQueueOnServer();
    clientEndPoint.callServerStoredMessages();
    assertEquals(0, clientEndPoint.getQueueLength());
  }
}
