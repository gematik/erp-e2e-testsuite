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

package de.gematik.test.erezept.actions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.usecases.GetCapabilityStatementCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxCapabilityStatement;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import lombok.val;
import org.junit.jupiter.api.Test;

class ResponseOfGetCapabilityStatementTest {

  @Test
  void shouldPerformGetCapabilityStatementRequest() {
    val actor = new PharmacyActor("Test-Apotheke");
    val useErpClient = mock(UseTheErpClient.class);

    ErpInteraction<ErxCapabilityStatement> mockInteraction = mock(ErpInteraction.class);

    actor.can(useErpClient);

    ResponseOfGetCapabilityStatement action = spy(ResponseOfGetCapabilityStatement.request());

    doReturn(mockInteraction)
        .when(action)
        .performCommandAs(any(GetCapabilityStatementCommand.class), eq(actor));

    ErpInteraction<ErxCapabilityStatement> result = action.answeredBy(actor);
    assertSame(mockInteraction, result);
  }
}
