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
import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.CommunicationPostCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.testutil.ErxFhirTestResourceUtil;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.data.error.ErrorDto;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import jakarta.ws.rs.WebApplicationException;
import java.util.Map;
import lombok.val;
import org.hl7.fhir.r4.model.AuditEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ReplyUseCaseTest extends TestWithActorContext {

  @Test
  void constructorShouldNotBeCallable() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(ReplyUseCase.class));
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
        ErxFhirTestResourceUtil.createErxAuditEvent(
            "testString", TelematikID.from("123"), "testName", AuditEvent.AuditEventAction.R);
    val mockResponse =
        ErpResponse.forPayload(resource, ErxCommunication.class)
            .withStatusCode(204)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(mockClient.request(any(CommunicationPostCommand.class))).thenReturn(mockResponse);
    try (val response =
        ReplyUseCase.replyPrescription(
            pharmacy, "taskId", "KVNR", supplyType, "Hello World - Message")) {
      assertEquals(204, response.getStatus());
    }
  }

  @ParameterizedTest
  @CsvSource({
    "'onPremise', 'TestSender'",
    "onpremise', 'TestSender'",
    "ONPREMISE', ''",
    "delivery', 'TestSender123456789'",
    "DELIVERY', 'TestSenderMitSönd3rZ@ichen'",
    "ShipMENT', 'TestSender'",
    "shipment', 'TestSender'",
    "null', 'TestSender'"
  })
  void shouldReplyPrescriptionWithSender(String supplyType, String sender) {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(1);
    val mockClient = pharmacy.getClient();

    val resource =
        ErxFhirTestResourceUtil.createErxAuditEvent(
            "testString", TelematikID.from("123"), "testName", AuditEvent.AuditEventAction.R);
    val mockResponse =
        ErpResponse.forPayload(resource, ErxCommunication.class)
            .withStatusCode(204)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(mockClient.request(any(CommunicationPostCommand.class))).thenReturn(mockResponse);
    try (val response =
        ReplyUseCase.replyPrescriptionWithSender(
            pharmacy, "taskId", "KVNR", supplyType, "Hello World - Message", sender)) {
      assertEquals(204, response.getStatus());
    }
  }

  @Test
  void shouldThrowIsOperationOutcome() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(1);
    val mockClient = pharmacy.getClient();

    val mockResponse =
        ErpResponse.forPayload(createOperationOutcome(), ErxCommunication.class)
            .withStatusCode(500)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
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
        ErpResponse.forPayload(createOperationOutcome(), ErxCommunication.class)
            .withStatusCode(500)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
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
        ErpResponse.forPayload(createOperationOutcome(), ErxCommunication.class)
            .withStatusCode(400)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
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
