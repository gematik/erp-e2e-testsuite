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

import static de.gematik.test.erezept.pharmacyserviceprovider.Main.VERSIONNUMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Random;
import kong.unirest.core.Unirest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Slf4j
class ServerEndpointIntegrationTest {

  static String SERVER_AUTH;
  static byte[] body;

  static String SERVER_URL;

  static void fetchTargets() {
    IntegrationSetupTest.fetchTargets();
    SERVER_AUTH = IntegrationSetupTest.serverAuth;
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


  // DeliveryEndpoint
  @Test
  void testDeliveryOnlyAllCorrect() {
    val resp =
            Unirest.post(SERVER_URL + "/delivery_only/{ti_id}?req={transactionID}")
                    .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
                    .header("X-Authorization", SERVER_AUTH)
                    .header("Content-Type", "application/pkcs7-mime")
                    .body(body)
                    .asString();
    assertEquals(200, resp.getStatus());
    assertEquals("no fitted receiver connected @ specific TelematikId: testTeleID", resp.getBody());
  }

  @SneakyThrows
  @ParameterizedTest
  @CsvSource({
          "'/delivery_only?req={transactionID}', 404 ",
          "'/pspmock/liefern/?req={transactionID}', 404",
          "'/pick_up?req={transactionID}', 404",
          "'/local_delivery?req={transactionID}', 404",
  })
  void shouldAnswer404Or200NoTeleID(String send, int expect) {
    val resp =
            Unirest.post(SERVER_URL + send)
                    .header("X-Authorization", SERVER_AUTH)
                    .routeParam(Map.of("transactionID", "transactionTestID"))
                    .body(body)
                    .asString();
    assertEquals(expect, resp.getStatus());
  }

  @SneakyThrows
  @ParameterizedTest
  @CsvSource({
          "'/delivery_only', 404 ",
          "'/pspmock/pick_up', 404",
          "'/pick_up', 404",
          "'/local_delivery', 404",
  })
  void shouldAnswer404NoIds(String send, int expect) {
    val resp =
            Unirest.post(SERVER_URL + send)
                    .header("X-Authorization", SERVER_AUTH)
                    .body(body)
                    .asString();
    assertEquals(expect, resp.getStatus());
  }

  @SneakyThrows
  @ParameterizedTest
  @CsvSource({
          "'/delivery_only', 404 ",
          "'/pspmock/pick_up', 404",
          "'/pick_up', 404",
          "'/local_delivery', 404",
  })
  void testNotOnlyBody(String route, int respCode) {
    val resp =
            Unirest.post(SERVER_URL + route)
                    .header("X-Authorization", SERVER_AUTH)
                    .header("Content-Type", "application/pkcs7-mime")
                    .body(body)
                    .asString();
    assertEquals(respCode, resp.getStatus());
  }

  // PspMockEndpiont
  @Test
  void testPspMockNoFittedReceiver() {
    val resp =
        Unirest.post(SERVER_URL + "/pspmock/shipment/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("X-Authorization", SERVER_AUTH)
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(200, resp.getStatus());
    assertEquals("no fitted receiver connected @ specific TelematikId: testTeleID", resp.getBody());
  }

  @Test
  void testPspMockAllCorrectTestEnvIsTI() {
    val resp =
        Unirest.post(SERVER_URL + "/pspmock/shipment/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("X-Authorization", SERVER_AUTH)
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(200, resp.getStatus());
    assertEquals("no fitted receiver connected @ specific TelematikId: testTeleID", resp.getBody());
  }

  @Test
  void testPspMockWrongDeliveryOption() {
    val resp =
        Unirest.post(SERVER_URL + "/pspmock/shopping/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("X-Authorization", SERVER_AUTH)
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
        Unirest.post(SERVER_URL + "/delivery_only/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("X-Authorization", SERVER_AUTH)
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(200, resp.getStatus());
    assertEquals("no fitted receiver connected @ specific TelematikId: testTeleID", resp.getBody());
  }

  // LocalDelivery
  @Test
  void testLocalDelivAllCorrect() {
    val resp =
        Unirest.post(SERVER_URL + "/local_delivery/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("X-Authorization", SERVER_AUTH)
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(200, resp.getStatus());
    assertEquals("no fitted receiver connected @ specific TelematikId: testTeleID", resp.getBody());
  }

  // 200
  @Test
  void test200AllCorrect() {
    val resp =
        Unirest.post(SERVER_URL + "/200/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("X-Authorization", SERVER_AUTH)
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(200, resp.getStatus());
    assertEquals("erfolgreiche Datenübermittlung", resp.getBody());
  }

  @SneakyThrows
  @ParameterizedTest
  @CsvSource({
    "'/200?req={transactionID}', 200, 'erfolgreiche Datenübermittlung, no telematikID' ",
    "'/201?req={transactionID}', 201, 'erfolgreiche Datenübermittlung, no telematikID' ",
    "'/300?req={transactionID}', 300, 'erfolgreiche Datenübermittlung, no telematikID' ",
    "'/401?req={transactionID}', 401, 'erfolgreiche Datenübermittlung, no telematikID' ",
    "'/408?req={transactionID}', 408, 'erfolgreiche Datenübermittlung, no telematikID' ",
    "'/409?req={transactionID}', 409, 'erfolgreiche Datenübermittlung, no telematikID' ",
    "'/410?req={transactionID}', 410, 'erfolgreiche Datenübermittlung, no telematikID' ",
    "'/500?req={transactionID}', 500, 'erfolgreiche Datenübermittlung, no telematikID' ",
  })
  void shouldAnswerNoTelematikId(String send, int expect, String expBody) {
    val resp =
        Unirest.post(SERVER_URL + send)
            .header("X-Authorization", SERVER_AUTH)
            .routeParam(Map.of("transactionID", "transactionTestID"))
            .body(body)
            .asString();
    assertEquals(expect, resp.getStatus());
    assertEquals(expBody, resp.getBody());
  }

  @SneakyThrows
  @ParameterizedTest
  @CsvSource({
    "'/200', 200 ",
    "'/201', 201",
    "'/300', 300",
    "'/401', 401",
    "'/408', 408",
    "'/409', 409",
    "'/500', 500",
  })
  void onlyBody(String send, int respCode) {
    val resp =
        Unirest.post(SERVER_URL + send)
            .header("X-Authorization", SERVER_AUTH)
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(respCode, resp.getStatus());
    assertEquals(
        "erfolgreiche Datenübermittlung, no telematikID, no transactionID arrived", resp.getBody());
  }

  @SneakyThrows
  @ParameterizedTest
  @CsvSource({
    "'/200', 404 ",
    "'/201', 404 ",
    "'/401', 404 ",
    "'/408', 404 ",
    "'/409', 404 ",
    "'/500', 404 ",
    "'/mock/200/', 404 ",
  })
  void testfail200NoBody(String path, int resCode) {
    val resp =
        Unirest.post(SERVER_URL + path)
            .header("X-Authorization", SERVER_AUTH)
            .header("Content-Type", "application/pkcs7-mime")
            .asString();
    assertEquals(resCode, resp.getStatus());
    assertEquals(
        "erfolgreiche Datenübermittlung, no telematikID, no transactionID arrived, no body arrived",
        resp.getBody());
  }

  // 201
  @Test
  void test201AllCorrect() {
    val resp =
        Unirest.post(SERVER_URL + "/201/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("X-Authorization", SERVER_AUTH)
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
        Unirest.post(SERVER_URL + "/300/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("X-Authorization", SERVER_AUTH)
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
        Unirest.post(SERVER_URL + "/401/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("X-Authorization", SERVER_AUTH)
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
        Unirest.post(SERVER_URL + "/408/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("X-Authorization", SERVER_AUTH)
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
        Unirest.post(SERVER_URL + "/409/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("X-Authorization", SERVER_AUTH)
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(409, resp.getStatus());
    assertEquals("erfolgreiche Datenübermittlung", resp.getBody());
  }

  @Test
  void test409NoBodyNoID() {
    val resp =
        Unirest.post(SERVER_URL + "/409")
            .header("X-Authorization", SERVER_AUTH)
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
        Unirest.post(SERVER_URL + "/410/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("X-Authorization", SERVER_AUTH)
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
        Unirest.post(SERVER_URL + "/500/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("X-Authorization", SERVER_AUTH)
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
        Unirest.post(SERVER_URL + "/mock/200/{ti_id}?req={transactionID}")
            .routeParam(Map.of("ti_id", "testTeleID", "transactionID", "transactionTestID"))
            .header("X-Authorization", SERVER_AUTH)
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(200, resp.getStatus());
    assertEquals("erfolgreiche Datenübermittlung", resp.getBody());
  }

  @Test
  void testMockNoTiId() {
    val resp =
        Unirest.post(SERVER_URL + "/mock/200/?req={transactionID}")
            .routeParam(Map.of("transactionID", "transactionTestID"))
            .header("X-Authorization", SERVER_AUTH)
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(200, resp.getStatus());
    assertEquals("erfolgreiche Datenübermittlung, no telematikID", resp.getBody());
  }

  @Test
  void testMockNoTransactionID() {
    val resp =
        Unirest.post(SERVER_URL + "/mock/200/{ti_id}")
            .routeParam(Map.of("ti_id", "testTeleID"))
            .header("X-Authorization", SERVER_AUTH)
            .header("Content-Type", "application/pkcs7-mime")
            .body(body)
            .asString();
    assertEquals(200, resp.getStatus());
    assertEquals("erfolgreiche Datenübermittlung, no transactionID", resp.getBody());
  }

  @Test
  void testInfoEnpointTrue() {
    val response =
            Unirest.get(SERVER_URL + "/info").header("X-Authorization", SERVER_AUTH).asString();
    assertEquals(200, response.getStatus());
    assertEquals("actual Version: " + VERSIONNUMBER, response.getBody());
  }



  @SneakyThrows
  @ParameterizedTest
  @CsvSource({
          "'/delivery_only/testApo123', 404 , 'blob == null or empty'",
          "'/pspmock/pick_up/testApo123', 404, 'blob == null or empty'",
          "'/pick_up/testApo123', 404, 'blob == null or empty'",
          "'/local_delivery/testApo123', 404, 'blob == null or empty'",
  })
  void shouldAnswer404NoIdsNoBlob(String send, int expect, String expectedString) {
    val resp =
            Unirest.post(SERVER_URL + send)
                    .header("X-Authorization", SERVER_AUTH)
                    .asString();
    assertEquals(expect, resp.getStatus());
    assertEquals(expectedString, resp.getBody());
  }

}
