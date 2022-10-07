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

package de.gematik.test.erezept.pharmacyserviceprovider.endpoints;

import static de.gematik.test.erezept.pharmacyserviceprovider.Main.VERSIONNUMBER;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.pharmacyserviceprovider.Main;
import de.gematik.test.erezept.pharmacyserviceprovider.PSPServerContext;
import java.util.Map;
import kong.unirest.Unirest;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IntegrationUnitTest {
  static PSPServerContext pspServerContext;
  static byte[] body;
  static final int WAIT_MILLIS_TO_START_STOP_SERVER = 1000;

  @SneakyThrows
  @BeforeAll
  static void startServer() {
    Thread.currentThread().join(WAIT_MILLIS_TO_START_STOP_SERVER);
    pspServerContext = Main.generatePSPServers();
    pspServerContext.startPSPServerContext();
    body =
        "VeryLong useless and senseless String with a very important number as formally known as: 42 "
            .getBytes();
  }

  @SneakyThrows
  @AfterAll
  static void stopServer() {
    pspServerContext.shutDownNow();
    Thread.currentThread().join(WAIT_MILLIS_TO_START_STOP_SERVER);
  }

  // DeliveryEndpoint
  @Test
  void testDeliveryOnlyAllCorrect() {
    val resp =
        Unirest.post("http://localhost:9095/delivery_only/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(404, resp.getStatus());
    assertEquals("no fitted receiver connected @ specific TelematikId: testTeleID", resp.getBody());
  }

  @SneakyThrows
  @ParameterizedTest
  @CsvSource({
    "'http://localhost:9095/delivery_only?req={transactionID}', 404 ",
    "'http://localhost:9095/pspmock/mocked_apo/?req={transactionID}', 404",
    "'http://localhost:9095/pick_up?req={transactionID}', 404",
    "'http://localhost:9095/local_delivery?req={transactionID}', 404",
  })
  void shouldAnswer404NoTeleID(String send, int expect) {
    val resp =
        Unirest.post(send)
            .routeParam(Map.of("transactionID", "transactionTestID"))
            .body(body)
            .asString();
    assertEquals(expect, resp.getStatus());
  }

  @SneakyThrows
  @ParameterizedTest
  @CsvSource({
    "'http://localhost:9095/delivery_only', 404 ",
    "'http://localhost:9095/pspmock/mocked_apo', 404",
    "'http://localhost:9095/pick_up', 404",
    "'http://localhost:9095/local_delivery', 404",
  })
  void shouldAnswer404NoIds(String send, int expect) {
    val resp = Unirest.post(send).body(body).asString();
    assertEquals(expect, resp.getStatus());
  }

  @SneakyThrows
  @ParameterizedTest
  @CsvSource({
    "'http://localhost:9095/delivery_only', 404 ",
    "'http://localhost:9095/pspmock/mocked_apo', 404",
    "'http://localhost:9095/pick_up', 404",
    "'http://localhost:9095/local_delivery', 404",
  })
  void testFailOnlyoBody(String send, int respCode) {
    val resp = Unirest.post(send).header("Content-Type", "application/pkcs7-mime").asString();
    assertEquals(404, resp.getStatus());
  }

  // PspMockEndpiont
  @Test
  void testPspMockNoFittedReceiver() {
    val resp =
        Unirest.post("http://localhost:9095/pspmock/shipment/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(404, resp.getStatus());
    assertEquals("no fitted receiver connected @ specific TelematikId: testTeleID", resp.getBody());
  }

  @Test
  void testPspMockAllCorrectTestEnvIsTI() {
    val resp =
        Unirest.post("http://localhost:9095/pspmock/shipment/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(404, resp.getStatus());
    assertEquals("no fitted receiver connected @ specific TelematikId: testTeleID", resp.getBody());
  }

  @Test
  void testPspMockWrongDeliveryOption() {
    val resp =
        Unirest.post("http://localhost:9095/pspmock/shopping/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(404, resp.getStatus());
    assertEquals(
        "de.gematik.test.erezept.pspwsclient.dataobjects.InvalidDeliveryOptionException: no kind of deliver option matches",
        resp.getBody());
  }

  // PickUp
  @Test
  void testPickUpAllCorrect() {
    val resp =
        Unirest.post("http://localhost:9095/delivery_only/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(404, resp.getStatus());
    assertEquals("no fitted receiver connected @ specific TelematikId: testTeleID", resp.getBody());
  }

  // LocalDelivery
  @Test
  void testLocalDelivAllCorrect() {
    val resp =
        Unirest.post("http://localhost:9095/local_delivery/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(404, resp.getStatus());
    assertEquals("no fitted receiver connected @ specific TelematikId: testTeleID", resp.getBody());
  }

  // 200
  @Test
  void test200AllCorrect() {
    val resp =
        Unirest.post("http://localhost:9095/200/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(200, resp.getStatus());
    assertEquals("erfolgreiche Datenübermittlung", resp.getBody());
  }

  @SneakyThrows
  @ParameterizedTest
  @CsvSource({
    "'http://localhost:9095/200?req={transactionID}', 200, 'erfolgreiche Datenübermittlung, no telematikID' ",
    "'http://localhost:9095/201?req={transactionID}', 201, 'erfolgreiche Datenübermittlung, no telematikID' ",
    "'http://localhost:9095/300?req={transactionID}', 300, 'erfolgreiche Datenübermittlung, no telematikID' ",
    "'http://localhost:9095/401?req={transactionID}', 401, 'erfolgreiche Datenübermittlung, no telematikID' ",
    "'http://localhost:9095/408?req={transactionID}', 408, 'erfolgreiche Datenübermittlung, no telematikID' ",
    "'http://localhost:9095/409?req={transactionID}', 409, 'erfolgreiche Datenübermittlung, no telematikID' ",
    "'http://localhost:9095/410?req={transactionID}', 410, 'erfolgreiche Datenübermittlung, no telematikID' ",
    "'http://localhost:9095/500?req={transactionID}', 500, 'erfolgreiche Datenübermittlung, no telematikID' ",
  })
  void shouldAnswerNoTelematikId(String send, int expect, String expBody) {
    val resp =
        Unirest.post(send)
            .routeParam(Map.of("transactionID", "transactionTestID"))
            .body(body)
            .asString();
    assertEquals(expect, resp.getStatus());
    assertEquals(expBody, resp.getBody());
  }

  @SneakyThrows
  @ParameterizedTest
  @CsvSource({
    "'http://localhost:9095/200', 200 ",
    "'http://localhost:9095/201', 201",
    "'http://localhost:9095/300', 300",
    "'http://localhost:9095/401', 401",
    "'http://localhost:9095/408', 408",
    "'http://localhost:9095/409', 409",
    "'http://localhost:9095/500', 500",
  })
  void onlyBody(String send, int respCode) {
    val resp =
        Unirest.post(send).header("Content-Type", "application/pkcs7-mime").body(body).asString();
    assertEquals(respCode, resp.getStatus());
    assertEquals(
        "erfolgreiche Datenübermittlung, no telematikID, no transactionID arrived", resp.getBody());
  }

  @SneakyThrows
  @ParameterizedTest
  @CsvSource({
    "'http://localhost:9095/200', 404 ",
    "'http://localhost:9095/201', 404 ",
    "'http://localhost:9095/401', 404 ",
    "'http://localhost:9095/408', 404 ",
    "'http://localhost:9095/409', 404 ",
    "'http://localhost:9095/500', 404 ",
    "'http://localhost:9095/mock/200/', 404 ",
  })
  void testfail200NoBody(String url, int resCode) {
    val resp = Unirest.post(url).header("Content-Type", "application/pkcs7-mime").asString();
    assertEquals(resCode, resp.getStatus());
    assertEquals(
        "erfolgreiche Datenübermittlung, no telematikID, no transactionID arrived, no body arrived",
        resp.getBody());
  }

  // 201
  @Test
  void test201AllCorrect() {
    val resp =
        Unirest.post("http://localhost:9095/201/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(201, resp.getStatus());
    assertEquals("erfolgreiche Datenübermittlung", resp.getBody());
  }

  // 300
  @Test
  void test300AllCorrect() {
    val resp =
        Unirest.post("http://localhost:9095/300/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(300, resp.getStatus());
    assertEquals("erfolgreiche Datenübermittlung", resp.getBody());
  }

  // 401
  @Test
  void test401AllCorrect() {
    val resp =
        Unirest.post("http://localhost:9095/401/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(401, resp.getStatus());
    assertEquals("erfolgreiche Datenübermittlung", resp.getBody());
  }

  // 408
  @Test
  void test408AllCorrect() {
    val resp =
        Unirest.post("http://localhost:9095/408/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(408, resp.getStatus());
    assertEquals("erfolgreiche Datenübermittlung", resp.getBody());
  }

  // 409
  @Test
  void test409AllCorrect() {
    val resp =
        Unirest.post("http://localhost:9095/409/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(409, resp.getStatus());
    assertEquals("erfolgreiche Datenübermittlung", resp.getBody());
  }

  @Test
  void test409NoBodyNoID() {
    val resp =
        Unirest.post("http://localhost:9095/409")
            .header("Content-Type", "application/pkcs7-mime")
            .asString();
    assertEquals(404, resp.getStatus());
    assertEquals(
        "erfolgreiche Datenübermittlung, no telematikID, no transactionID arrived, no body arrived",
        resp.getBody());
  }

  // 410
  @Test
  void test410AllCorrect() {
    val resp =
        Unirest.post("http://localhost:9095/410/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(410, resp.getStatus());
    assertEquals("erfolgreiche Datenübermittlung", resp.getBody());
  }

  // 500
  @Test
  void test500AllCorrect() {
    val resp =
        Unirest.post("http://localhost:9095/500/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(500, resp.getStatus());
    assertEquals("erfolgreiche Datenübermittlung", resp.getBody());
  }

  // mock
  @Test
  void testMockAllCorrect() {
    val resp =
        Unirest.post("http://localhost:9095/mock/200/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(200, resp.getStatus());
    assertEquals("erfolgreiche Datenübermittlung", resp.getBody());
  }

  @Test
  void testMockNoTiId() {
    val resp =
        Unirest.post("http://localhost:9095/mock/200/?req={transactionID}")
            .routeParam(Map.of("transactionID", "transactionTestID"))
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(200, resp.getStatus());
    assertEquals("erfolgreiche Datenübermittlung, no telematikID", resp.getBody());
  }

  @Test
  void testMockNoTransactionID() {
    val resp =
        Unirest.post("http://localhost:9095/mock/200/{ti_id}")
            .routeParam(Map.of("ti_id", "testTeleID"))
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(200, resp.getStatus());
    assertEquals("erfolgreiche Datenübermittlung, no transactionID", resp.getBody());
  }

  @Test
  void testInfoEnpointTrue() {
    val response = Unirest.get("http://localhost:9095/info").asString();
    assertEquals(200, response.getStatus());
    assertEquals("actual Version: " + VERSIONNUMBER, response.getBody());
  }
}
