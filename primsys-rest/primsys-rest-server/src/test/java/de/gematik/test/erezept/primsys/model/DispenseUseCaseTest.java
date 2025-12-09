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

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.DispensePrescriptionAsBundleCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
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
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

class DispenseUseCaseTest extends TestWithActorContext {

  @ParameterizedTest
  @ValueSource(strings = {"1.4.0", "1.5.0"})
  @ClearSystemProperty(key = "erp.fhir.profile")
  void shouldDispensePrescription(String fhirProfile) {
    System.setProperty("erp.fhir.profile", fhirProfile);

    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(0);
    val mockClient = pharmacy.getClient();

    val bundleMock = mock(ErxMedicationDispenseBundle.class);
    when(bundleMock.getId()).thenReturn("123456789");

    val mockResponse =
        ErpResponse.forPayload(bundleMock, ErxMedicationDispenseBundle.class)
            .withStatusCode(204)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());

    when(mockClient.request(any(DispensePrescriptionAsBundleCommand.class)))
        .thenReturn(mockResponse);

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
    val usecase = new DispenseUseCase(pharmacy);
    try (val response = usecase.dispensePrescription(taskId, secret)) {
      assertEquals(204, response.getStatus());
    }
  }

  @Test
  void shouldThrowWebExceptionOnDispensePrescriptionBecauseOfMissingAcceptData() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(0);
    val usecase = new DispenseUseCase(pharmacy);

    try (val response = usecase.dispensePrescription("taskId", "accessCode")) {
      fail("DispenseUseCase did not throw the expected Exception");
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(404, wae.getResponse().getStatus());
    }
  }

  @Test
  void dispensePrescriptionWithDispenseDataShouldWork() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(0);
    val mockClient = pharmacy.getClient();

    val bundleMock = mock(ErxMedicationDispenseBundle.class);
    when(bundleMock.getId()).thenReturn("123456789");

    val mockResponse =
        ErpResponse.forPayload(bundleMock, ErxMedicationDispenseBundle.class)
            .withStatusCode(204)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(mockClient.request(any(DispensePrescriptionAsBundleCommand.class)))
        .thenReturn(mockResponse);

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
    val dispenseMedications =
        List.of(
            PznDispensedMedicationDataMapper.randomDto(),
            PznDispensedMedicationDataMapper.randomDto());

    val usecase = new DispenseUseCase(pharmacy);
    try (val response = usecase.dispensePrescription(taskId, secret, dispenseMedications)) {
      assertEquals(204, response.getStatus());
    }
  }
}
