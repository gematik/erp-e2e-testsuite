/*
 * Copyright 2024 gematik GmbH
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.rest.param.QueryParameter;
import de.gematik.test.erezept.client.usecases.ConsentDeleteCommand;
import de.gematik.test.erezept.client.usecases.ConsentGetCommand;
import de.gematik.test.erezept.client.usecases.search.ConsentDeleteBuilder;
import de.gematik.test.erezept.fhir.builder.erp.ErxConsentBuilder;
import de.gematik.test.erezept.fhir.resources.erp.ErxConsentBundle;
import de.gematik.test.erezept.fhir.values.KVNR;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RejectConsentTest {
  private ErpClient erpClientMock;
  private MockActorsUtils mockUtil;

  private PatientActor patientActor;

  @BeforeEach
  void init() {
    StopwatchProvider.init();
    this.mockUtil = new MockActorsUtils();
    val actorStage = mockUtil.actorStage;
    this.erpClientMock = mockUtil.erpClientMock;
    patientActor = actorStage.getPatientNamed("Leonie Hütter");
  }

  @Test
  void shouldBuildConsentRejectCorrect() {
    val mockResponse = mockUtil.createErpResponse(null, Resource.class, 400);
    when(erpClientMock.request(any())).thenReturn(mockResponse);
    assertDoesNotThrow(
        () -> patientActor.performs(RejectConsent.forOneSelf().withoutEnsureIsPresent()));
  }

  @Test
  void shouldBuildConsentRejectCorrectWithEnsure() {
    val mockResponse = mockUtil.createErpResponse(null, Resource.class, 400);
    val mockConsBundle = new ErxConsentBundle();
    mockConsBundle.addEntry().setResource(ErxConsentBuilder.forKvnr(KVNR.random()).build());
    val mockErxBundleResponse =
        mockUtil.createErpResponse(mockConsBundle, ErxConsentBundle.class, 200);
    when(erpClientMock.request(any(ConsentDeleteCommand.class))).thenReturn(mockResponse);
    when(erpClientMock.request(any(ConsentGetCommand.class))).thenReturn(mockErxBundleResponse);
    assertDoesNotThrow(() -> patientActor.performs(RejectConsent.forOneSelf().buildValid()));
  }

  @Test
  void shouldBuildConsentRejectCorrectWithoutQueryParam() {
    val mockResponse = mockUtil.createErpResponse(null, Resource.class, 400);
    val mockConsBundle = new ErxConsentBundle();
    mockConsBundle.addEntry().setResource(ErxConsentBuilder.forKvnr(KVNR.random()).build());
    val mockErxBundleResponse =
        mockUtil.createErpResponse(mockConsBundle, ErxConsentBundle.class, 200);
    when(erpClientMock.request(any(ConsentDeleteCommand.class))).thenReturn(mockResponse);
    when(erpClientMock.request(any(ConsentGetCommand.class))).thenReturn(mockErxBundleResponse);
    assertDoesNotThrow(
        () ->
            patientActor.performs(
                RejectConsent.forOneSelf()
                    .withCustomCommand(
                        ConsentDeleteBuilder.withCustomQuerySet()
                            .addQuery(new QueryParameter("null", "null"))
                            .build(),
                        false)));
  }

  @Test
  void shouldBuildConsentRejectCorrectWith() {
    val mockResponse = mockUtil.createErpResponse(null, Resource.class, 400);
    val mockConsBundle = new ErxConsentBundle();
    mockConsBundle.addEntry().setResource(ErxConsentBuilder.forKvnr(KVNR.random()).build());
    val mockErxBundleResponse =
        mockUtil.createErpResponse(mockConsBundle, ErxConsentBundle.class, 200);
    when(erpClientMock.request(any(ConsentDeleteCommand.class))).thenReturn(mockResponse);
    when(erpClientMock.request(any(ConsentGetCommand.class))).thenReturn(mockErxBundleResponse);
    assertDoesNotThrow(
        () ->
            patientActor.performs(
                RejectConsent.forOneSelf(ConsentDeleteBuilder.withCustomCategory("TEST"), true)));
  }
}
