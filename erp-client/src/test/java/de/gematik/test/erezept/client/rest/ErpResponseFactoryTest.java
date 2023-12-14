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

import static java.text.MessageFormat.*;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.client.exceptions.*;
import de.gematik.test.erezept.fhir.parser.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.util.*;
import java.time.Duration;
import java.util.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.*;

class ErpResponseFactoryTest {

  private static FhirParser fhir;
  private static ErpResponseFactory responseFactory;
  private static final Map<String, String> HEADERS_JSON =
      Map.of("content-type", MediaType.FHIR_JSON.asString());
  private static final int STATUS_OK = 200;
  private static final int STATUS_ERROR = 500;
  
  private static final String testToken =
          "eyJhbGciOiJCUDI1NlIxIiwidHlwIjoiYXQrSldUIiwia2lkIjoicHVrX2lkcF9zaWcifQ.eyJzdWIiOiJJWERkLTNyUVpLS0ZYVWR4R0dqNFBERG9WNk0wUThaai1xdzF2cjF1XzU4IiwicHJvZmVzc2lvbk9JRCI6IjEuMi4yNzYuMC43Ni40LjQ5Iiwib3JnYW5pemF0aW9uTmFtZSI6ImdlbWF0aWsgTXVzdGVya2Fzc2UxR0tWTk9ULVZBTElEIiwiaWROdW1tZXIiOiJYMTEwNTAyNDE0IiwiYW1yIjpbIm1mYSIsInNjIiwicGluIl0sImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6NTUwMTEvYXV0aC9yZWFsbXMvaWRwLy53ZWxsLWtub3duL29wZW5pZC1jb25maWd1cmF0aW9uIiwiZ2l2ZW5fbmFtZSI6IlJvYmluIEdyYWYiLCJjbGllbnRfaWQiOiJlcnAtdGVzdHN1aXRlLWZkIiwiYWNyIjoiZ2VtYXRpay1laGVhbHRoLWxvYS1oaWdoIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdDozMDAwLyIsImF6cCI6ImVycC10ZXN0c3VpdGUtZmQiLCJzY29wZSI6Im9wZW5pZCBlLXJlemVwdCIsImF1dGhfdGltZSI6MTY0MzgwNDczMywiZXhwIjoxNjQzODA1MDMzLCJmYW1pbHlfbmFtZSI6IlbDs3Jtd2lua2VsIiwiaWF0IjoxNjQzODA0NjEzLCJqdGkiOiI2Yjg3NmU0MWNmMGViNGJkIn0.MV5cDnL3JBZ4b6xr9SqiYDmZ7qtZFEWBd1vCrHzVniZeDhkyuSYc7xhf577h2S21CzNgrMp0M6JALNW9Qjnw_g";

  private final String RESOURCE_PATH_ERP = "fhir/valid/erp/1.1.1/";

  @BeforeAll
  static void setUp() {
    fhir = new FhirParser();
    responseFactory = new ErpResponseFactory(fhir, false);
  }

  private String encodeTestRessource(Resource resource, EncodingType type) {
    return fhir.encode(resource, type);
  }

  @Test
  void deserializeAuditEventResponses() {
    val auditEvents = List.of("AuditEvent_01.json");

    auditEvents.stream()
        .map(filename -> RESOURCE_PATH_ERP + filename)
        .map(ResourceUtils::readFileFromResource)
        .forEach(
            content -> {
              val response =
                  responseFactory.createFrom(
                      STATUS_OK, Duration.ZERO, HEADERS_JSON, testToken, content, ErxAuditEvent.class);
              val auditEvent = response.getAsBaseResource();
              assertNotNull(
                  auditEvent,
                  format("Response must contain a Resource of Type {0}", ErxAuditEvent.class));
              assertDoesNotThrow(response::getExpectedResource);
            });
  }

  @Test
  void unexpectedOperationOutcomeResponse() {
    val testOperationOutcome =
        encodeTestRessource(FhirTestResourceUtil.createOperationOutcome(), EncodingType.JSON);
    val response =
        responseFactory.createFrom(
            STATUS_ERROR,
            Duration.ZERO,
            HEADERS_JSON,
            testToken,
            testOperationOutcome,
            OperationOutcome.class);
    val resource = response.getAsBaseResource();
    assertNotNull(
        resource, format("Response must contain a Resource of Type {0}", OperationOutcome.class));
    assertTrue(resource instanceof OperationOutcome, "Resource is expected to be OperationOutcome");
    assertTrue(response.isOfExpectedType());

    // get the concrete OperationOutcome
    val concreteResource = response.getAsOperationOutcome();
    assertNotNull(
        concreteResource, format("Resource must be castable to Type {0}", OperationOutcome.class));
  }

