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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.ConsentDeleteCommand;
import de.gematik.test.erezept.client.usecases.ConsentGetCommand;
import de.gematik.test.erezept.client.usecases.ConsentPostCommand;
import de.gematik.test.erezept.fhir.builder.erp.ErxConsentBuilder;
import de.gematik.test.erezept.fhir.resources.erp.ErxConsent;
import de.gematik.test.erezept.fhir.resources.erp.ErxConsentBundle;
import de.gematik.test.erezept.fhir.values.KVNR;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GrantConsentTest {
  private ErpClient erpClientMock;
  private MockActorsUtils mockUtil;

  private PharmacyActor pharmacyActor;
  private PatientActor patientActor;

  @BeforeEach
  void init() {
    StopwatchProvider.init();
    this.mockUtil = new MockActorsUtils();
    val actorStage = mockUtil.actorStage;
    this.erpClientMock = mockUtil.erpClientMock;
    pharmacyActor = actorStage.getPharmacyNamed("Stadtapotheke");
    patientActor = actorStage.getPatientNamed("Leonie Hütter");
  }

  @Test
  void shouldBuildConsentDepositCorrect() {
    val mockResponse = mockUtil.createErpResponse(null, Resource.class, 400);
    when(erpClientMock.request(any())).thenReturn(mockResponse);
    assertDoesNotThrow(() -> patientActor.performs(GrantConsent.forOneSelf().withDefaultConsent()));
  }

  @Test
  void shouldBuildConsentDepositCorrectWithCustomConsent() {
    val mockResponse = mockUtil.createErpResponse(null, Resource.class, 400);
    when(erpClientMock.request(any())).thenReturn(mockResponse);
    val consent = ErxConsentBuilder.forKvnr(KVNR.random()).build();
    assertDoesNotThrow(
        () ->
            patientActor.performs(
                GrantConsent.withKvnrAndEnsureIsUnset(KVNR.random())
                    .ensureConsentIsUnset(
                        false) // ensure is set to false caused by mock and Null pointer
                    .withConsent(consent)));
  }

  @Test
  void shouldBuildConsentDepositCorrectWithDeposeConsentConstructor() {
    val mockResponse = mockUtil.createErpResponse(null, Resource.class, 400);
    when(erpClientMock.request(any())).thenReturn(mockResponse);
    val consent = ErxConsentBuilder.forKvnr(KVNR.random()).build();
    assertDoesNotThrow(
        () ->
            patientActor.performs(
                GrantConsent.withKvnrAndDecideIsUnset(KVNR.random(), false).withConsent(consent)));
  }

  @Test
  void shouldBuildConsentDepositCorrectWithEnsureAndConsent() {
    val mockConsentResponse = mockUtil.createErpResponse(null, ErxConsent.class, 200);
    val mockResourceResponse = mockUtil.createErpResponse(null, Resource.class, 200);
    val mockConsBundle = new ErxConsentBundle();
    mockConsBundle.addEntry().setResource(ErxConsentBuilder.forKvnr(KVNR.random()).build());
    val mockErxBundleResponse =
        mockUtil.createErpResponse(mockConsBundle, ErxConsentBundle.class, 200);
    when(erpClientMock.request(any(ConsentPostCommand.class))).thenReturn(mockConsentResponse);
    when(erpClientMock.request(any(ConsentDeleteCommand.class))).thenReturn(mockResourceResponse);
    when(erpClientMock.request(any(ConsentGetCommand.class))).thenReturn(mockErxBundleResponse);
    val mockErpResponseRessource = mockUtil.createErpResponse(null, Resource.class, 201);
    when(erpClientMock.request(any(ConsentDeleteCommand.class)))
        .thenReturn(mockErpResponseRessource);
    val consent = ErxConsentBuilder.forKvnr(KVNR.random()).build();
    assertDoesNotThrow(
        () ->
            patientActor.performs(
                GrantConsent.withKvnrAndEnsureIsUnset(KVNR.random())
                    .ensureConsentIsUnset(true)
                    .withConsent(consent)));
  }
}
