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

package de.gematik.test.erezept.primsys.model;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.CommunicationGetCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunicationBundle;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.rest.params.CommunicationFilterParams;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

class GetCommunicationsUseCaseTest extends TestWithActorContext {

  @Test
  void shouldEmptyFetchCommunications() {
    val ctx = ActorContext.getInstance();
    val pharm = ctx.getPharmacies().get(0);
    val mockClient = pharm.getClient();

    val params = new CommunicationFilterParams();
    val uc = new GetCommunicationsUseCase(pharm);

    val body = new ErxCommunicationBundle();
    val erpResponse =
        ErpResponse.forPayload(body, ErxCommunicationBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());

    when(mockClient.request(any(CommunicationGetCommand.class))).thenReturn(erpResponse);

    val r = assertDoesNotThrow(() -> uc.getCommunications(params));
    assertEquals(200, r.getStatus());
    val list = assertInstanceOf(List.class, r.getEntity());
    assertTrue(list.isEmpty());
  }
}
