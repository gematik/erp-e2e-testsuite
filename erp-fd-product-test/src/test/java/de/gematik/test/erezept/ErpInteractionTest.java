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

package de.gematik.test.erezept;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErpInteractionTest {

  @Test
  void shouldBuildExpectation() {
    val cmd = new TaskCreateCommand();
    val response = new ErpResponse(201, Map.of(), new ErxTask());
    val interaction = new ErpInteraction<>(cmd, response);

    assertDoesNotThrow(interaction::expectation);
    assertNotNull(interaction.expectation());
  }

  @Test
  void shouldProvideExpectedRessource() {
    val cmd = new TaskCreateCommand();
    val response = new ErpResponse(201, Map.of(), new ErxTask());
    val interaction = new ErpInteraction<>(cmd, response);

    assertDoesNotThrow(interaction::getExpectedResponse);
    assertNotNull(interaction.getExpectedResponse());
  }

  @Test
  void shouldProvideOperationOutcomeExpectation() {
    val cmd = new TaskCreateCommand();
    val response = new ErpResponse(201, Map.of(), new ErxTask());
    val interaction = new ErpInteraction<>(cmd, response);

    assertDoesNotThrow(interaction::asOperationOutcome);
    assertNotNull(interaction.asOperationOutcome());
  }

  @Test
  void shouldThrowOnUnexpected() {
    val cmd = new TaskCreateCommand();
    val response = new ErpResponse(404, Map.of(), FhirTestResourceUtil.createOperationOutcome());
    val interaction = new ErpInteraction<>(cmd, response);

    assertThrows(UnexpectedResponseResourceError.class, interaction::getExpectedResponse);
  }
}
