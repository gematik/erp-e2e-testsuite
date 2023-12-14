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

package de.gematik.test.erezept.client.rest;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.client.exceptions.*;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import java.util.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.*;

class ErpResponseTest {

  @Test
  void getOptionalResourceType() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    assertTrue(response.getResourceOptional().isPresent());
  }

  @Test
  void getEmptyOptionalResourceType() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    assertTrue(response.getResourceOptional(OperationOutcome.class).isEmpty());
  }

  @Test
  void shouldThrowOnUnexpectedResource() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), ErxTask.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    assertThrows(UnexpectedResponseResourceError.class, response::getExpectedResource);
  }

  @Test
  void shouldReturnNullableResourceType() {
    val response =
        ErpResponse.forPayload(null, KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    assertNull(response.getResourceType());
  }

  @Test
  void shouldDetectOperationOutcome() {
    val operationOutcome = FhirTestResourceUtil.createOperationOutcome();
    val response =
        ErpResponse.forPayload(operationOutcome, OperationOutcome.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    assertTrue(response.isOperationOutcome());
    assertDoesNotThrow(response::getAsOperationOutcome);
  }

  @Test
  void shouldAlsoReturnGenericResource() {
    val operationOutcome = FhirTestResourceUtil.createOperationOutcome();
    val response =
        ErpResponse.forPayload(operationOutcome, OperationOutcome.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    assertTrue(response.isOperationOutcome());
    assertTrue(response.isResourceOfType(Resource.class));
    assertTrue(response.getResourceOptional().isPresent());
  }

  @Test
  void shouldHaveDefaultContentLengthOfZero() {
    val response =
        ErpResponse.forPayload(null, KbvErpBundle.class)
            .withStatusCode(500)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    assertTrue(response.isEmptyBody());
    assertEquals(0L, response.getContentLength());
    assertNull(response.getAsBaseResource());
  }

  @Test
  void shouldHaveDefaultContentLengthOfZero2() {
    val headers = Map.of("content-length", "");
    val response =
        ErpResponse.forPayload(null, KbvErpBundle.class)
            .withStatusCode(500)
            .withHeaders(headers)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    assertTrue(response.isEmptyBody());
    assertEquals(0L, response.getContentLength());
    assertNull(response.getAsBaseResource());
  }

  @Test
  void shouldDetectEmptyBody() {
    val headers = Map.of("content-length", "0");
    val response =
        ErpResponse.forPayload(null, KbvErpBundle.class)
            .withStatusCode(500)
            .withHeaders(headers)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    assertTrue(response.isEmptyBody());
    assertEquals(0L, response.getContentLength());
    assertNull(response.getAsBaseResource());
  }

  @Test
  void shouldDetectBodyContent() {
    val headers = Map.of("content-length", "10");
    val operationOutcome = FhirTestResourceUtil.createOperationOutcome();
    val response =
        ErpResponse.forPayload(operationOutcome, OperationOutcome.class)
            .withStatusCode(404)
            .withHeaders(headers)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    assertFalse(response.isEmptyBody());
    assertEquals(10L, response.getContentLength());
    assertEquals("10", response.getHeaderValue("content-length"));
  }

  @Test
  void shouldDetectJson() {
    val headers = Map.of("content-type", "application/fhir+json; fhirVersion=4.0; charset=utf-8");
    val response =
        ErpResponse.forPayload(null, KbvErpBundle.class)
            .withStatusCode(500)
            .withHeaders(headers)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    assertTrue(response.isJson());
    assertFalse(response.isXML());
  }

  @Test
  void shouldDetectXml() {
    val headers = Map.of("content-type", "application/fhir+xml; fhirVersion=4.0");
    val response =
        ErpResponse.forPayload(null, KbvErpBundle.class)
            .withStatusCode(500)
            .withHeaders(headers)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    assertTrue(response.isXML());
    assertFalse(response.isJson());
  }

  @Test
  void doesNotThrowExceptionOnToString() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    assertDoesNotThrow(response::toString);
  }

  @Test
  void shouldHaveValidPayloadOnEmptyValidationResult() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    assertTrue(response.isValidPayload());
  }

  @Test
  void shouldThrowOnInvalidPayload() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createFailingValidationResult());
    assertFalse(response.isValidPayload());
    assertThrows(FhirValidationException.class, response::getExpectedResource);
  }

  @Test
  void shouldGetEmptyOptionalOnNullResource() {
    val response =
        ErpResponse.forPayload(null, KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createFailingValidationResult());
    assertTrue(response.getResourceOptional().isEmpty());
  }
}
