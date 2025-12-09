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

package de.gematik.test.erezept.actions;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.*;
import static de.gematik.test.fuzzing.kbv.CreateManipulatorFactory.getCreateManipulators;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

class TaskCreateTest extends ErpFhirBuildingTest {

  @Test
  void shouldPerformCorrectCommand() {
    val useErpClient = mock(UseTheErpClient.class);
    val doctor = new DoctorActor("MocDoc");
    doctor.can(useErpClient);

    val patient = mock(PatientActor.class);
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient.getName()));

    val mockResponse =
        ErpResponse.forPayload(createOperationOutcome(), ErxTask.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(TaskCreateCommand.class))).thenReturn(mockResponse);

    assertDoesNotThrow(
        () ->
            doctor.performs(
                TaskCreate.forPatient(patient)
                    .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)));
  }

  @Test
  void shouldPerformCorrectCommandWithManipulator() {
    val useErpClient = mock(UseTheErpClient.class);
    val doctor = new DoctorActor("MocDoc");
    doctor.can(useErpClient);

    val patient = mock(PatientActor.class);
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient.getName()));

    val mockResponse =
        ErpResponse.forPayload(createOperationOutcome(), ErxTask.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(TaskCreateCommand.class))).thenReturn(mockResponse);

    assertDoesNotThrow(
        () ->
            doctor.performs(
                TaskCreate.forPatient(patient)
                    .manipulator(getCreateManipulators().get(0).getParameter())
                    .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)));
  }

  @Test
  void shouldPerformCorrectCommandWithoutManipulator() {
    val useErpClient = mock(UseTheErpClient.class);
    val doctor = new DoctorActor("MocDoc");
    doctor.can(useErpClient);

    val patient = mock(PatientActor.class);
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient.getName()));

    val mockResponse =
        ErpResponse.forPayload(createOperationOutcome(), ErxTask.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(TaskCreateCommand.class))).thenReturn(mockResponse);

    assertDoesNotThrow(
        () -> doctor.performs(TaskCreate.withFlowType(PrescriptionFlowType.FLOW_TYPE_200)));
  }
}
