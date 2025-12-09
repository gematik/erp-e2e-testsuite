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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import java.util.Map;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;

class TaskAbortTest {

  @Test
  void shouldPerformCorrectCommandAsPatient() {
    val useErpClient = mock(UseTheErpClient.class);
    val patient = new PatientActor("Sina");
    patient.can(useErpClient);

    val erxTask = new ErxTask();
    erxTask.setId("123");
    val mockResponse =
        ErpResponse.forPayload(createOperationOutcome(), EmptyResource.class)
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

    val accessCode = AccessCode.random();
    erxTask.addIdentifier(accessCode.asIdentifier());

    val mockResponse =
        ErpResponse.forPayload(createOperationOutcome(), EmptyResource.class)
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
    erxTask.addIdentifier(Secret.random().asIdentifier());
    erxTask.getMeta().addProfile(ErpWorkflowStructDef.TASK.getCanonicalUrl());

    val mockResponse =
        ErpResponse.forPayload(createOperationOutcome(), EmptyResource.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());

    when(useErpClient.request(any(TaskAbortCommand.class))).thenReturn(mockResponse);

    val acceptBundle = new ErxAcceptBundle();
    acceptBundle.addEntry(new Bundle.BundleEntryComponent().setResource(erxTask));

    assertDoesNotThrow(() -> pharmacy.performs(TaskAbort.asPharmacy(acceptBundle)));
  }
}
