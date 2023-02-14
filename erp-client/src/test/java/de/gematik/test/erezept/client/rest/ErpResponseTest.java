/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
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
import de.gematik.test.erezept.fhir.testutil.*;
import java.util.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.*;

class ErpResponseTest {

  @Test
  void getOptionalResourceType() {
    val response = new ErpResponse(200, Map.of(), KbvErpBundleBuilder.faker("X12345673").build());
    assertTrue(response.getResourceOptional(KbvErpBundle.class).isPresent());
  }

  @Test
  void getEmptyOptionalResourceType() {
    val response = new ErpResponse(200, Map.of(), KbvErpBundleBuilder.faker("X12345673").build());
    assertTrue(response.getResourceOptional(OperationOutcome.class).isEmpty());
  }

  @Test
  void shouldThrowOnUnexpectedResource() {
    val response = new ErpResponse(200, Map.of(), KbvErpBundleBuilder.faker("X12345673").build());
    assertThrows(UnexpectedResponseResourceError.class, () -> response.getResource(ErxTask.class));
  }

  @Test
  void shouldReturnNullableResourceType() {
    val response = new ErpResponse(200, Map.of(), null);
    assertNull(response.getResourceType());
  }

  @Test
  void shouldDetectOperationOutcome() {
    val response = new ErpResponse(404, Map.of(), FhirTestResourceUtil.createOperationOutcome());
    assertTrue(response.isOperationOutcome());
  }

  @Test
  void shouldAlsoReturnGenericResource() {
    val response = new ErpResponse(404, Map.of(), FhirTestResourceUtil.createOperationOutcome());
    assertTrue(response.isOperationOutcome());
    assertTrue(response.isResourceOfType(Resource.class));
    assertTrue(response.getResourceOptional(Resource.class).isPresent());
  }

  @Test
  void shouldDetectEmptyBody() {
    val headers = Map.of("content-length", "0");
    val response = new ErpResponse(500, headers, null);
    assertTrue(response.isEmptyBody());
    assertEquals(0L, response.getContentLength());
  }

  @Test
  void shouldDetectJson() {
    val headers = Map.of("content-type", "application/fhir+json; fhirVersion=4.0; charset=utf-8");
    val response = new ErpResponse(500, headers, null);
    assertTrue(response.isJson());
    assertFalse(response.isXML());
  }

  @Test
  void shouldDetectXml() {
    val headers = Map.of("content-type", "application/fhir+xml; fhirVersion=4.0");
    val response = new ErpResponse(500, headers, null);
    assertTrue(response.isXML());
    assertFalse(response.isJson());
  }

  @Test
  void doesNotThrowExceptionToString() {
    val response = new ErpResponse(200, Map.of(), KbvErpBundleBuilder.faker("X12345673").build());
    assertDoesNotThrow(response::toString);
  }
}
