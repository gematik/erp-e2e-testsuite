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

package de.gematik.test.erezept.primsys.model;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import jakarta.ws.rs.WebApplicationException;
import java.util.Map;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.Test;

class FetchReceiptUseCaseTest extends TestWithActorContext {

  @Test
  void shouldThrowExceptionWhenNoReceiptPresent() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(1);
    val mockClient = pharmacy.getClient();

    val prescriptionId = PrescriptionId.random();
    val taskId = prescriptionId.getValue();
    val secret = Secret.random().getValue();

    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    val task = mock(ErxTask.class);

    when(prescriptionBundle.getTask()).thenReturn(task);
    when(task.getStatus()).thenReturn(Task.TaskStatus.INPROGRESS);
    val getResponse =
        ErpResponse.forPayload(prescriptionBundle, ErxPrescriptionBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(mockClient.request(any(TaskGetByIdCommand.class))).thenReturn(getResponse);

    val useCase = new FetchReceiptUseCase(pharmacy);
    assertThrows(WebApplicationException.class, () -> useCase.fetchReceipt(taskId, secret));
  }

  @Test
  void shouldExtractReceipt() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(1);
    val mockClient = pharmacy.getClient();

    val prescriptionId = PrescriptionId.random();
    val taskId = prescriptionId.getValue();
    val secret = Secret.random().getValue();

    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    val task = mock(ErxTask.class);
    val receipt = new ErxReceipt();

    when(prescriptionBundle.getTask()).thenReturn(task);
    when(task.getStatus()).thenReturn(Task.TaskStatus.COMPLETED);
    when(prescriptionBundle.getReceipt()).thenReturn(Optional.of(receipt));
    val getResponse =
        ErpResponse.forPayload(prescriptionBundle, ErxPrescriptionBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(mockClient.request(any(TaskGetByIdCommand.class))).thenReturn(getResponse);

    val useCase = new FetchReceiptUseCase(pharmacy);
    try (val response = useCase.fetchReceipt(taskId, secret)) {
      assertTrue(response.hasEntity());
      assertEquals(200, response.getStatus());
    }
  }
}
