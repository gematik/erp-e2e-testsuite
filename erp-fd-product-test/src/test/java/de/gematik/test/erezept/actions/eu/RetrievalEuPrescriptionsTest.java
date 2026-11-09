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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.testutils.ErpResponseUtil;
import de.gematik.test.erezept.client.usecases.eu.EuGetPrescriptionPostCommand;
import de.gematik.test.erezept.fhir.r4.eu.EuPrescriptionBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.utils.ActorMockUtil;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class RetrievalEuPrescriptionsTest extends ErpFhirBuildingTest {

  private List<PrescriptionId> prescriptionIds;

  @BeforeEach
  void setup() {
    StopwatchProvider.init();
    this.prescriptionIds = List.of(PrescriptionId.random(), PrescriptionId.random());
  }

  @Test
  void shouldBuildCommandWithExpectedInput() {
    val pharmacyMock = ActorMockUtil.createRandomPharmacy();
    val pharmacyActor = pharmacyMock.getLeft();
    val pharmacyClient = pharmacyMock.getRight();

    val patientActor = new PatientActor("Alice");
    patientActor.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), "Alice"));

    val responseBundle = new EuPrescriptionBundle();
    val mockResponse =
        ErpResponseUtil.createErpResponse(responseBundle, EuPrescriptionBundle.class, 200);

    when(pharmacyClient.request(ArgumentMatchers.any(EuGetPrescriptionPostCommand.class)))
        .thenReturn(mockResponse);

    val action =
        RetrievalEuPrescriptions.forPatient(patientActor)
            .withPrescriptionIds(prescriptionIds)
            .withRandomAccessCode();

    pharmacyActor.performs(action);

    ArgumentCaptor<EuGetPrescriptionPostCommand> cmdCaptor =
        ArgumentCaptor.forClass(EuGetPrescriptionPostCommand.class);

    Mockito.verify(pharmacyClient, times(1)).request(cmdCaptor.capture());
  }

  @Test
  void shouldRetrievalEuPrescriptionsSuccessfully() {
    val pharmacyMock = ActorMockUtil.createRandomPharmacy();
    val pharmacyActor = pharmacyMock.getLeft();
    val pharmacyClient = pharmacyMock.getRight();

    val kvnr = KVNR.random();
    val accessCode = EuAccessCode.random();

    val responseBundle = new EuPrescriptionBundle();
    val mockResponse =
        ErpResponseUtil.createErpResponse(responseBundle, EuPrescriptionBundle.class, 200);
    when(pharmacyClient.request(ArgumentMatchers.any(EuGetPrescriptionPostCommand.class)))
        .thenReturn(mockResponse);

    val action =
        RetrievalEuPrescriptions.forKvnr(kvnr)
            .withPrescriptionIds(prescriptionIds)
            .withAccessCode(accessCode);

    assertDoesNotThrow(() -> pharmacyActor.performs(action));
    Mockito.verify(pharmacyClient, times(1))
        .request(ArgumentMatchers.any(EuGetPrescriptionPostCommand.class));
  }

  @Test
  void factoryMethodShouldCreateInstance() {
    val kvnr = KVNR.random();
    val accessCode = EuAccessCode.random();

    val action =
        RetrievalEuPrescriptions.forKvnr(kvnr)
            .withPrescriptionIds(prescriptionIds)
            .withAccessCode(accessCode);

    assertNotNull(action, "Factory should return a non-null action");
  }
}
