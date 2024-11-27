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

package de.gematik.test.erezept.primsys.actors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.config.dto.actor.PharmacyConfiguration;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.model.ActorContext;
import jakarta.ws.rs.WebApplicationException;
import java.security.MessageDigest;
import java.util.Map;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;

class BaseActorTest extends TestWithActorContext {

  @Test
  void shouldProcessOperationOutcomeFdResponse() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(1);
    val mockClient = pharmacy.getClient();

    val mockResponse =
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), ErxTask.class)
            .withHeaders(Map.of())
            .withStatusCode(500)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(mockClient.request(any(TaskCreateCommand.class))).thenReturn(mockResponse);
    val command = new TaskCreateCommand();
    assertThrows(WebApplicationException.class, () -> pharmacy.erpRequest(command));
  }

  @Test
  void shouldProcessErrorFdResponse() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(1);
    val mockClient = pharmacy.getClient();

    val mockResponse =
        ErpResponse.forPayload(new ErxTask(), ErxTask.class)
            .withHeaders(Map.of())
            .withStatusCode(500)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(mockClient.request(any(TaskCreateCommand.class))).thenReturn(mockResponse);
    val command = new TaskCreateCommand();
    assertThrows(WebApplicationException.class, () -> pharmacy.erpRequest(command));
  }

  @Test
  void shouldSneakilyThrowOnMessageDigestError() {
    try (val mdMock = mockStatic(MessageDigest.class)) {
      mdMock.when(() -> MessageDigest.getInstance("MD5")).thenThrow(new RuntimeException("test"));

      val cfg = new PharmacyConfiguration();
      cfg.setName("Pharmacy");
      assertThrows(RuntimeException.class, () -> new Pharmacy(cfg, null, null, null));
    }
  }

  @Test
  void shouldForwardToEncode() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(1);
    val content = assertDoesNotThrow(() -> pharmacy.encode(new Bundle(), EncodingType.XML));
    assertNotNull(content);
  }
}
