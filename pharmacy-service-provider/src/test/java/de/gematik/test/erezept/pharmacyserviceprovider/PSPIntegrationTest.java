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

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.pspwsclient.PharmaServiceProviderWSClient;
import de.gematik.test.erezept.pspwsclient.dataobjects.DeliveryOption;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import kong.unirest.Unirest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Slf4j
class PSPIntegrationTest {

  static final String WS_LOCALHOST_9095 = "ws://localhost:9095/";
  static PharmaServiceProviderWSClient clientEndPoint;
  static byte[] body;
  static final int WAIT_MILLIS_BEFORE_ASSERT = 300;
  static final int WAIT_MILLIS_BEFORE_SEND = 100;
  static final int WAIT_MILLIS_TO_START_STOP_SERVER = 1000;
  static PSPServerContext servers;
  static final String apoId = "apoAmFlughafen";

  @SneakyThrows
  @BeforeAll
  static void tryWsConnection() {
    Thread.currentThread().join(WAIT_MILLIS_TO_START_STOP_SERVER * 2);
    // starts backendServer
    servers = de.gematik.test.erezept.pharmacyserviceprovider.Main.generatePSPServers();
    // starts backendWsServerSide
    servers.startPSPServerContext();
    // open websocket
    clientEndPoint = new PharmaServiceProviderWSClient(WS_LOCALHOST_9095, apoId);
    clientEndPoint.connectBlocking(1, TimeUnit.SECONDS);
    final long finishTime = System.currentTimeMillis() + WAIT_MILLIS_TO_START_STOP_SERVER * 3;
    while (clientEndPoint.isConnected() && System.currentTimeMillis() <= finishTime) {
      log.info("..wait for clientEndPoint.isNotConnected()..");
      Thread.currentThread().join(500);
    }
    body =
        "MIAGCyqGSIb3DQEJEAEXoIAwgAIBADGCAgMwggH/AgEAMIGmMIGaMQswCQYDVQQGEwJERTEfMB0GA1UECgwWZ2VtYXRpayBHbWJIIE5PVC1WQUxJRDFIMEYGA1UECww/SW5zdGl0dXRpb24gZGVzIEdlc3VuZGhlaXRzd2VzZW5zLUNBIGRlciBUZWxlbWF0aWtpbmZyYXN0cnVrdHVyMSAwHgYDVQQDDBdHRU0uU01DQi1DQTI0IFRFU1QtT05MWQIHAXLewUJXxjBNBgkqhkiG9w0BAQcwQKAPMA0GCWCGSAFlAwQCAQUAoRwwGgYJKoZIhvcNAQEIMA0GCWCGSAFlAwQCAQUAog8wDQYJKoZIhvcNAQEJBAAEggEATCCqlBorem3qG5w8Qbp5rJtI2Nm7juP/WDuJKmtKC6x9q+F4oN+5JOdqsYYgAlfpn+E/YBrirluWjZT4fkhRQ81xL6Ikt9hn+FeeuLhb8n/WszyxH6B0mrtRoaECqYCR2dxV8VqdICIq/LwgNXTyMtMivHALROh/ZuBRwq3GUZXyzjAwEN9Y1vmvAYuFcZOlCbo1JLHeH2FnZ6M6hBrYUiB6I3mIZerYp6NwbqppKq1g6BS73ZbLlneVz0eDR4xtDUhfU2kr68QP5buRHoqg6ZISdnGYdjWee+X9Da4Ag5qZiBVy3K2ac4BCX/DJ0e7vxeIMwTVDhea9/0qUKR4BOjCABgkqhkiG9w0BBwEwHgYJYIZIAWUDBAEuMBEEDHkFM+m1GukxWKWrFQIBEICCAbrnJ6ioDSFs5wrZtbsl9cR+vPbhNswk8peX88aihDecfZ/DaaJqhiYcaDZsRrWjwn/ukscAngZIjO41VuJDJEeGsuzFwQheC70uN6LEGQ8IwJyrpOmUjm0LiVr3UJSVzxvMVG02r/nCZjtNfFiEdvPxeQBTrRNJy32c/EGhSjvdPLOwYnd2kq0PKXyHJGhzKDaQnftWeyYjHvh+jiJ8mbnyOHiY/F6j27WSKIgJfml74NnQd7WqGnh+rCR7b6VTtBcLZ1t21zu94F3ZRtp6Fq/jbtjKx37vyfJPfSKCLFTs3vDsuRfAIH+sZacFedzDVQCmOF9jC7GBwqyB+bAnyxfJbS72+OZPqxaV3Yvtp9RjNYBxT0EEVEK9/PrFrmB5WizGg3sgh4jZOATL8boEjJgvapYlV9gd8l44NR7j79IzgkX5x7+vStZELLB48nQY+cRegT4b8l/flrYIEzvl7gYNqqF2rlWots3wcC2owCltQ+7+wOe0mhirWnjFdsvi1nOw2AVFqZRmBZGw5xqc2bhmN5fQYV5jo+WXKKxU+OtYX/OO8gWB5ICxDysToi4+777HKbpBEcfnpHvkAAAEENONKUWWIfpH19sRyry//AEAAAAAAAA="
            .getBytes();
  }

  @SneakyThrows
  @AfterAll
  static void closeConnection() {
    clientEndPoint.close();
    servers.shutDownNow();
    Thread.currentThread().join(WAIT_MILLIS_TO_START_STOP_SERVER);
  }

