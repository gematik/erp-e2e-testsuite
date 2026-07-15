/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createOperationOutcome;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.rest.fd.FhirBResponse;
import de.gematik.bbriccs.rest.fd.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErpInteractionTest {

  @Test
  void shouldBuildExpectation() {
    val response =
        FhirBResponse.forPayload(ErxTask.class, new ErxTask())
            .withStatusCode(201)
            .andValidationResult(createEmptyValidationResult());
    val interaction = new ErpInteraction<>(response);

    assertDoesNotThrow(interaction::expectation);
    assertNotNull(interaction.expectation());
  }

  @Test
  void shouldProvideExpectedRessource() {
    val response =
        FhirBResponse.forPayload(ErxTask.class, new ErxTask())
            .withStatusCode(201)
            .andValidationResult(createEmptyValidationResult());
    val interaction = new ErpInteraction<>(response);

    assertDoesNotThrow(interaction::getExpectedResponse);
    assertNotNull(interaction.getExpectedResponse());
  }

  @Test
  void shouldProvideOperationOutcomeExpectation() {
    val response =
        FhirBResponse.forPayload(ErxTask.class, new ErxTask())
            .withStatusCode(201)
            .andValidationResult(createEmptyValidationResult());
    val interaction = new ErpInteraction<>(response);

    assertDoesNotThrow(interaction::asOperationOutcome);
    assertNotNull(interaction.asOperationOutcome());
  }

  @Test
  void shouldThrowOnUnexpected() {
    val response =
        FhirBResponse.forPayload(ErxTask.class, createOperationOutcome())
            .withStatusCode(404)
            .andValidationResult(createEmptyValidationResult());
    val interaction = new ErpInteraction<>(response);

    assertThrows(UnexpectedResponseResourceError.class, interaction::getExpectedResponse);
  }
}
