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

package de.gematik.test.erezept.primsys.rest.response;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.primsys.data.error.ErrorDto;
import de.gematik.test.erezept.primsys.data.error.ErrorType;
import jakarta.ws.rs.WebApplicationException;
import java.util.Map;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;

class ErrorResponseBuilderTest extends ErpFhirBuildingTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void shouldHavePrivateConstructor() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(ErrorResponseBuilder.class));
  }

  @Test
  void shouldEncodeInternalErrorResponse() throws JsonProcessingException {
    val response = ErrorResponseBuilder.createInternalError(400, "internal error");
    val entity = response.getEntity();

    val json = MAPPER.writeValueAsString(entity);
    assertNotNull(json);
    val errorDto = MAPPER.readValue(json, ErrorDto.class);
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

    val json = MAPPER.writeValueAsString(entity);
    assertNotNull(json);
    val errorDto = MAPPER.readValue(json, ErrorDto.class);
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

    val json = MAPPER.writeValueAsString(entity);
    assertNotNull(json);
    val errorDto = MAPPER.readValue(json, ErrorDto.class);
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
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
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
