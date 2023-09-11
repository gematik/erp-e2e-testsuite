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

package de.gematik.test.erezept.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.gematik.idp.client.*;
import de.gematik.idp.token.*;
import de.gematik.test.erezept.client.exceptions.*;
import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.client.vau.*;
import de.gematik.test.erezept.fhir.parser.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.smartcard.*;
import java.security.cert.*;
import lombok.*;
import org.junit.jupiter.api.*;

class ErpClientTest {

  private VauClient mockVau;
  private IdpClient mockIdp;

  @BeforeEach
  void setup() {
    mockIdp = mock(IdpClient.class);
    mockVau = mock(VauClient.class);
  }

  @AfterEach
  void resetMocks() {
    reset(mockVau, mockIdp);
  }

  @Test
  void shouldCatchNpeFromIdpClientOnAuthenticateWithSmartcard() {
    val mockSmartcard = mock(Smartcard.class);
    when(mockIdp.initialize()).thenThrow(NullPointerException.class);

    val erpClient = ErpClient.builder().idpClient(mockIdp).vauClient(mockVau).build();
    assertThrows(IdpClientRuntimeException.class, () -> erpClient.authenticateWith(mockSmartcard));
  }

  @Test
  void shouldCatchNpeFromIdpClientOnAuthenticateWithChallenge() {
    val mockCert = mock(X509Certificate.class);

    when(mockIdp.initialize()).thenThrow(NullPointerException.class);

    val erpClient = ErpClient.builder().idpClient(mockIdp).vauClient(mockVau).build();
    assertThrows(
        IdpClientRuntimeException.class, () -> erpClient.authenticateWith(mockCert, (p) -> p));
  }

  @Test
  void shouldCatchNpeFromIdpClientOnLogin() {
    val mockSmartcard = mock(Smartcard.class);

    when(mockIdp.login(any())).thenThrow(NullPointerException.class);

    val erpClient = ErpClient.builder().idpClient(mockIdp).vauClient(mockVau).build();
    erpClient.authenticateWith(mockSmartcard);
    val cmd = new TaskGetCommand();
    assertThrows(IdpClientRuntimeException.class, () -> erpClient.request(cmd));
  }

  @Test
  void shouldNotThrow() {
    val mockSmartcard = mock(Smartcard.class);
    val mockToken = mock(IdpTokenResult.class);
    val mockJsonWebToken = mock(JsonWebToken.class);
    val mockSmardCardCertificate = mock(SmartcardCertificate.class);

    when(mockSmartcard.getAutCertificate()).thenReturn(mockSmardCardCertificate);
    when(mockIdp.login(any())).thenReturn(mockToken);
    when(mockToken.getAccessToken()).thenReturn(mockJsonWebToken);
    when(mockJsonWebToken.getRawString()).thenReturn("idptoken123");

    val mockResponse = mock(Response.class);
    when(mockVau.send(anyString(), any(), any())).thenReturn(mockResponse);
    when(mockResponse.getBody()).thenReturn("mock body");

    val mockErpResponse = mock(ErpResponse.class);
    val mockResponseFactory = mock(ErpResponseFactory.class);
    when(mockResponseFactory.createFrom(anyInt(), any(), any(), any(), any()))
        .thenReturn(mockErpResponse);

    val erpClient =
        ErpClient.builder()
            .idpClient(mockIdp)
            .vauClient(mockVau)
            .responseFactory(mockResponseFactory)
            .acceptMime(MediaType.FHIR_XML)
            .sendMime(MediaType.FHIR_JSON)
            .build();
    erpClient.authenticateWith(mockSmartcard);

    val cmd = new TaskGetCommand();
    assertDoesNotThrow(() -> erpClient.request(cmd));
    assertDoesNotThrow(() -> erpClient.request(cmd));
    verify(mockToken, times(1)).getExpiresIn();
    verify(mockIdp, times(2)).login(any());
  }

  @Test
  void shouldRefreshExpiredToken() {
    val mockSmartcard = mock(Smartcard.class);
    val mockToken = mock(IdpTokenResult.class);
    val mockJsonWebToken = mock(JsonWebToken.class);
    val mockSmardCardCertificate = mock(SmartcardCertificate.class);

    when(mockSmartcard.getAutCertificate()).thenReturn(mockSmardCardCertificate);
    when(mockIdp.login(any())).thenReturn(mockToken);
    when(mockToken.getAccessToken()).thenReturn(mockJsonWebToken);

    when(mockJsonWebToken.getRawString()).thenReturn("idptoken123");

    val mockResponse = mock(Response.class);
    when(mockVau.send(anyString(), any(), any())).thenReturn(mockResponse);
    when(mockResponse.getBody()).thenReturn("mock body");

    val mockErpResponse = mock(ErpResponse.class);
    val mockResponseFactory = mock(ErpResponseFactory.class);
    when(mockResponseFactory.createFrom(anyInt(), any(), any(), any(), any()))
        .thenReturn(mockErpResponse);

    val erpClient =
        ErpClient.builder()
            .idpClient(mockIdp)
            .vauClient(mockVau)
            .responseFactory(mockResponseFactory)
            .acceptMime(MediaType.FHIR_XML)
            .sendMime(MediaType.FHIR_JSON)
            .build();
    erpClient.authenticateWith(mockSmartcard);

    // second call will have a token which is still valid
    when(mockToken.getExpiresIn()).thenReturn(100);
    val cmd = new TaskGetCommand();
    assertDoesNotThrow(() -> erpClient.request(cmd));
    assertDoesNotThrow(() -> erpClient.request(cmd));
    /* should call login only on first attempt, while the second one will reuse token because of expires in 100 seconds */
    verify(mockIdp, times(1)).login(any());
  }

  @Test
  void shouldForwardEncodeResource() {
    val mockFhir = mock(FhirParser.class);
    val encoded = "encoded resource";
    when(mockFhir.encode(any(), any(), anyBoolean())).thenReturn(encoded);

    val erpClient = ErpClient.builder().fhir(mockFhir).build();
    assertEquals(encoded, erpClient.encode(new KbvErpBundle(), EncodingType.XML));
  }

  @Test
  void shouldFailOnInvalidRequestBody() {
    val mockFhir = mock(FhirParser.class);
    val vr = FhirTestResourceUtil.createFailingValidationResult();
    val mockSmartcard = mock(Smartcard.class);
    val mockToken = mock(IdpTokenResult.class);
    val mockJsonWebToken = mock(JsonWebToken.class);
    val mockSmardCardCertificate = mock(SmartcardCertificate.class);

    when(mockSmartcard.getAutCertificate()).thenReturn(mockSmardCardCertificate);
    when(mockIdp.login(any())).thenReturn(mockToken);
    when(mockToken.getAccessToken()).thenReturn(mockJsonWebToken);
    when(mockToken.getExpiresIn()).thenReturn(100);
    when(mockJsonWebToken.getRawString()).thenReturn("idptoken123");
    when(mockFhir.validate(anyString())).thenReturn(vr);
    when(mockFhir.encode(any(), any())).thenReturn("mock resource content");

    val erpClient =
        ErpClient.builder()
            .idpClient(mockIdp)
            .vauClient(mockVau)
            .validateRequest(true)
            .fhir(mockFhir)
            .acceptMime(MediaType.FHIR_XML)
            .sendMime(MediaType.FHIR_JSON)
            .build();
    erpClient.authenticateWith(mockSmartcard);

    val cmd =
        new TaskActivateCommand(TaskId.from("123"), AccessCode.random(), "hello world".getBytes());
    assertThrows(FhirValidationException.class, () -> erpClient.request(cmd));
  }
}
