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
import de.gematik.test.erezept.client.usecases.CloseTaskCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.data.AcceptedPrescriptionDto;
import de.gematik.test.erezept.primsys.data.valuesets.PatientInsuranceTypeDto;
import de.gematik.test.erezept.primsys.mapping.HealthAppRequestDataMapper;
import jakarta.ws.rs.WebApplicationException;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

@SetSystemProperty(key = "erp.fhir.profile", value = "1.4.0")
class CloseDiGATest extends TestWithActorContext {

  @Test
  void shouldCloseDiGAPrescription() {
    val ctx = ActorContext.getInstance();
    val ktr = ctx.getHealthInsurances().get(0);
    val mockClient = ktr.getClient();

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
            .forKvnr("X110407071", PatientInsuranceTypeDto.GKV)
            .andDiGA(HealthAppRequestDataMapper.random().build().getDto());

    ActorContext.getInstance().addAcceptedPrescription(acceptDto);
    val usecase = new CloseDiGA(ktr);
    try (val response = usecase.closePrescription(taskId, secret)) {
      assertEquals(204, response.getStatus());
    }
  }

  @Test
  void shouldDeclineDiGAPrescription() {
    val ctx = ActorContext.getInstance();
    val ktr = ctx.getHealthInsurances().get(0);
    val mockClient = ktr.getClient();

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
            .forKvnr("X110407071", PatientInsuranceTypeDto.GKV)
            .andDiGA(HealthAppRequestDataMapper.random().build().getDto());

    ActorContext.getInstance().addAcceptedPrescription(acceptDto);
    val usecase = new CloseDiGA(ktr);
    try (val response = usecase.declinePrescription(taskId, secret)) {
      assertEquals(204, response.getStatus());
    }
  }

  @Test
  void shouldThrowWebExceptionOnCloseDiGAPrescriptionBecauseOfMissingAcceptData() {
    val ctx = ActorContext.getInstance();
    val ktr = ctx.getHealthInsurances().get(0);

    val usecase = new CloseDiGA(ktr);
    try (val response = usecase.closePrescription("taskId", "accessCode")) {
      fail("CloseUseCase did not throw the expected Exception");
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(404, wae.getResponse().getStatus());
    }
  }
}
