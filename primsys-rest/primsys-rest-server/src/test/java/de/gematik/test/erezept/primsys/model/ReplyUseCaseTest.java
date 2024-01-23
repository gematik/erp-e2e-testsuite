/*
 * Copyright 2023 gematik GmbH
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

import static java.text.MessageFormat.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.client.usecases.CommunicationPostCommand;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.data.error.ErrorDto;
import de.gematik.test.erezept.primsys.rest.response.*;
import de.gematik.test.erezept.testutil.PrivateConstructorsUtil;
import jakarta.ws.rs.*;
import java.util.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class ReplyUseCaseTest extends TestWithActorContext {

  @Test
  void constructorShouldNotBeCallable() {
    assertTrue(PrivateConstructorsUtil.throwsInvocationTargetException(ReplyUseCase.class));
  }

  @ParameterizedTest
  @CsvSource({
    "onPremise",
    "onpremise",
    "ONPREMISE",
    "delivery",
    "DELIVERY",
    "ShipMENT",
    "shipment",
    "null"
  })
  void shouldReplyPrescription(String supplyType) {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(1);
    val mockClient = pharmacy.getClient();

    val resource =
        FhirTestResourceUtil.createErxAuditEvent(
            "testString", TelematikID.from("123"), "testName", AuditEvent.AuditEventAction.R);
    val mockResponse =
        ErpResponse.forPayload(resource, ErxCommunication.class)
            .withStatusCode(204)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(mockClient.request(any(CommunicationPostCommand.class))).thenReturn(mockResponse);
    try (val response =
        ReplyUseCase.replyPrescription(
            pharmacy, "taskId", "KVNR", supplyType, "Hello World - Message")) {
      assertEquals(204, response.getStatus());
    }
  }

  @Test
  void shouldThrowIsOperationOutcome() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(1);
    val mockClient = pharmacy.getClient();

    val mockResponse =
        ErpResponse.forPayload(
                FhirTestResourceUtil.createOperationOutcome(), ErxCommunication.class)
            .withStatusCode(500)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    doThrow(ErrorResponseBuilder.createFachdienstErrorException(mockResponse))
        .when(mockClient)
        .request(any(CommunicationPostCommand.class));

    try (val response =
        ReplyUseCase.replyPrescription(
            pharmacy, "taskId", "KVNR", "onPremise", "Hello World - Message")) {
      fail(
          format(
              "ReplyUseCase did not throw the expected Exception and answered with {0}",
              response.getStatus()));
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
        ErpResponse.forPayload(
                FhirTestResourceUtil.createOperationOutcome(), ErxCommunication.class)
            .withStatusCode(500)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    doThrow(ErrorResponseBuilder.createFachdienstErrorException(mockResponse))
        .when(mockClient)
        .request(any(CommunicationPostCommand.class));
    try (val response =
        ReplyUseCase.replyPrescription(
            pharmacy, "taskId", "KVNR", "onPremise", "Hello World - Message")) {
      fail(
          format(
              "ReplyUseCase did not throw the expected Exception and answered with {0}",
              response.getStatus()));
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
        ErpResponse.forPayload(
                FhirTestResourceUtil.createOperationOutcome(), ErxCommunication.class)
            .withStatusCode(400)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    doThrow(ErrorResponseBuilder.createFachdienstErrorException(mockResponse))
        .when(mockClient)
        .request(any(CommunicationPostCommand.class));

    try (val response =
        ReplyUseCase.replyPrescription(
            pharmacy, "taskId", "KVNR", "onPremise", "Hello World - Message")) {
      fail(
          format(
              "ReplyUseCase did not throw the expected Exception and answered with {0}",
              response.getStatus()));
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(400, wae.getResponse().getStatus());
      assertEquals(ErrorDto.class, wae.getResponse().getEntity().getClass());
    }
  }
}
