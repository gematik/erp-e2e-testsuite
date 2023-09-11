/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.actions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.actors.*;
import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.*;
import java.util.*;
import lombok.*;
import org.junit.jupiter.api.*;

class TaskCreateTest {

  @Test
  void shouldPerformCorrectCommand() {
    val useErpClient = mock(UseTheErpClient.class);
    val doctor = new DoctorActor("MocDoc");
    doctor.can(useErpClient);

    val patient = mock(PatientActor.class);
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient.getName()));

    val mockResponse =
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), ErxTask.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(useErpClient.request(any(TaskCreateCommand.class))).thenReturn(mockResponse);

    assertDoesNotThrow(
        () ->
            doctor.performs(
                TaskCreate.forPatient(patient)
                    .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)));
  }
}
