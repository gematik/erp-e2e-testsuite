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

package de.gematik.test.erezept.screenplay.abilities;

import static java.text.MessageFormat.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.client.*;
import de.gematik.test.erezept.client.cfg.*;
import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.fhir.builder.erp.*;
import de.gematik.test.erezept.fhir.parser.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.konnektor.*;
import java.security.cert.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import net.serenitybdd.core.*;
import net.serenitybdd.core.reports.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

@Slf4j
class UseTheErpClientTest {

  private static final String TEST_TOKEN =
      "eyJhbGciOiJCUDI1NlIxIiwidHlwIjoiYXQrSldUIiwia2lkIjoicHVrX2lkcF9zaWcifQ.eyJzdWIiOiJJWERkLTNyUVpLS0ZYVWR4R0dqNFBERG9WNk0wUThaai1xdzF2cjF1XzU4IiwicHJvZmVzc2lvbk9JRCI6IjEuMi4yNzYuMC43Ni40LjQ5Iiwib3JnYW5pemF0aW9uTmFtZSI6ImdlbWF0aWsgTXVzdGVya2Fzc2UxR0tWTk9ULVZBTElEIiwiaWROdW1tZXIiOiJYMTEwNTAyNDE0IiwiYW1yIjpbIm1mYSIsInNjIiwicGluIl0sImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6NTUwMTEvYXV0aC9yZWFsbXMvaWRwLy53ZWxsLWtub3duL29wZW5pZC1jb25maWd1cmF0aW9uIiwiZ2l2ZW5fbmFtZSI6IlJvYmluIEdyYWYiLCJjbGllbnRfaWQiOiJlcnAtdGVzdHN1aXRlLWZkIiwiYWNyIjoiZ2VtYXRpay1laGVhbHRoLWxvYS1oaWdoIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdDozMDAwLyIsImF6cCI6ImVycC10ZXN0c3VpdGUtZmQiLCJzY29wZSI6Im9wZW5pZCBlLXJlemVwdCIsImF1dGhfdGltZSI6MTY0MzgwNDczMywiZXhwIjoxNjQzODA1MDMzLCJmYW1pbHlfbmFtZSI6IlbDs3Jtd2lua2VsIiwiaWF0IjoxNjQzODA0NjEzLCJqdGkiOiI2Yjg3NmU0MWNmMGViNGJkIn0.MV5cDnL3JBZ4b6xr9SqiYDmZ7qtZFEWBd1vCrHzVniZeDhkyuSYc7xhf577h2S21CzNgrMp0M6JALNW9Qjnw_g";

  private ErpClient mockErpClient;
  private FhirParser mockFhir;
  private UseTheErpClient ability;

  @BeforeEach
  void setup() {
    try (MockedStatic<ErpClientFactory> f = mockStatic(ErpClientFactory.class)) {
      mockErpClient = mock(ErpClient.class);
      mockFhir = mock(FhirParser.class);
      when(mockErpClient.getFhir()).thenReturn(mockFhir);
      when(mockErpClient.getAcceptMime()).thenReturn(MediaType.ACCEPT_FHIR_JSON);
      when(mockErpClient.getSendMime()).thenReturn(MediaType.FHIR_JSON);
      when(mockErpClient.getClientType()).thenReturn(ClientType.PS);
      ability = UseTheErpClient.with(mockErpClient);
      log.trace(format("Prepared Ability {0}", ability));
    }
  }

  @Test
  void shouldReportRequestResponse() {
    val chargeItem = ErxChargeItemBuilder.faker(PrescriptionId.random()).build();
    val cmd = new ChargeItemPostCommand(chargeItem, new Secret("123"));
    val expectedResponse =
        ErpResponse.forPayload(chargeItem, ErxChargeItem.class)
            .withStatusCode(201)
            .usedJwt(TEST_TOKEN)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    when(mockErpClient.request(cmd)).thenReturn(expectedResponse);
    when(mockFhir.encode(any(), any(), eq(true))).thenReturn("mock fhir resource");

    try (MockedStatic<Serenity> serenityMockedStatic = mockStatic(Serenity.class)) {
      val mockWithTitle = mock(WithTitle.class);
      val mockAndContent = mock(AndContent.class);
      serenityMockedStatic.when(Serenity::recordReportData).thenReturn(mockWithTitle);
      when(mockWithTitle.withTitle(anyString())).thenReturn(mockAndContent);

      val response = ability.request(cmd);
      val requestTitle = format("{0} {1}", cmd.getMethod(), cmd.getRequestLocator());
      verify(mockWithTitle, times(1)).withTitle(requestTitle);
      verify(mockWithTitle, times(3)).withTitle(anyString());
      verify(mockAndContent, times(2)).andContents("mock fhir resource");
    }
  }

  @Test
  void shouldForwardDecodeToClient() {
    val chargeItem = ErxChargeItemBuilder.faker(PrescriptionId.random()).build();
    when(mockFhir.decode(eq(ErxChargeItem.class), any())).thenReturn(chargeItem);

    val ret = ability.decode(ErxChargeItem.class, "don't care");
    assertEquals(chargeItem, ret);
    verify(mockFhir, times(1)).decode(ErxChargeItem.class, "don't care");
  }

  @Test
  void shouldAuthenticateWithKonnektor() {
    val mockCertificate = mock(X509Certificate.class);
    val challengeResponse = "HelloWorld".getBytes();
    val mockKonnektorAbility = mock(UseTheKonnektor.class);
    when(mockKonnektorAbility.getSmcbAuthCertificate())
        .thenReturn(new KonnektorResponse<>(mockCertificate));
    when(mockKonnektorAbility.externalAuthenticate(any()))
        .thenReturn(new KonnektorResponse<>(challengeResponse));
    doNothing().when(mockErpClient).authenticateWith(any(), any());
    ability.authenticateWith(mockKonnektorAbility);

    verify(mockErpClient, times(1)).authenticateWith(eq(mockCertificate), any());
  }
}
