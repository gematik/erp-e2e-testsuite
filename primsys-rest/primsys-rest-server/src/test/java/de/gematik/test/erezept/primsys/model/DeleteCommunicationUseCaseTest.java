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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.CommunicationDeleteCommand;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

class DeleteCommunicationUseCaseTest extends TestWithActorContext {

  @Test
  void shouldDeleteCommunication() {
    val ctx = ActorContext.getInstance();
    val pharm = ctx.getPharmacies().get(0);
    val mockClient = pharm.getClient();

    val erpResponse =
        ErpResponse.forPayload(null, EmptyResource.class)
            .withStatusCode(204)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());

    when(mockClient.request(any(CommunicationDeleteCommand.class))).thenReturn(erpResponse);

    val uc = new DeleteCommunicationUseCase(pharm);
    val r = assertDoesNotThrow(() -> uc.forId("123"));
    assertEquals(204, r.getStatus());
  }
}
