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

package de.gematik.test.erezept.client.vau;

import static de.gematik.test.erezept.client.testutils.VauCertificateGenerator.generateX509Certificate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.vau.protocol.VauVersion;
import de.gematik.test.erezept.crypto.BC;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import kong.unirest.*;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.*;
import org.mockito.Answers;
import org.mockito.MockedStatic;

class VauClientTest {

  private static X509Certificate vauCertificate;
  private MockedStatic<Unirest> unitRestMock;

  @BeforeAll
  static void init() throws Exception {
    val keyPairGenerator = KeyPairGenerator.getInstance("EC", BC.getSecurityProvider());
    keyPairGenerator.initialize(new ECGenParameterSpec(VauVersion.V1.getCurve()));
    val keyPair = keyPairGenerator.generateKeyPair();
    vauCertificate = generateX509Certificate(keyPair.getPrivate(), keyPair.getPublic());
  }

  @BeforeEach
  void setUp() {
    unitRestMock = mockStatic(Unirest.class, Answers.RETURNS_DEEP_STUBS);
    when(Unirest.primaryInstance()).thenReturn(new UnirestInstance(new Config()));
  }

  private void prepareEndpointVauCertificate(byte[] responseData) {
    when(Unirest.get("https://erp/VAUCertificate").asBytes().getBody()).thenReturn(responseData);
  }

  private void prepareResponseVau(byte[] resBody, String... resHeader) {
    prepareResponseVau(resBody, 200, resHeader);
  }

  @SuppressWarnings("unchecked")
  private void prepareResponseVau(byte[] resBody, int returnCode, String... resHeader) {
    val httpResponseMock = mock(HttpResponse.class);
    when(Unirest.post("https://erp/VAU/0")
            .header("Content-Type", "application/octet-stream")
            .header(eq(VauHeader.X_ERP_USER.getValue()), anyString())
            .body(any(byte[].class))
            .asBytes())
        .thenReturn(httpResponseMock);

    // mock response status code
    when(httpResponseMock.getStatus()).thenReturn(returnCode);

    // mock response body
    when(httpResponseMock.getBody()).thenReturn(resBody);

    // mock response headers
    val headers = new Headers();
    for (int i = 0; i < resHeader.length; i += 2) {
      headers.add(resHeader[i], resHeader[i + 1]);
    }
    when(httpResponseMock.getHeaders()).thenReturn(headers);
  }

  @SneakyThrows
  @Test
  void shouldHandleResponse() {
    prepareEndpointVauCertificate(vauCertificate.getEncoded());
    prepareResponseVau(
        "Nobody calls me chicken".getBytes(StandardCharsets.UTF_8),
        "Userpseudonym",
        "0",
        "X-Request-Id",
        "testRequestId-123456");

    val vau =
        new VauClient("https://erp", ClientType.FDV, vauCertificate, "testApiKey", "testAgent");
    vau.initialize();
    val response = vau.send("What's wrong, McFly? Chicken!", "testToken", "/Task");

    assertEquals(200, response.getStatusCode());
    assertEquals("Nobody calls me chicken", response.getBody());
    assertEquals("testRequestId-123456", response.getHeader().get("X-Request-Id"));
  }

  @SneakyThrows
  @Test
  void shouldHandleUnencryptedResponse500() {
    prepareEndpointVauCertificate(vauCertificate.getEncoded());
    prepareResponseVau(
        "Nobody calls me chicken".getBytes(StandardCharsets.UTF_8),
        500,
        "Userpseudonym",
        "0",
        "content-type",
        "octet-stream",
        "X-Request-Id",
        "testRequestId-123456");

    val vau =
        new VauClient("https://erp", ClientType.PS, vauCertificate, "testApiKey", "testAgent");
    vau.initialize();
    assertThrows(
        VauException.class, () -> vau.send("What's wrong, McFly? Chicken!", "testToken", "/Task"));
  }

  @SneakyThrows
  @Test
  void shouldNotFailIfUserpseudonymMissing() {
    prepareEndpointVauCertificate(vauCertificate.getEncoded());
    prepareResponseVau("Nobody calls me chicken".getBytes(StandardCharsets.UTF_8));

    try {
      val vau = new VauClient("https://erp", ClientType.PS, vauCertificate, "", "");
      vau.initialize();
      vau.send("What's wrong, McFly? Chicken!", "", null);
    } catch (VauException e) {
      fail();
    }
  }

  @SneakyThrows
  @Test
  void shouldNotFailIfUseragentMissing() {
    prepareEndpointVauCertificate(vauCertificate.getEncoded());
    prepareResponseVau("Nobody calls me chicken".getBytes(StandardCharsets.UTF_8));

    try {
      val vau = new VauClient("https://erp", ClientType.PS, vauCertificate, null, null);
      vau.initialize();
      vau.send("What's wrong, McFly? Chicken!", "", null);
    } catch (VauException e) {
      fail();
    }
  }

  @SneakyThrows
  @Test
  void shouldFailIfBaseUrlMissing() {
    prepareEndpointVauCertificate(vauCertificate.getEncoded());
    assertThrows(
        NullPointerException.class,
        () -> new VauClient(null, ClientType.PS, vauCertificate, null, null));
  }

  @SneakyThrows
  @Test
  void shouldFailIfClientTypeMissing() {
    prepareEndpointVauCertificate(vauCertificate.getEncoded());
    assertThrows(
        NullPointerException.class,
        () -> new VauClient("https://erp", null, vauCertificate, null, null));
  }

  @SneakyThrows
  @Test
  void shouldFailIfVauCertificateMissing() {
    prepareEndpointVauCertificate(vauCertificate.getEncoded());
    assertThrows(
        NullPointerException.class,
        () -> new VauClient("https://erp", ClientType.PS, null, null, null));
  }

  @AfterEach
  void resetMocks() {
    unitRestMock.closeOnDemand();
  }
}
