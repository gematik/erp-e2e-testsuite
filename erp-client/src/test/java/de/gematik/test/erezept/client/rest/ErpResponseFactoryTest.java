/*
 * Copyright (c) 2022 gematik GmbH
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

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import ca.uhn.fhir.parser.DataFormatException;
import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.val;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ErpResponseFactoryTest {

  private final Map<String, String> HEADERS_JSON =
      Map.of("content-type", MediaType.FHIR_JSON.asString());
  private final int STATUS_OK = 200;
  private final int STATUS_ERROR = 500;

  private final String RESOURCE_PATH_ERP = "fhir/valid/erp/1.1.1/";

  private static ErpResponseFactory responseFactory;

  @BeforeAll
  static void setUp() {
    responseFactory = new ErpResponseFactory(new FhirParser());
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
                  responseFactory.createFrom(STATUS_OK, HEADERS_JSON, content, ErxAuditEvent.class);
              val auditEvent = response.getResource();
              assertNotNull(
                  auditEvent,
                  format("Response must contain a Resource of Type {0}", ErxAuditEvent.class));
            });
  }

  @Test
  void unexpectedOperationOutcomeResponse() {
    val testOperationOutcome = FhirTestResourceUtil.createOperationOutcome();
    val response = responseFactory.createFrom(STATUS_ERROR, HEADERS_JSON, testOperationOutcome);
    val resource = response.getResource();
    assertNotNull(
        resource, format("Response must contain a Resource of Type {0}", OperationOutcome.class));
    assertTrue(resource instanceof OperationOutcome, "Resource is expected to be OperationOutcome");
    assertTrue(
        Objects.requireNonNull(response.getResourceType()).isInstance(testOperationOutcome),
        "Resource is expected to be the original OperationOutcome");

    // get the concrete OperationOutcome
    val concreteResource = response.getResource(OperationOutcome.class);
    assertNotNull(
        concreteResource, format("Resource must be castable to Type {0}", OperationOutcome.class));
  }

  @Test
  void expectedOperationOutcome() {
    val testOperationOutcome = FhirTestResourceUtil.createOperationOutcome();
    val response = responseFactory.createFrom(STATUS_ERROR, HEADERS_JSON, testOperationOutcome);
    val outputOO = response.getResource();
    assertNotNull(
        outputOO, format("Response must contain a Resource of Type {0}", OperationOutcome.class));
    assertTrue(outputOO instanceof OperationOutcome, "Resource is expected to be OperationOutcome");
    assertTrue(
        Objects.requireNonNull(response.getResourceType()).isInstance(testOperationOutcome),
        "Resource is expected to be the original OperationOutcome");

    val concreteResource = response.getResource(OperationOutcome.class);
    assertNotNull(
        concreteResource, format("Resource must be castable to Type {0}", OperationOutcome.class));
  }

  @Test
  void expectOperationOutcomeButWasAuditEvent() {
    val filename = "AuditEvent_01.json";
    val auditEventContent = ResourceUtils.readFileFromResource(RESOURCE_PATH_ERP + filename);
    assertThrows(
        DataFormatException.class,
        () ->
            responseFactory.createFrom(
                STATUS_ERROR, HEADERS_JSON, auditEventContent, OperationOutcome.class));
  }

  @Test
  void fetchUnexpectedResponseResource() {
    val testOperationOutcome = FhirTestResourceUtil.createOperationOutcome();
    val response = responseFactory.createFrom(STATUS_ERROR, HEADERS_JSON, testOperationOutcome);
    assertThrows(
        UnexpectedResponseResourceError.class, () -> response.getResource(ErxAuditEvent.class));
  }

  @Test
  void fetchUnexpectedResponseResourceOptional() {
    val testOperationOutcome = FhirTestResourceUtil.createOperationOutcome();
    val response = responseFactory.createFrom(STATUS_ERROR, HEADERS_JSON, testOperationOutcome);
    val resource = response.getResourceOptional(ErxAuditEvent.class);
    assertTrue(resource.isEmpty());
  }

  @Test
  void isResourceOfType() {
    val testOperationOutcome = FhirTestResourceUtil.createOperationOutcome();
    val response = responseFactory.createFrom(STATUS_ERROR, HEADERS_JSON, testOperationOutcome);
    assertTrue(response.isResourceOfType(OperationOutcome.class));
  }

  @Test
  void isNotResourceOfType() {
    val testOperationOutcome = FhirTestResourceUtil.createOperationOutcome();
    val response = responseFactory.createFrom(STATUS_ERROR, HEADERS_JSON, testOperationOutcome);
    assertFalse(response.isResourceOfType(ErxAuditEvent.class));
  }
}
