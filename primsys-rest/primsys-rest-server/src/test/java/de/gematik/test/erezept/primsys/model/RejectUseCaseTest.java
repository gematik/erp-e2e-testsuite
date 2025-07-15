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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskRejectCommand;
import de.gematik.test.erezept.fhir.testutil.ErxFhirTestResourceUtil;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.data.error.ErrorDto;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import jakarta.ws.rs.WebApplicationException;
import java.util.Map;
import lombok.val;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;

class RejectUseCaseTest extends TestWithActorContext {

  @Test
  void shouldRejectPrescription() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(1);
    val mockClient = pharmacy.getClient();

    val resource =
        ErxFhirTestResourceUtil.createErxAuditEvent(
            "testString", TelematikID.from("123"), "testName", AuditEvent.AuditEventAction.R);
    val mockResponse =
        ErpResponse.forPayload(resource, Resource.class)
            .withStatusCode(204)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(mockClient.request(any(TaskRejectCommand.class))).thenReturn(mockResponse);
    val useCase = new RejectUseCase(pharmacy);
    try (val response = useCase.rejectPrescription("taskId", "accessCode", "verySecret")) {
      assertEquals(204, response.getStatus());
    }
  }

  @Test
  void shouldThrowIsOperationOutcome() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(1);
    val mockClient = pharmacy.getClient();

    val mockResponse =
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), Resource.class)
            .withStatusCode(500)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    doThrow(ErrorResponseBuilder.createFachdienstErrorException(mockResponse))
        .when(mockClient)
        .request(any(TaskRejectCommand.class));

    val useCase = new RejectUseCase(pharmacy);
    try (val response = useCase.rejectPrescription("taskId", "accessCode", "verySecret")) {
      fail("RejectUseCase did not throw the expected Exception");
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(500, wae.getResponse().getStatus());
      assertEquals(ErrorDto.class, wae.getResponse().getEntity().getClass());
    }
  }

  @Test
  void shouldThrowWebApplicationWith500andNull() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(1);
    val mockClient = pharmacy.getClient();

    val mockResponse =
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), Resource.class)
            .withStatusCode(500)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    doThrow(ErrorResponseBuilder.createFachdienstErrorException(mockResponse))
        .when(mockClient)
        .request(any(TaskRejectCommand.class));

    val useCase = new RejectUseCase(pharmacy);
    try (val response = useCase.rejectPrescription("taskId", "accessCode", "verySecret")) {
      fail("RejectUseCase did not throw the expected Exception");
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(500, wae.getResponse().getStatus());
      assertEquals(ErrorDto.class, wae.getResponse().getEntity().getClass());
    }
  }

  @Test
  void shouldThrowGetStatusCodeIsBigger299() {
    val ctx = ActorContext.getInstance();
    val ktr = ctx.getHealthInsurances().get(0);
    val mockClient = ktr.getClient();

    val mockResponse =
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), Resource.class)
            .withStatusCode(400)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    doThrow(ErrorResponseBuilder.createFachdienstErrorException(mockResponse))
        .when(mockClient)
        .request(any(TaskRejectCommand.class));

    val useCase = new RejectUseCase(ktr);
    try (val response = useCase.rejectPrescription("taskId", "accessCode", "verySecret")) {
      fail("RejectUseCase did not throw the expected Exception");
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(400, wae.getResponse().getStatus());
      assertEquals(ErrorDto.class, wae.getResponse().getEntity().getClass());
    }
  }
}
