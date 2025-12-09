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

package de.gematik.test.erezept.client.rest;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.*;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.test.erezept.client.exceptions.FhirValidationException;
import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

class ErpResponseTest extends ErpFhirBuildingTest {

  @Test
  void getOptionalResourceType() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    assertTrue(response.getResourceOptional().isPresent());
  }

  @Test
  void getEmptyOptionalResourceType() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    assertTrue(response.getResourceOptional(OperationOutcome.class).isEmpty());
  }

  @Test
  void shouldThrowOnUnexpectedResource() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), ErxTask.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    assertThrows(UnexpectedResponseResourceError.class, response::getExpectedResource);
  }

  @Test
  void shouldReturnEmptyResourceTypeOnNull() {
    val response =
        ErpResponse.forPayload(null, KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    assertNotNull(response.getResourceType());
    assertEquals(EmptyResource.class, response.getResourceType());
  }

  @Test
  void shouldDetectOperationOutcome() {
    val operationOutcome = createOperationOutcome();
    val response =
        ErpResponse.forPayload(operationOutcome, OperationOutcome.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    assertTrue(response.isOperationOutcome());
    assertDoesNotThrow(response::getAsOperationOutcome);
  }

  @Test
  void shouldAlsoReturnGenericResource() {
    val operationOutcome = createOperationOutcome();
    val response =
        ErpResponse.forPayload(operationOutcome, OperationOutcome.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
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
            .andValidationResult(createEmptyValidationResult());
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
            .andValidationResult(createEmptyValidationResult());
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
            .andValidationResult(createEmptyValidationResult());
    assertTrue(response.isEmptyBody());
    assertEquals(0L, response.getContentLength());
    assertNull(response.getAsBaseResource());
  }

  @Test
  void shouldDetectBodyContent() {
    val headers = Map.of("content-length", "10");
    val operationOutcome = createOperationOutcome();
    val response =
        ErpResponse.forPayload(operationOutcome, OperationOutcome.class)
            .withStatusCode(404)
            .withHeaders(headers)
            .andValidationResult(createEmptyValidationResult());
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
            .andValidationResult(createEmptyValidationResult());
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
            .andValidationResult(createEmptyValidationResult());
    assertTrue(response.isXML());
    assertFalse(response.isJson());
  }

  @Test
  void doesNotThrowExceptionOnToString() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    assertDoesNotThrow(response::toString);
  }

  @Test
  void shouldHaveValidPayloadOnEmptyValidationResult() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    assertTrue(response.isValidPayload());
  }

  @Test
  void shouldThrowOnInvalidPayload() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createFailingValidationResult());
    assertFalse(response.isValidPayload());
    assertThrows(FhirValidationException.class, response::getExpectedResource);
  }

  @Test
  void shouldGetEmptyOptionalOnNullResource() {
    val response =
        ErpResponse.forPayload(null, KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createFailingValidationResult());
    assertTrue(response.getResourceOptional().isEmpty());
  }

  @ParameterizedTest
  @MethodSource
  @NullSource
  void shouldThrowCustomExceptionOnUnexpected(Resource resource) {
    val response =
        ErpResponse.forPayload(resource, ErxTask.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    Function<ErpResponse<? extends Resource>, RuntimeException> errorFunction =
        r -> new RuntimeException("test");
    assertThrows(RuntimeException.class, () -> response.getExpectedOrThrow(errorFunction));
  }

  static Stream<Arguments> shouldThrowCustomExceptionOnUnexpected() {
    return Stream.of(KbvErpBundleFaker.builder().fake(), createOperationOutcome())
        .map(Arguments::of);
  }

  @Test
  void shouldNotThrowOnExpectedResource() {
    val resource = KbvErpBundleFaker.builder().fake();
    val response =
        ErpResponse.forPayload(resource, KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    Function<ErpResponse<? extends Resource>, RuntimeException> errorFunction =
        r -> new RuntimeException("test");
    val r2 = assertDoesNotThrow(() -> response.getExpectedOrThrow(errorFunction));
    assertEquals(r2, resource);
  }
}
