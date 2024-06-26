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

package de.gematik.test.erezept.primsys.actors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.model.ActorContext;
import jakarta.ws.rs.WebApplicationException;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

class BaseActorTest extends TestWithActorContext {

  @Test
  void shouldCreateErpResponse() {
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
}
