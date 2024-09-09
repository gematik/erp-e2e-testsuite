/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.primsys.model;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ClosePrescriptionCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.data.AcceptedPrescriptionDto;
import de.gematik.test.erezept.primsys.mapping.PznDispensedMedicationDataMapper;
import de.gematik.test.erezept.primsys.mapping.PznMedicationDataMapper;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

class CloseUseCaseTest extends TestWithActorContext {

  @Test
  void constructorShouldNotBeCallable() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(CloseUseCase.class));
  }

  @Test
  void closePrescriptionShouldWork() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(0);
    val mockClient = pharmacy.getClient();

    val receiptMock = mock(ErxReceipt.class);
    when(receiptMock.getId()).thenReturn("123456789");

    val mockResponse =
        ErpResponse.forPayload(receiptMock, ErxReceipt.class)
            .withStatusCode(204)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());

    when(mockClient.request(any(ClosePrescriptionCommand.class))).thenReturn(mockResponse);

    val taskId = PrescriptionId.random().getValue();
    val accessCode = AccessCode.random().getValue();
    val secret = "verrySecrets3cr3t";
    val acceptDto =
        AcceptedPrescriptionDto.withPrescriptionId(taskId)
            .withAccessCode(accessCode)
            .withSecret(secret)
            .forKvnr("X110407071")
            .andMedication(PznMedicationDataMapper.randomDto());
    ActorContext.getInstance().addAcceptedPrescription(acceptDto);
    try (val response = CloseUseCase.closePrescription(pharmacy, taskId, secret)) {
      assertEquals(204, response.getStatus());
    }
  }

  @Test
  void shouldThrowWebExceptionOnClosePrescriptionBecauseOfMissingAcceptData() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(0);

    try (val response = CloseUseCase.closePrescription(pharmacy, "taskId", "accessCode")) {
      fail("RejectUseCase did not throw the expected Exception");
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(404, wae.getResponse().getStatus());
    }
  }

  @Test
  void closePrescriptionWithDispenseDataShouldWork() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(0);
    val mockClient = pharmacy.getClient();

    val receiptMock = mock(ErxReceipt.class);
    when(receiptMock.getId()).thenReturn("123456789");

    val mockResponse =
        ErpResponse.forPayload(receiptMock, ErxReceipt.class)
            .withStatusCode(204)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(mockClient.request(any(ClosePrescriptionCommand.class))).thenReturn(mockResponse);

    val taskId = PrescriptionId.random().getValue();
    val accessCode = AccessCode.random().getValue();
    val secret = "verrySecrets3cr3t";
    val acceptDto =
        AcceptedPrescriptionDto.withPrescriptionId(taskId)
            .withAccessCode(accessCode)
            .withSecret(secret)
            .forKvnr("X110407071")
            .andMedication(PznMedicationDataMapper.randomDto());
    ActorContext.getInstance().addAcceptedPrescription(acceptDto);
    val dispenseMedications =
        List.of(
            PznDispensedMedicationDataMapper.randomDto(),
            PznDispensedMedicationDataMapper.randomDto());

    try (val response =
        CloseUseCase.closePrescription(pharmacy, taskId, secret, dispenseMedications)) {
      assertEquals(204, response.getStatus());
    }
  }
}
