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
import static org.mockito.Mockito.times;
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
import de.gematik.test.erezept.fhir.valuesets.eu.EuPartNaming;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import java.util.function.Consumer;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class GetEuPrescriptionsTest extends ErpFhirBuildingTest {

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

    when(erpClientMock.request(ArgumentMatchers.any(EuGetPrescriptionPostCommand.class)))
        .thenReturn(mockResponse);

    val action = GetEuPrescriptions.forPatient(patient).withRandomAccessCode();

    pharmacy.performs(action);

    ArgumentCaptor<EuGetPrescriptionPostCommand> cmdCaptor =
        ArgumentCaptor.forClass(EuGetPrescriptionPostCommand.class);

    Mockito.verify(erpClientMock, times(1)).request(cmdCaptor.capture());
  }

  @Test
  void shouldBuildCommandWithExpectedInputAndManipulator() {
    val kvnr = KVNR.random();
    val responseBundle = new EuPrescriptionBundle();
    val mockResponse = mockUtil.createErpResponse(responseBundle, EuPrescriptionBundle.class, 200);
    when(erpClientMock.request(ArgumentMatchers.any(EuGetPrescriptionPostCommand.class)))
        .thenReturn(mockResponse);
    val action =
        GetEuPrescriptions.forKvnr(kvnr)
            .with(removePart(EuPartNaming.ACCESS_CODE))
            .withRandomAccessCode();
    pharmacy.performs(action);
    ArgumentCaptor<EuGetPrescriptionPostCommand> cmdCaptor =
        ArgumentCaptor.forClass(EuGetPrescriptionPostCommand.class);
    Mockito.verify(erpClientMock, times(1)).request(cmdCaptor.capture());
  }

  private static Consumer<EuGetPrescriptionInput> removePart(EuPartNaming part) {
    return input ->
        input.getRequestData().getPart().stream()
            .filter(p -> p.getName().equals(part.getCode()))
            .forEach(p -> p.setValue(null));
  }

  @Test
  void shouldGetEuPrescriptionsSuccessfully() {
    val kvnr = KVNR.random();

    val responseBundle = new EuPrescriptionBundle();
    val mockResponse = mockUtil.createErpResponse(responseBundle, EuPrescriptionBundle.class, 200);
    when(erpClientMock.request(ArgumentMatchers.any(EuGetPrescriptionPostCommand.class)))
        .thenReturn(mockResponse);

    val action = GetEuPrescriptions.forKvnr(kvnr).withRandomAccessCode();

    assertDoesNotThrow(() -> pharmacy.performs(action));
    Mockito.verify(erpClientMock, times(1))
        .request(ArgumentMatchers.any(EuGetPrescriptionPostCommand.class));
  }

  @Test
  void factoryMethodShouldCreateInstance() {
    val kvnr = KVNR.random();

    val action = GetEuPrescriptions.forKvnr(kvnr).withRandomAccessCode();

    assertNotNull(action, "Factory should return a non-null action");
  }
}
