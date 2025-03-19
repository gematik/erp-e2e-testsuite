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
 */

package de.gematik.test.erezept.client.vau;

import static de.gematik.test.erezept.client.testutils.VauCertificateGenerator.generateX509Certificate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.crypto.BC;
import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.vau.protocol.VauVersion;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import javax.net.ssl.SSLContext;
import kong.unirest.core.Headers;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestInstance;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

class VauClientTest {

  private static X509Certificate vauCertificate;
  private UnirestInstance unitRestMock;

  @BeforeAll
  static void init() throws Exception {
    val keyPairGenerator = KeyPairGenerator.getInstance("EC", BC.getSecurityProvider());
    keyPairGenerator.initialize(new ECGenParameterSpec(VauVersion.V1.getCurve()));
    val keyPair = keyPairGenerator.generateKeyPair();
    vauCertificate = generateX509Certificate(keyPair.getPrivate(), keyPair.getPublic());
  }

  private VauClient createMockClient(
      String fdBaseUrl,
      ClientType clientType,
      X509Certificate vauCertificate,
      String xApiKey,
      String userAgent) {
    try (val unirestMock = mockStatic(Unirest.class, Answers.RETURNS_DEEP_STUBS)) {
      unitRestMock = mock(UnirestInstance.class, Answers.RETURNS_DEEP_STUBS);
      when(Unirest.spawnInstance()).thenReturn(unitRestMock);
      return new VauClient(fdBaseUrl, clientType, vauCertificate, xApiKey, userAgent);
    }
  }

  private void prepareEndpointVauCertificate(byte[] responseData) {
    when(unitRestMock.get("https://erp/VAUCertificate").asBytes().getBody())
        .thenReturn(responseData);
  }

  private void prepareResponseVau(byte[] resBody, String... resHeader) {
    prepareResponseVau(resBody, 200, resHeader);
  }

  @SuppressWarnings("unchecked")
  private void prepareResponseVau(byte[] resBody, int returnCode, String... resHeader) {
    val httpResponseMock = mock(HttpResponse.class);
    when(unitRestMock.post("https://erp/VAU/0").body(any(byte[].class)).asBytes())
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
    val vau =
        this.createMockClient(
            "https://erp", ClientType.FDV, vauCertificate, "testApiKey", "testAgent");

    prepareEndpointVauCertificate(vauCertificate.getEncoded());
    prepareResponseVau(
        "Nobody calls me chicken".getBytes(StandardCharsets.UTF_8),
        "Userpseudonym",
        "0",
        "X-Request-Id",
        "testRequestId-123456");

    vau.initialize();
    val innerRequest = mock(HttpBRequest.class);
    val response = vau.send(innerRequest, "testToken", "/Task");

    assertEquals(200, response.statusCode());
    assertEquals("Nobody calls me chicken", response.bodyAsString());
    assertEquals("testRequestId-123456", response.headerValue("X-Request-Id"));
  }

  @SneakyThrows
  @Test
  void shouldHandleUnencryptedResponse500() {
    val vau =
        this.createMockClient(
            "https://erp", ClientType.PS, vauCertificate, "testApiKey", "testAgent");

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
    vau.initialize();
    val innerRequest = mock(HttpBRequest.class);
    assertThrows(VauException.class, () -> vau.send(innerRequest, "testToken", "/Task"));
  }

  @SneakyThrows
  @Test
  void shouldNotFailIfUserpseudonymMissing() {
    try {
      val vau = this.createMockClient("https://erp", ClientType.PS, vauCertificate, "", "");

      prepareEndpointVauCertificate(vauCertificate.getEncoded());
      prepareResponseVau("Nobody calls me chicken".getBytes(StandardCharsets.UTF_8));

      vau.initialize();
      val innerRequest = mock(HttpBRequest.class);
      vau.send(innerRequest, "", null);
    } catch (VauException e) {
      fail();
    }
  }

  @SneakyThrows
  @Test
  void shouldNotFailIfUseragentMissing() {
    try {
      val vau = this.createMockClient("https://erp", ClientType.PS, vauCertificate, null, null);

      prepareEndpointVauCertificate(vauCertificate.getEncoded());
      prepareResponseVau("Nobody calls me chicken".getBytes(StandardCharsets.UTF_8));

      vau.initialize();
      val innerRequest = mock(HttpBRequest.class);
      vau.send(innerRequest, "", null);
    } catch (VauException e) {
      fail();
    }
  }

  @SneakyThrows
  @Test
  void shouldFailIfVauCertificateMissing() {
    assertThrows(
        NullPointerException.class,
        () -> this.createMockClient("https://erp", ClientType.PS, null, null, null));
  }

  @Test
  void shouldThrowOnMissingTlsContext() {
    val vauClient =
        new VauClient("https://erp", ClientType.PS, vauCertificate, "testApiKey", "testAgent");

    try (val mockSslContext = mockStatic(SSLContext.class)) {
      mockSslContext
          .when(() -> SSLContext.getInstance(anyString()))
          .thenThrow(new NoSuchAlgorithmException());
      assertThrows(NoSuchAlgorithmException.class, vauClient::initialize);
    }
  }
}
