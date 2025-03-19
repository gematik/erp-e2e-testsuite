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
 */

package de.gematik.test.erezept.actions;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createOperationOutcome;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.actors.*;
import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.r4.erp.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import java.util.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.*;

class TaskAbortTest {
  @Test
  void shouldPerformCorrectCommandAsPatient() {
    val useErpClient = mock(UseTheErpClient.class);
    val patient = new PatientActor("Sina");
    patient.can(useErpClient);

    val erxTask = new ErxTask();
    erxTask.setId("123");
    val mockResponse =
        ErpResponse.forPayload(createOperationOutcome(), Resource.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(TaskAbortCommand.class))).thenReturn(mockResponse);

    assertDoesNotThrow(() -> patient.performs(TaskAbort.asPatient(erxTask)));
  }

  @Test
  void shouldPerformCorrectCommandAsLeistungserbringer() {
    val useErpClient = mock(UseTheErpClient.class);
    val doctor = new DoctorActor("Gündüla Gunther");
    doctor.can(useErpClient);

    val erxTask = new ErxTask();
    erxTask.setId("123");

    Identifier accessCodeIdentifier =
        new Identifier()
            .setSystem("https://gematik.de/fhir/NamingSystem/AccessCode")
            .setValue("456");
    erxTask.addIdentifier(accessCodeIdentifier);

    val mockResponse =
        ErpResponse.forPayload(createOperationOutcome(), Resource.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(TaskAbortCommand.class))).thenReturn(mockResponse);

    assertDoesNotThrow(() -> doctor.performs(TaskAbort.asLeistungserbringer(erxTask)));
  }

  @Test
  void shouldPerformCorrectCommandAsPharmacy() {
    val useErpClient = mock(UseTheErpClient.class);
    val pharmacy = new PharmacyActor("Stadtapotheke");
    pharmacy.can(useErpClient);

    val erxTask = new ErxTask();
    erxTask.setId("456");

    Identifier secretIdentifier = new Identifier();
    secretIdentifier.setSystem(ErpWorkflowNamingSystem.SECRET.getCanonicalUrl());
    secretIdentifier.setValue("some-secret");

    erxTask.addIdentifier(secretIdentifier);

    val mockResponse =
        ErpResponse.forPayload(createOperationOutcome(), Resource.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());

    when(useErpClient.request(any(TaskAbortCommand.class))).thenReturn(mockResponse);

    val acceptBundle = new ErxAcceptBundle();
    acceptBundle.addEntry(new Bundle.BundleEntryComponent().setResource(erxTask));

    assertDoesNotThrow(() -> pharmacy.performs(TaskAbort.asPharmacy(acceptBundle)));
  }
}
