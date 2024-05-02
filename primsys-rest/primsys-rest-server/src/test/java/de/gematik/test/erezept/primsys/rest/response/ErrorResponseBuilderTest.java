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

package de.gematik.test.erezept.primsys.rest.response;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import de.gematik.test.erezept.primsys.data.error.ErrorDto;
import de.gematik.test.erezept.primsys.data.error.ErrorType;
import de.gematik.test.erezept.testutil.PrivateConstructorsUtil;
import jakarta.ws.rs.WebApplicationException;
import java.util.Map;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ErrorResponseBuilderTest {

  private ObjectMapper mapper;

  @BeforeEach
  void setup() {
    mapper = new ObjectMapper();
  }

  @Test
  void shouldHavePrivateConstructor() {
    assertTrue(PrivateConstructorsUtil.throwsInvocationTargetException(ErrorResponseBuilder.class));
  }

  @Test
  void shouldEncodeInternalErrorResponse() throws JsonProcessingException {
    val response = ErrorResponseBuilder.createInternalError(400, "internal error");
    val entity = response.getEntity();

    val json = mapper.writeValueAsString(entity);
    assertNotNull(json);
    val errorDto = mapper.readValue(json, ErrorDto.class);
    assertEquals(ErrorType.INTERNAL, errorDto.getType());
  }

  @Test
  void shouldThrowInternalErrorResponse() {
    assertThrows(
        WebApplicationException.class,
        () -> ErrorResponseBuilder.throwInternalError(400, "internal error"));
  }

  @Test
  void shouldCreateInternalErrorResponseFromThrowable() throws JsonProcessingException {
    val throwable = new NullPointerException("for testing");
    val response = ErrorResponseBuilder.createInternalError(throwable);
    val entity = response.getEntity();

    val json = mapper.writeValueAsString(entity);
    assertNotNull(json);
    val errorDto = mapper.readValue(json, ErrorDto.class);
    assertEquals(ErrorType.INTERNAL, errorDto.getType());
  }

  @Test
  void shouldEncodeFachdienstErrorResponse() throws JsonProcessingException {
    val erpResponse =
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), Resource.class)
            .withStatusCode(500)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    val response = ErrorResponseBuilder.createFachdienstError(erpResponse);
    val entity = response.getEntity();

    val json = mapper.writeValueAsString(entity);
    assertNotNull(json);
    val errorDto = mapper.readValue(json, ErrorDto.class);
    assertEquals(ErrorType.FACHDIENST, errorDto.getType());
  }

  @Test
  void shouldThrowFachdienstErrorResponse() {
    val erpResponse =
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), Resource.class)
            .withStatusCode(500)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    assertThrows(
        WebApplicationException.class,
        () -> ErrorResponseBuilder.throwFachdienstError(erpResponse));
  }

  @Test
  void shouldThrowInternalErrorOnMissingOperationOutcome() {
    val erpResponse =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of("content-length", "10"))
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    try {
      val response = ErrorResponseBuilder.createFachdienstError(erpResponse);
    } catch (WebApplicationException wae) {
      val errorDto = (ErrorDto) wae.getResponse().getEntity();
      assertEquals(ErrorType.INTERNAL, errorDto.getType());
    }
  }

  @Test
  void shouldThrowInternalErrorOnMissingOperationOutcomeWithEmptyBody() {
    val erpResponse =
        ErpResponse.forPayload(null, Resource.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    try {
      val response = ErrorResponseBuilder.createFachdienstError(erpResponse);
    } catch (WebApplicationException wae) {
      val errorDto = (ErrorDto) wae.getResponse().getEntity();
      assertEquals(ErrorType.INTERNAL, errorDto.getType());
    }
  }
}
