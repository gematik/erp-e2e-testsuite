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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

class TaskCreateTest {

  @Test
  void shouldPerformCorrectCommand() {
    val useErpClient = mock(UseTheErpClient.class);
    val doctor = new DoctorActor("MocDoc");
    doctor.can(useErpClient);

    val patient = mock(PatientActor.class);
    patient.can(ProvidePatientBaseData.forGkvPatient("X123456789", patient.getName()));

    val mockResponse =
        new ErpResponse(404, Map.of(), FhirTestResourceUtil.createOperationOutcome());
    when(useErpClient.request(any(TaskCreateCommand.class))).thenReturn(mockResponse);

    assertDoesNotThrow(
        () ->
            doctor.performs(
                TaskCreate.forPatient(patient)
                    .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)));
  }
}
