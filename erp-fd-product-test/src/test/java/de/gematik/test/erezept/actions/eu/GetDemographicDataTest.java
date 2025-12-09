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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.abilities.ProvidePharmacyBaseData;
import de.gematik.test.erezept.actions.MockActorsUtils;
import de.gematik.test.erezept.actors.ActorStage;
import de.gematik.test.erezept.actors.EuPharmacyActor;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.eu.EuGetPrescriptionPostCommand;
import de.gematik.test.erezept.config.dto.actor.EuPharmacyConfiguration;
import de.gematik.test.erezept.fhir.r4.eu.EuGetPrescriptionInput;
import de.gematik.test.erezept.fhir.r4.eu.EuPrescriptionBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GetDemographicDataTest extends ErpFhirBuildingTest {

  private MockActorsUtils mockUtil;
  private ErpClient erpClientMock;
  private EuPharmacyActor pharmacy;

  @BeforeEach
  void setup() {
    StopwatchProvider.init();
    this.mockUtil = new MockActorsUtils();
    ActorStage actorStage = mockUtil.actorStage;
    this.erpClientMock = mockUtil.erpClientMock;
    this.pharmacy = actorStage.getEuPharmacyNamed("Hannes Vogt");

    pharmacy.can(UseTheErpClient.with(erpClientMock));

    EuPharmacyConfiguration cfg = new EuPharmacyConfiguration();
    cfg.setCountryCode("LI");
    pharmacy.can(ProvidePharmacyBaseData.fromConfiguration(cfg));
  }

  @Test
  void shouldBuildCommandWithExpectedInput() {
    val patient = mockUtil.actorStage.getPatientNamed("Fridolin Straßer");

    val responseBundle = new EuPrescriptionBundle();
    val mockResponse = mockUtil.createErpResponse(responseBundle, EuPrescriptionBundle.class, 200);

    when(erpClientMock.request(any(EuGetPrescriptionPostCommand.class))).thenReturn(mockResponse);

    val action = GetDemographicData.forPatient(patient).withRandomAccessCode();

    pharmacy.performs(action);

    ArgumentCaptor<EuGetPrescriptionPostCommand> cmdCaptor =
        ArgumentCaptor.forClass(EuGetPrescriptionPostCommand.class);

    verify(erpClientMock, times(1)).request(cmdCaptor.capture());
  }

  @Test
  void shouldPerformDemographicsRequestSuccessfully() {
    val kvnr = KVNR.random();
    val accessCode = EuAccessCode.random();

    val responseBundle = new EuPrescriptionBundle();
    val mockResponse = mockUtil.createErpResponse(responseBundle, EuPrescriptionBundle.class, 200);
    when(erpClientMock.request(any(EuGetPrescriptionPostCommand.class))).thenReturn(mockResponse);

    val action = GetDemographicData.forKvnr(kvnr).withAccessCode(accessCode);

    assertDoesNotThrow(() -> pharmacy.performs(action));
    verify(erpClientMock, times(1)).request(any(EuGetPrescriptionPostCommand.class));
  }

  @Test
  void factoryMethodShouldCreateInstance() {
    val kvnr = KVNR.random();
    val accessCode = EuAccessCode.random();

    val action =
        GetDemographicData.forKvnr(kvnr)
            .with(EuGetPrescriptionInput::getRequestData) // dummy manipulator
            .withAccessCode(accessCode);

    assertNotNull(action, "Factory should return a non-null action");
  }
}
