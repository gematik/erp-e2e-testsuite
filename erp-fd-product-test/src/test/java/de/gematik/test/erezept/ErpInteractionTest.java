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

package de.gematik.test.erezept;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import java.util.Map;
import lombok.val;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;

class ErpInteractionTest {

  @Test
  void shouldBuildExpectation() {
    val response =
        ErpResponse.forPayload(new ErxTask(), ErxTask.class)
            .withStatusCode(201)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    val interaction = new ErpInteraction<>(response);

    assertDoesNotThrow(interaction::expectation);
    assertNotNull(interaction.expectation());
  }

  @Test
  void shouldProvideExpectedRessource() {
    val response =
        ErpResponse.forPayload(new ErxTask(), ErxTask.class)
            .withStatusCode(201)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    val interaction = new ErpInteraction<>(response);

    assertDoesNotThrow(interaction::getExpectedResponse);
    assertNotNull(interaction.getExpectedResponse());
  }

  @Test
  void shouldProvideOperationOutcomeExpectation() {
    val response =
        ErpResponse.forPayload(new ErxTask(), ErxTask.class)
            .withStatusCode(201)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    val interaction = new ErpInteraction<>(response);

    assertDoesNotThrow(interaction::asOperationOutcome);
    assertNotNull(interaction.asOperationOutcome());
  }

  @Test
  void shouldThrowOnUnexpected() {
    val response =
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), ErxTask.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    val interaction = new ErpInteraction<>(response);

    assertThrows(UnexpectedResponseResourceError.class, interaction::getExpectedResponse);
  }
}
