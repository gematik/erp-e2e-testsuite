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

package de.gematik.test.erezept.actions;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.bbriccs.crypto.BC;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.actors.ActorStage;
import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.rest.MediaType;
import de.gematik.test.erezept.config.ConfigurationReader;
import de.gematik.test.erezept.config.dto.actor.PatientConfiguration;
import de.gematik.test.erezept.config.dto.actor.PsActorConfiguration;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Resource;
import org.mockito.stubbing.Answer;

/** This utility class initializes the actor stage with mocked actors */
public class MockActorsUtils extends ErpFhirParsingTest {
  private final String encodedJwt =
      "eyJhbGciOiJCUDI1NlIxIiwidHlwIjoiYXQrSldUIiwia2lkIjoicHVrX2lkcF9zaWcifQ.eyJzdWIiOiJJWERkLTNyUVpLS0ZYVWR4R0dqNFBERG9WNk0wUThaai1xdzF2cjF1XzU4IiwicHJvZmVzc2lvbk9JRCI6IjEuMi4yNzYuMC43Ni40LjQ5Iiwib3JnYW5pemF0aW9uTmFtZSI6ImdlbWF0aWsgTXVzdGVya2Fzc2UxR0tWTk9ULVZBTElEIiwiaWROdW1tZXIiOiJYMTEwNTAyNDE0IiwiYW1yIjpbIm1mYSIsInNjIiwicGluIl0sImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6NTUwMTEvYXV0aC9yZWFsbXMvaWRwLy53ZWxsLWtub3duL29wZW5pZC1jb25maWd1cmF0aW9uIiwiZ2l2ZW5fbmFtZSI6IlJvYmluIEdyYWYiLCJjbGllbnRfaWQiOiJlcnAtdGVzdHN1aXRlLWZkIiwiYWNyIjoiZ2VtYXRpay1laGVhbHRoLWxvYS1oaWdoIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdDozMDAwLyIsImF6cCI6ImVycC10ZXN0c3VpdGUtZmQiLCJzY29wZSI6Im9wZW5pZCBlLXJlemVwdCIsImF1dGhfdGltZSI6MTY0MzgwNDczMywiZXhwIjoxNjQzODA1MDMzLCJmYW1pbHlfbmFtZSI6IlbDs3Jtd2lua2VsIiwiaWF0IjoxNjQzODA0NjEzLCJqdGkiOiI2Yjg3NmU0MWNmMGViNGJkIn0.MV5cDnL3JBZ4b6xr9SqiYDmZ7qtZFEWBd1vCrHzVniZeDhkyuSYc7xhf577h2S21CzNgrMp0M6JALNW9Qjnw_g";
  public ErpClient erpClientMock;
  public ActorStage actorStage;

  /**
   * please don't forget to initialize your Actors with concrete Objects for example:
   * actorStage.getPatientNamed("Leonie Hütter");
   */
  public MockActorsUtils() {
    StopwatchProvider.init();
    this.actorStage = new ActorStage();
    initDefaultContextMock();
  }

  public <E extends Resource> ErpResponse<E> createErpResponse(
      @Nullable Resource resource, Class<E> expectType) {
    return createErpResponse(
        resource, expectType, 200, encodedJwt, Map.of(), createEmptyValidationResult());
  }

  public <E extends Resource> ErpResponse<E> createErpResponse(
      @Nullable Resource resource, Class<E> expectType, int statusCode) {
    return createErpResponse(
        resource, expectType, statusCode, encodedJwt, Map.of(), createEmptyValidationResult());
  }

  public <E extends Resource> ErpResponse<E> createErpResponse(
      @Nullable Resource resource,
      Class<E> expectType,
      int statusCode,
      String jwtToken,
      Map<String, String> headderMap,
      ValidationResult validationResult) {
    return ErpResponse.forPayload(resource, expectType)
        .withStatusCode(statusCode)
        .usedJwt(jwtToken)
        .withHeaders(headderMap)
        .andValidationResult(validationResult);
  }

  private void initDefaultContextMock() {
    BC.init();
    val cfg = ConfigurationReader.forPrimSysConfiguration().create();
    try (val erpClientFactoryMockedStatic = mockStatic(ErpClientFactory.class)) {
      erpClientMock = mock(ErpClient.class);

      erpClientFactoryMockedStatic
          .when(() -> ErpClientFactory.createErpClient(any(), any(PsActorConfiguration.class)))
          .thenReturn(erpClientMock);
      erpClientFactoryMockedStatic
          .when(() -> ErpClientFactory.createErpClient(any(), any(PatientConfiguration.class)))
          .thenReturn(erpClientMock);
      when(erpClientMock.getFhir()).thenReturn(parser);
      when(erpClientMock.encode(any(), any()))
          .thenAnswer(
              (Answer<String>)
                  invocationOnMock -> {
                    val args = invocationOnMock.getArguments();
                    return parser.encode((IBaseResource) args[0], (EncodingType) args[1]);
                  });

      when(erpClientMock.getAcceptMime()).thenReturn(MediaType.ACCEPT_FHIR_XML);
      when(erpClientMock.getSendMime()).thenReturn(MediaType.FHIR_XML);
      when(erpClientMock.getClientType()).thenReturn(ClientType.PS);

      cfg.getActors()
          .getPharmacies()
          .subList(0, 3)
          .forEach(pharm -> this.actorStage.getPharmacyNamed(pharm.getName()));
      cfg.getActors()
          .getDoctors()
          .subList(0, 3)
          .forEach(doc -> this.actorStage.getDoctorNamed(doc.getName()));
      cfg.getActors()
          .getPatients()
          .subList(0, 5)
          .forEach(patient -> this.actorStage.getPatientNamed(patient.getName()));
    }
  }
}
