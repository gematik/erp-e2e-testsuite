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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.actions.eu;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.actions.MockActorsUtils;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.eu.EuConsentDeleteCommand;
import de.gematik.test.erezept.client.usecases.eu.EuConsentGetCommand;
import de.gematik.test.erezept.client.usecases.eu.EuConsentPostCommand;
import de.gematik.test.erezept.fhir.builder.eu.EuConsentBuilder;
import de.gematik.test.erezept.fhir.r4.eu.EuConsent;
import de.gematik.test.erezept.fhir.r4.eu.EuConsentBundle;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EuGrantConsentTest {

  private MockActorsUtils mockUtil;
  private ErpClient erpClientMock;
  private PatientActor patient;

  @BeforeEach
  void setup() {
    StopwatchProvider.init();
    mockUtil = new MockActorsUtils();
    val actorStage = mockUtil.actorStage;
    this.erpClientMock = mockUtil.erpClientMock;
    this.patient = actorStage.getPatientNamed("Leonie Hütter");
  }

  @Test
  void shouldGrantConsentWithDefaultConsent() {
    val mockResponse =
        mockUtil.createErpResponse(
            EuConsentBuilder.forKvnr(KVNR.random()).build(), EuConsent.class, 201);

    when(erpClientMock.request(any(EuConsentPostCommand.class))).thenReturn(mockResponse);

    val action = EuGrantConsent.withKvnrAndDecideIsUnset(KVNR.random(), false).withDefaultConsent();

    assertDoesNotThrow(() -> patient.performs(action));
  }

  @Test
  void shouldGrantConsentWithExplicitConsent() {
    val consent = EuConsentBuilder.forKvnr(KVNR.random()).build();

    val mockResponse = mockUtil.createErpResponse(consent, EuConsent.class, 201);
    when(erpClientMock.request(any(EuConsentPostCommand.class))).thenReturn(mockResponse);

    val action = EuGrantConsent.withKvnrAndDecideIsUnset(KVNR.random(), false).withConsent(consent);

    assertDoesNotThrow(() -> patient.performs(action));
  }

  @Test
  void shouldGrantConsentForOneSelf() {
    val mockResponse =
        mockUtil.createErpResponse(
            EuConsentBuilder.forKvnr(KVNR.random()).build(), EuConsent.class, 201);
    when(erpClientMock.request(any(EuConsentPostCommand.class))).thenReturn(mockResponse);

    val action = EuGrantConsent.forOneSelf().withDefaultConsent();

    assertDoesNotThrow(() -> patient.performs(action));
  }

  @Test
  void shouldWorkForOneSelf() {
    val mockEuConsentBundle = new EuConsentBundle();
    mockEuConsentBundle.addEntry().setResource(EuConsentBuilder.forKvnr(KVNR.random()).build());
    val mockEuGetConsentResponse =
        mockUtil.createErpResponse(mockEuConsentBundle, EuConsentBundle.class, 200);
    val mockResponse = mockUtil.createErpResponse(null, EmptyResource.class, 204);
    when(erpClientMock.request(any(EuConsentDeleteCommand.class))).thenReturn(mockResponse);
    when(erpClientMock.request(any(EuConsentGetCommand.class)))
        .thenReturn(mockEuGetConsentResponse);

    val action = EuRejectConsent.forOneSelf().build();
    assertDoesNotThrow(() -> patient.performs(action));
  }

  @Test
  void shouldUseProvidedKvnrIfSet() {
    val kvnr = KVNR.random();
    val consent = EuConsentBuilder.forKvnr(kvnr).build();
    val mockResponse = mockUtil.createErpResponse(consent, EuConsent.class, 201);
    when(erpClientMock.request(any(EuConsentPostCommand.class))).thenReturn(mockResponse);

    val action = EuGrantConsent.withKvnrAndDecideIsUnset(kvnr, false).withConsent(consent);

    assertDoesNotThrow(() -> patient.performs(action));
  }

  @Test
  void ShouldSetKvnrAndReturnBuilder() {
    KVNR kvnr = KVNR.random();
    EuGrantConsent.EuGrantConsentBuilder builder = new EuGrantConsent.EuGrantConsentBuilder();

    EuGrantConsent.EuGrantConsentBuilder result = builder.withKvnr(kvnr);

    assertSame(builder, result, "Builder should return itself for chaining");
  }
}