  static void sendUnirestPostWithTwoIdsAndBody(String url) {
    Unirest.post(url)
        .routeParam(Map.of("ti_id", apoId, "transactionID", "transactionTestID"))
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();
  }

  static void sendUnirestPostToMockEndpiont(String mockApo) {
    Unirest.post(format("http://localhost:9095/pspmock/%s/{ti_id}?req={transactionID}", mockApo))
        .routeParam(Map.of("ti_id", apoId, "transactionID", "transactionTestID"))
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();
  }

  @SneakyThrows
  @BeforeEach
  void clearMessage() {
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_SEND);
    clientEndPoint.clearQueue();
  }

  @SneakyThrows
  @Test
  void expectCorrectWsResponse() {
    clientEndPoint.clearQueue();
    sendUnirestPostToMockEndpiont("shipment");
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);
    assertEquals(
        DeliveryOption.SHIPMENT,
        clientEndPoint.consumeOldest(1000).orElseThrow().getDeliveryOption());
  }

  @SneakyThrows
  @Test
  void expect404WsResponse() {
    clientEndPoint.clearQueue();
    sendUnirestPostToMockEndpiont("liefern");
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);
    assertFalse(clientEndPoint.hasMessage());
  }

  @SneakyThrows
  @Test
  void testPickUpWsConnectionShouldAnsTwoIds() {
    clientEndPoint.clearQueue();
    sendUnirestPostWithTwoIdsAndBody("http://localhost:9095/pick_up/{ti_id}?req={transactionID}");
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);

    assertTrue(clientEndPoint.hasMessage());
    val optionalMessage = clientEndPoint.consumeOldest(1000);
    Assertions.assertEquals(
        DeliveryOption.ON_PREMISE, optionalMessage.orElseThrow().getDeliveryOption());
    assertTrue(optionalMessage.isPresent());
    val message = optionalMessage.orElseThrow();
    assertEquals(body.length, message.getBlob().length);
  }

  @SneakyThrows
  @ParameterizedTest
  @CsvSource({
    "'http://localhost:9095/pspmock/versand/{ti_id}?req={transactionID}', 'arrived @ SHIPMENT' ",
    "'http://localhost:9095/local_delivery/{ti_id}?req={transactionID}', 'arrived @ DELIVERY' ",
    "'http://localhost:9095/delivery_only/{ti_id}?req={transactionID}', 'arrived @ SHIPMENT' ",
  })
  void shouldAnswer(String send, String expect) {
    sendUnirestPostWithTwoIdsAndBody(send);
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);
    Assertions.assertEquals(
        expect,
        clientEndPoint.consumeOldest(1000).orElseThrow(() -> new AssertionError()).getNote());
  }

  // QueueTests
  @SneakyThrows
  @Test
  void testQueueShouldBeSizeOfTree() {
    clientEndPoint.clearQueue();
    sendUnirestPostWithTwoIdsAndBody(
        "http://localhost:9095/delivery_only/{ti_id}?req={transactionID}");

    sendUnirestPostWithTwoIdsAndBody(
        "http://localhost:9095/delivery_only/{ti_id}?req={transactionID}");

    sendUnirestPostWithTwoIdsAndBody(
        "http://localhost:9095/delivery_only/{ti_id}?req={transactionID}");

    Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);
    assertEquals(3, clientEndPoint.getQueueLength());
  }

  @SneakyThrows
  @Test
  void clearQueueShoulWork() {
    sendUnirestPostWithTwoIdsAndBody(
        "http://localhost:9095/delivery_only/{ti_id}?req={transactionID}");
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);
    clientEndPoint.clearQueue();
    assertEquals(0, clientEndPoint.getQueueLength());
  }

  @SneakyThrows
  @Test
  void consumeOldestWorksCorrect() {
    sendUnirestPostWithTwoIdsAndBody(
        "http://localhost:9095/delivery_only/{ti_id}?req={transactionID}");
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_SEND);

    sendUnirestPostWithTwoIdsAndBody(
        "http://localhost:9095/delivery_only/{ti_id}?req={transactionID}");
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_SEND);

    sendUnirestPostWithTwoIdsAndBody(
        "http://localhost:9095/delivery_only/{ti_id}?req={transactionID}");
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_SEND);

    Unirest.post("http://localhost:9095/pspmock/shipment/{ti_id}?req={transactionID}")
        .routeParam(Map.of("ti_id", apoId, "transactionID", "44"))
        .header("Content-Type", "application/pkcs7-mime")
        .body(body)
        .asString();

    Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);
    try {
      val recipeData = clientEndPoint.consumeOldest(1000);
      assertEquals("44", recipeData.orElseThrow(() -> new AssertionError()).getTransactionId());
    } catch (IndexOutOfBoundsException e) {
      throw new AssertionError(e);
    }
  }

  @Test
  void consumeOldestWithEmptyQueueThrowsIndexOutOfBoundsException2() {
    clientEndPoint.clearQueue();
    assertTrue(clientEndPoint.consumeOldest().isEmpty());
  }

  @SneakyThrows
  @Test
  void hasMassage() {
    clientEndPoint.clearQueue();
    sendUnirestPostWithTwoIdsAndBody(
        "http://localhost:9095/pspmock/shipment/{ti_id}?req={transactionID}");
    Thread.currentThread().join(WAIT_MILLIS_BEFORE_ASSERT);
    assertTrue(clientEndPoint.hasMessage());
  }
}
