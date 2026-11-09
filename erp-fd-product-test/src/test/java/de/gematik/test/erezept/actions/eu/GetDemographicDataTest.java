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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.client.testutils.ErpResponseUtil;
import de.gematik.test.erezept.client.usecases.eu.EuGetPrescriptionPostCommand;
import de.gematik.test.erezept.fhir.r4.eu.EuGetPrescriptionInput;
import de.gematik.test.erezept.fhir.r4.eu.EuPrescriptionBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.utils.ActorMockUtil;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GetDemographicDataTest extends ErpFhirBuildingTest {

  @BeforeEach
  void setup() {
    StopwatchProvider.init();
  }

  @Test
  void shouldBuildCommandWithExpectedInput() {
    val patientMock = ActorMockUtil.createRandomPatient();
    val patientActor = patientMock.getLeft();

    val pharmacyMock = ActorMockUtil.createRandomEuPharmacy("LI");
    val pharmacyActor = pharmacyMock.getLeft();
    val pharmacyClient = pharmacyMock.getRight();

    val responseBundle = new EuPrescriptionBundle();
    val mockResponse =
        ErpResponseUtil.createErpResponse(responseBundle, EuPrescriptionBundle.class, 200);

    when(pharmacyClient.request(any(EuGetPrescriptionPostCommand.class))).thenReturn(mockResponse);

    val action = GetDemographicData.forPatient(patientActor).withRandomAccessCode();

    pharmacyActor.performs(action);

    ArgumentCaptor<EuGetPrescriptionPostCommand> cmdCaptor =
        ArgumentCaptor.forClass(EuGetPrescriptionPostCommand.class);

    verify(pharmacyClient, times(1)).request(cmdCaptor.capture());
  }

  @Test
  void shouldPerformDemographicsRequestSuccessfully() {
    val pharmacyMock = ActorMockUtil.createRandomEuPharmacy("LI");
    val pharmacyActor = pharmacyMock.getLeft();
    val pharmacyClient = pharmacyMock.getRight();

    val kvnr = KVNR.random();
    val accessCode = EuAccessCode.random();

    val responseBundle = new EuPrescriptionBundle();
    val mockResponse =
        ErpResponseUtil.createErpResponse(responseBundle, EuPrescriptionBundle.class, 200);
    when(pharmacyClient.request(any(EuGetPrescriptionPostCommand.class))).thenReturn(mockResponse);

    val action = GetDemographicData.forKvnr(kvnr).withAccessCode(accessCode);

    assertDoesNotThrow(() -> pharmacyActor.performs(action));
    verify(pharmacyClient, times(1)).request(any(EuGetPrescriptionPostCommand.class));
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
