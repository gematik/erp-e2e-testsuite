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

package de.gematik.test.erezept.pharmacyserviceprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.pspwsclient.PSPClient;
import de.gematik.test.erezept.pspwsclient.PharmaServiceProviderWSClient;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import kong.unirest.Unirest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class FetchPspMessageFromServerTest {

  static PSPClient clientEndPoint;
  static final String URLtoGrizzlyWS = "ws://localhost:9095/";
  static byte[] body;
  static final int WAIT_MILLIS_BEFORE_ASSERT = 300;
  static final int WAIT_MILLIS_BEFORE_SEND = 100;
  static final int WAIT_MILLIS_TO_START_STOP_SERVER = 1000;
  static PSPServerContext servers;
  static final String TELEMATIK_ID = "ABC123";

  @SneakyThrows
  @BeforeAll
  static void setup() {
    Thread.currentThread().join(WAIT_MILLIS_TO_START_STOP_SERVER);
    // starts backendServer
    servers = de.gematik.test.erezept.pharmacyserviceprovider.Main.generatePSPServers();
    // starts backendWsServerSide
    servers.startPSPServerContext();
    body =
        "MIAGCyqGSIb3DQEJEAEXoIAwgAIBADGCAgMwggH/AgEAMIGmMIGaMQswCQYDVQQGEwJERTEfMB0GA1UECgwWZ2VtYXRpayBHbWJIIE5PVC1WQUxJRDFIMEYGA1UECww/SW5zdGl0dXRpb24gZGVzIEdlc3VuZGhlaXRzd2VzZW5zLUNBIGRlciBUZWxlbWF0aWtpbmZyYXN0cnVrdHVyMSAwHgYDVQQDDBdHRU0uU01DQi1DQTI0IFRFU1QtT05MWQIHAXLewUJXxjBNBgkqhkiG9w0BAQcwQKAPMA0GCWCGSAFlAwQCAQUAoRwwGgYJKoZIhvcNAQEIMA0GCWCGSAFlAwQCAQUAog8wDQYJKoZIhvcNAQEJBAAEggEATCCqlBorem3qG5w8Qbp5rJtI2Nm7juP/WDuJKmtKC6x9q+F4oN+5JOdqsYYgAlfpn+E/YBrirluWjZT4fkhRQ81xL6Ikt9hn+FeeuLhb8n/WszyxH6B0mrtRoaECqYCR2dxV8VqdICIq/LwgNXTyMtMivHALROh/ZuBRwq3GUZXyzjAwEN9Y1vmvAYuFcZOlCbo1JLHeH2FnZ6M6hBrYUiB6I3mIZerYp6NwbqppKq1g6BS73ZbLlneVz0eDR4xtDUhfU2kr68QP5buRHoqg6ZISdnGYdjWee+X9Da4Ag5qZiBVy3K2ac4BCX/DJ0e7vxeIMwTVDhea9/0qUKR4BOjCABgkqhkiG9w0BBwEwHgYJYIZIAWUDBAEuMBEEDHkFM+m1GukxWKWrFQIBEICCAbrnJ6ioDSFs5wrZtbsl9cR+vPbhNswk8peX88aihDecfZ/DaaJqhiYcaDZsRrWjwn/ukscAngZIjO41VuJDJEeGsuzFwQheC70uN6LEGQ8IwJyrpOmUjm0LiVr3UJSVzxvMVG02r/nCZjtNfFiEdvPxeQBTrRNJy32c/EGhSjvdPLOwYnd2kq0PKXyHJGhzKDaQnftWeyYjHvh+jiJ8mbnyOHiY/F6j27WSKIgJfml74NnQd7WqGnh+rCR7b6VTtBcLZ1t21zu94F3ZRtp6Fq/jbtjKx37vyfJPfSKCLFTs3vDsuRfAIH+sZacFedzDVQCmOF9jC7GBwqyB+bAnyxfJbS72+OZPqxaV3Yvtp9RjNYBxT0EEVEK9/PrFrmB5WizGg3sgh4jZOATL8boEjJgvapYlV9gd8l44NR7j79IzgkX5x7+vStZELLB48nQY+cRegT4b8l/flrYIEzvl7gYNqqF2rlWots3wcC2owCltQ+7+wOe0mhirWnjFdsvi1nOw2AVFqZRmBZGw5xqc2bhmN5fQYV5jo+WXKKxU+OtYX/OO8gWB5ICxDysToi4+777HKbpBEcfnpHvkAAAEENONKUWWIfpH19sRyry//AEAAAAAAAA="
            .getBytes();
    Thread.currentThread().join(WAIT_MILLIS_TO_START_STOP_SERVER);
  }

  @SneakyThrows
  @AfterAll
  static void closeConnection() {
    servers.shutDownNow();
  }

  @SneakyThrows
  @BeforeEach
  void sendMessages() {
    Unirest.post("http://localhost:9095/delivery_only/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", TELEMATIK_ID, "transactionID", "11"))
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_SEND);
    Unirest.post("http://localhost:9095/local_delivery/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", "2", "transactionID", "22"))
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);
    Unirest.post("http://localhost:9095/pick_up/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", TELEMATIK_ID, "transactionID", "33"))
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_SEND);

    Unirest.post("http://localhost:9095/delivery_only/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", "4", "transactionID", "44"))
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_SEND);
    Unirest.post("http://localhost:9095/pspmock/variableEndpoint/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", TELEMATIK_ID, "transactionID", "55-tu"))
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();

    Thread.currentThread().join(WAIT_MILLIS_BEFORE_SEND);
    Unirest.post("http://localhost:9095/pspmock/apo_amFlugahefen/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", "6", "transactionID", "66-tu"))
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();

    Thread.currentThread().join(WAIT_MILLIS_BEFORE_SEND);
    Unirest.post(
            "http://localhost:9095/pspmock/internettapoMitLieferdienst/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", TELEMATIK_ID, "transactionID", "77_Ru"))
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();
  }

  @SneakyThrows
  @Test
  void fetchRecipesFormServer() {
    Thread.currentThread().join(WAIT_MILLIS_TO_START_STOP_SERVER);
    clientEndPoint = new PharmaServiceProviderWSClient(URLtoGrizzlyWS, TELEMATIK_ID);
    clientEndPoint.connectBlocking(WAIT_MILLIS_TO_START_STOP_SERVER, TimeUnit.MILLISECONDS);
    assertEquals(0, clientEndPoint.getQueueLength());
    clientEndPoint.callServerStoredMessages();
    // wait for serverResponse
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT * 4);
    int newQueueLength = clientEndPoint.getQueueLength();
    assertEquals(2, newQueueLength);
  }
}
