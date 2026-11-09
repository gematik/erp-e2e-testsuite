/*
 * Copyright (Change Date see Readme), gematik GmbH
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
import de.gematik.test.erezept.client.testutils.ErpResponseUtil;
import de.gematik.test.erezept.client.usecases.eu.EuConsentDeleteCommand;
import de.gematik.test.erezept.client.usecases.eu.EuConsentGetCommand;
import de.gematik.test.erezept.client.usecases.eu.EuConsentPostCommand;
import de.gematik.test.erezept.fhir.builder.eu.EuConsentBuilder;
import de.gematik.test.erezept.fhir.r4.eu.EuConsent;
import de.gematik.test.erezept.fhir.r4.eu.EuConsentBundle;
import de.gematik.test.utils.ActorMockUtil;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EuGrantConsentTest {

  @BeforeEach
  void setup() {
    StopwatchProvider.init();
  }

  @Test
  void shouldGrantConsentWithDefaultConsent() {
    val patientMock = ActorMockUtil.createRandomPatient();
    val patientActor = patientMock.getLeft();
    val patientClient = patientMock.getRight();

    val mockResponse =
        ErpResponseUtil.createErpResponse(
            EuConsentBuilder.forKvnr(KVNR.random()).build(), EuConsent.class, 201);

    when(patientClient.request(any(EuConsentPostCommand.class))).thenReturn(mockResponse);

    val action = EuGrantConsent.withKvnrAndDecideIsUnset(KVNR.random(), false).withDefaultConsent();

    assertDoesNotThrow(() -> patientActor.performs(action));
  }

  @Test
  void shouldGrantConsentWithExplicitConsent() {
    val patientMock = ActorMockUtil.createRandomPatient();
    val patientActor = patientMock.getLeft();
    val patientClient = patientMock.getRight();

    val consent = EuConsentBuilder.forKvnr(KVNR.random()).build();

    val mockResponse = ErpResponseUtil.createErpResponse(consent, EuConsent.class, 201);
    when(patientClient.request(any(EuConsentPostCommand.class))).thenReturn(mockResponse);

    val action = EuGrantConsent.withKvnrAndDecideIsUnset(KVNR.random(), false).withConsent(consent);

    assertDoesNotThrow(() -> patientActor.performs(action));
  }

  @Test
  void shouldGrantConsentForOneSelf() {
    val patientMock = ActorMockUtil.createRandomPatient();
    val patientActor = patientMock.getLeft();
    val patientClient = patientMock.getRight();

    val mockResponse =
        ErpResponseUtil.createErpResponse(
            EuConsentBuilder.forKvnr(KVNR.random()).build(), EuConsent.class, 201);
    when(patientClient.request(any(EuConsentPostCommand.class))).thenReturn(mockResponse);

    val action = EuGrantConsent.forOneSelf().withDefaultConsent();

    assertDoesNotThrow(() -> patientActor.performs(action));
  }

  @Test
  void shouldWorkForOneSelf() {
    val patientMock = ActorMockUtil.createRandomPatient();
    val patientActor = patientMock.getLeft();
    val patientClient = patientMock.getRight();

    val mockEuConsentBundle = new EuConsentBundle();
    mockEuConsentBundle.addEntry().setResource(EuConsentBuilder.forKvnr(KVNR.random()).build());
    val mockEuGetConsentResponse =
        ErpResponseUtil.createErpResponse(mockEuConsentBundle, EuConsentBundle.class, 200);
    val mockResponse = ErpResponseUtil.createErpResponse(null, EmptyResource.class, 204);
    when(patientClient.request(any(EuConsentDeleteCommand.class))).thenReturn(mockResponse);
    when(patientClient.request(any(EuConsentGetCommand.class)))
        .thenReturn(mockEuGetConsentResponse);

    val action = EuRejectConsent.forOneSelf().build();
    assertDoesNotThrow(() -> patientActor.performs(action));
  }

  @Test
  void shouldUseProvidedKvnrIfSet() {
    val patientMock = ActorMockUtil.createRandomPatient();
    val patientActor = patientMock.getLeft();
    val patientClient = patientMock.getRight();

    val kvnr = KVNR.random();
    val consent = EuConsentBuilder.forKvnr(kvnr).build();
    val mockResponse = ErpResponseUtil.createErpResponse(consent, EuConsent.class, 201);
    when(patientClient.request(any(EuConsentPostCommand.class))).thenReturn(mockResponse);

    val action = EuGrantConsent.withKvnrAndDecideIsUnset(kvnr, false).withConsent(consent);

    assertDoesNotThrow(() -> patientActor.performs(action));
  }

  @Test
  void ShouldSetKvnrAndReturnBuilder() {
    val kvnr = KVNR.random();
    EuGrantConsent.EuGrantConsentBuilder builder = new EuGrantConsent.EuGrantConsentBuilder();

    EuGrantConsent.EuGrantConsentBuilder result = builder.withKvnr(kvnr);

    assertSame(builder, result, "Builder should return itself for chaining");
  }
}
