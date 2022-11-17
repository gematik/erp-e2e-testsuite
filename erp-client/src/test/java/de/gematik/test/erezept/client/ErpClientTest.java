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

package de.gematik.test.erezept.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.gematik.idp.client.IdpClient;
import de.gematik.idp.client.IdpClientRuntimeException;
import de.gematik.idp.client.IdpTokenResult;
import de.gematik.idp.token.JsonWebToken;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.rest.ErpResponseFactory;
import de.gematik.test.erezept.client.rest.MediaType;
import de.gematik.test.erezept.client.usecases.TaskGetCommand;
import de.gematik.test.erezept.client.vau.Response;
import de.gematik.test.erezept.client.vau.VauClient;
import de.gematik.test.smartcard.Smartcard;
import java.security.cert.X509Certificate;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    when(mockIdp.login(any())).thenReturn(mockToken);
    when(mockToken.getAccessToken()).thenReturn(mockJsonWebToken);
    when(mockJsonWebToken.getRawString()).thenReturn("idptoken123");

    val mockResponse = mock(Response.class);
    when(mockVau.send(anyString(), any(), any())).thenReturn(mockResponse);
    when(mockResponse.getBody()).thenReturn("mock body");

    val mockErpResponse = mock(ErpResponse.class);
    val mockResponseFactory = mock(ErpResponseFactory.class);
    when(mockResponseFactory.createFrom(anyInt(), any(), any(), any())).thenReturn(mockErpResponse);

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
  }
}
