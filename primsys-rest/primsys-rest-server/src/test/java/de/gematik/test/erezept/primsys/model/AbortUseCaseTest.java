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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.fhir.testutil.ErxFhirTestResourceUtil;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.data.error.ErrorDto;
import de.gematik.test.erezept.primsys.rest.response.*;
import jakarta.ws.rs.*;
import java.util.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.*;

class AbortUseCaseTest extends TestWithActorContext {

  @Test
  void constructorShouldNotBeCallable() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(AbortUseCase.class));
  }

  @Test
  void shouldAbortPrescription() {
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
    when(mockClient.request(any())).thenReturn(mockResponse);
    try (var response =
        AbortUseCase.abortPrescription(pharmacy, "taskId", "accessCode", "verySecret")) {
      assertEquals(200, response.getStatus());
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
        .request(any(TaskAbortCommand.class));

    try (val response =
        AbortUseCase.abortPrescription(pharmacy, "taskId", "accessCode", "verySecret")) {
      fail("AbortUseCase did not throw the expected Exception and answered with ");
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
        .request(any(TaskAbortCommand.class));

    try (val response =
        AbortUseCase.abortPrescription(pharmacy, "taskId", "accessCode", "verySecret")) {
      fail("AbortUseCase did not throw the expected Exception");
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(500, wae.getResponse().getStatus());
      assertEquals(ErrorDto.class, wae.getResponse().getEntity().getClass());
    }
  }

  @Test
  void shouldThrowGetStatusCodeIsBigger299() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(1);
    val mockClient = pharmacy.getClient();

    val mockResponse =
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), Resource.class)
            .withStatusCode(400)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    doThrow(ErrorResponseBuilder.createFachdienstErrorException(mockResponse))
        .when(mockClient)
        .request(any(TaskAbortCommand.class));

    try (val response =
        AbortUseCase.abortPrescription(pharmacy, "taskId", "accessCode", "verySecret")) {
      fail("AbortUseCase did not throw the expected Exception and answered with ");
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(400, wae.getResponse().getStatus());
      assertEquals(ErrorDto.class, wae.getResponse().getEntity().getClass());
    }
  }
}
