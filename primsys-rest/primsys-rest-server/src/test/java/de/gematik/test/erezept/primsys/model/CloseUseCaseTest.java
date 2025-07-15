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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.CloseTaskCommand;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.data.AcceptedPrescriptionDto;
import de.gematik.test.erezept.primsys.data.valuesets.PatientInsuranceTypeDto;
import de.gematik.test.erezept.primsys.mapping.KbvPznMedicationDataMapper;
import de.gematik.test.erezept.primsys.mapping.PznDispensedMedicationDataMapper;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

class CloseUseCaseTest extends TestWithActorContext {

  static Stream<Arguments> shouldClosePrescription() {
    return Stream.of(
        arguments("1.4.0", true),
        arguments("1.4.0", false),
        arguments("1.5.0", true),
        arguments("1.5.0", false));
  }

  @ParameterizedTest
  @MethodSource
  @ClearSystemProperty(key = "erp.fhir.profile")
  void shouldClosePrescription(String fhirProfile, boolean shouldHaveLastMedicationDispenseDate) {
    System.setProperty("erp.fhir.profile", fhirProfile);

    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(0);
    val mockClient = pharmacy.getClient();

    val receiptMock = mock(ErxReceipt.class);
    when(receiptMock.getId()).thenReturn("123456789");

    val prescriptionBundleMock = mock(ErxPrescriptionBundle.class);

    val mockTask = mock(ErxTask.class);
    when(prescriptionBundleMock.getTask()).thenReturn(mockTask);
    when(mockTask.hasLastMedicationDispenseDate()).thenReturn(shouldHaveLastMedicationDispenseDate);

    val mockResponse =
        ErpResponse.forPayload(receiptMock, ErxReceipt.class)
            .withStatusCode(204)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());

    val mockTaskGetResponse =
        ErpResponse.forPayload(prescriptionBundleMock, ErxPrescriptionBundle.class)
            .withStatusCode(204)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());

    when(mockClient.request(any(TaskGetByIdCommand.class))).thenReturn(mockTaskGetResponse);

    when(mockClient.request(any(CloseTaskCommand.class))).thenReturn(mockResponse);

    val taskId = PrescriptionId.random().getValue();
    val accessCode = AccessCode.random().getValue();
    val secret = "verrySecrets3cr3t";
    val acceptDto =
        AcceptedPrescriptionDto.withPrescriptionId(taskId)
            .withAccessCode(accessCode)
            .withSecret(secret)
            .forKvnr("X110407071", PatientInsuranceTypeDto.GKV)
            .andMedication(KbvPznMedicationDataMapper.randomDto());
    ActorContext.getInstance().addAcceptedPrescription(acceptDto);
    val usecase = new CloseUseCase(pharmacy);
    try (val response = usecase.closePrescription(taskId, secret)) {
      assertEquals(204, response.getStatus());
    }
  }

  @Test
  void shouldThrowWebExceptionOnClosePrescriptionBecauseOfMissingAcceptData() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(0);
    val usecase = new CloseUseCase(pharmacy);
    val mockClient = pharmacy.getClient();
    val prescriptionBundleMock = mock(ErxPrescriptionBundle.class);

    val mockTask = mock(ErxTask.class);
    when(prescriptionBundleMock.getTask()).thenReturn(mockTask);
    when(mockTask.hasLastMedicationDispenseDate()).thenReturn(false);

    val mockTaskGetResponse =
        ErpResponse.forPayload(prescriptionBundleMock, ErxPrescriptionBundle.class)
            .withStatusCode(204)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());

    when(mockClient.request(any(TaskGetByIdCommand.class))).thenReturn(mockTaskGetResponse);

    try (val response = usecase.closePrescription("taskId", "accessCode")) {
      fail("CloseUseCase did not throw the expected Exception");
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
    when(mockClient.request(any(CloseTaskCommand.class))).thenReturn(mockResponse);

    val taskId = PrescriptionId.random().getValue();
    val accessCode = AccessCode.random().getValue();
    val secret = "verrySecrets3cr3t";
    val acceptDto =
        AcceptedPrescriptionDto.withPrescriptionId(taskId)
            .withAccessCode(accessCode)
            .withSecret(secret)
            .forKvnr("X110407071", PatientInsuranceTypeDto.PKV)
            .andMedication(KbvPznMedicationDataMapper.randomDto());
    ActorContext.getInstance().addAcceptedPrescription(acceptDto);
    val dispenseMedications =
        List.of(
            PznDispensedMedicationDataMapper.randomDto(),
            PznDispensedMedicationDataMapper.randomDto());

    val usecase = new CloseUseCase(pharmacy);
    try (val response = usecase.closePrescription(taskId, secret, dispenseMedications)) {
      assertEquals(204, response.getStatus());
    }
  }
}