  @Test
  void expectedOperationOutcome() {
    val testOperationOutcome =
        encodeTestRessource(FhirTestResourceUtil.createOperationOutcome(), EncodingType.JSON);
    val response =
        responseFactory.createFrom(
            STATUS_ERROR,
            Duration.ZERO,
            HEADERS_JSON,
            testToken,
            testOperationOutcome,
            OperationOutcome.class);
    val outputOO = response.getAsBaseResource();
    assertNotNull(
        outputOO, format("Response must contain a Resource of Type {0}", OperationOutcome.class));
    assertTrue(outputOO instanceof OperationOutcome, "Resource is expected to be OperationOutcome");

    val concreteResource = response.getExpectedResource();
    assertNotNull(
        concreteResource, format("Resource must be castable to Type {0}", OperationOutcome.class));
  }

  @Test
  void shouldReceiveErxAuditEventAlthoughOperationOutcomeExpected() {
    val filename = "AuditEvent_01.json";
    val auditEventContent = ResourceUtils.readFileFromResource(RESOURCE_PATH_ERP + filename);
    val response =
        responseFactory.createFrom(
            STATUS_ERROR, Duration.ZERO, HEADERS_JSON, testToken, auditEventContent, OperationOutcome.class);
    assertFalse(response.isResourceOfType(OperationOutcome.class));
    assertTrue(response.isResourceOfType(ErxAuditEvent.class));
  }

  @Test
  void fetchUnexpectedResponseResource() {
    val testOperationOutcome =
        encodeTestRessource(FhirTestResourceUtil.createOperationOutcome(), EncodingType.JSON);
    val response =
        responseFactory.createFrom(
            STATUS_ERROR, Duration.ZERO, HEADERS_JSON, testToken, testOperationOutcome, ErxAuditEvent.class);
    assertThrows(UnexpectedResponseResourceError.class, response::getExpectedResource);
  }

  @Test
  void fetchUnexpectedResponseResourceOptional() {
    val testOperationOutcome =
        encodeTestRessource(FhirTestResourceUtil.createOperationOutcome(), EncodingType.JSON);
    val response =
        responseFactory.createFrom(
            STATUS_ERROR, Duration.ZERO, HEADERS_JSON, testToken, testOperationOutcome, ErxAuditEvent.class);
    val resource = response.getResourceOptional();
    assertTrue(resource.isEmpty());
  }

  @Test
  void isResourceOfType() {
    val testOperationOutcome =
        encodeTestRessource(FhirTestResourceUtil.createOperationOutcome(), EncodingType.JSON);
    val response =
        responseFactory.createFrom(
            STATUS_ERROR, Duration.ZERO, HEADERS_JSON, testToken, testOperationOutcome, ErxAuditEvent.class);
    assertTrue(response.isResourceOfType(OperationOutcome.class));
    assertTrue(response.isOperationOutcome());
  }

  @Test
  void isNotResourceOfType() {
    val testOperationOutcome =
        encodeTestRessource(FhirTestResourceUtil.createOperationOutcome(), EncodingType.JSON);
    val response =
        responseFactory.createFrom(
            STATUS_ERROR, Duration.ZERO, HEADERS_JSON, testToken, testOperationOutcome, ErxAuditEvent.class);
    assertFalse(response.isResourceOfType(ErxAuditEvent.class));
  }

  @Test
  void shouldValidateEmptyContentCorrectly() {
    val rf = new ErpResponseFactory(fhir, true);
    val response = rf.createFrom(201, HEADERS_JSON, testToken, "", Resource.class);
    assertTrue(response.isValidPayload());
  }

  @Test
  void shouldValidateOperationOutcomeCorrectly() {
    val rf = new ErpResponseFactory(fhir, true);
    val testOperationOutcome =
        encodeTestRessource(FhirTestResourceUtil.createOperationOutcome(), EncodingType.JSON);
    val response = rf.createFrom(404, HEADERS_JSON, testToken, testOperationOutcome, ErxAuditEvent.class);
    assertTrue(response.isValidPayload());
  }

  @Test
  void shouldFailOnInvalidOperationOutcomeCorrectly() {
    val rf = new ErpResponseFactory(fhir, true);
    val testOperationOutcome =
        encodeTestRessource(FhirTestResourceUtil.createOperationOutcome(), EncodingType.JSON)
            .replace("issue", "issues");
    val response = rf.createFrom(404, HEADERS_JSON, testToken, testOperationOutcome, ErxAuditEvent.class);
    assertFalse(response.isValidPayload());
  }
}
