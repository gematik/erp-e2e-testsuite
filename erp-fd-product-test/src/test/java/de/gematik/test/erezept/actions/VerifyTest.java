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

package de.gematik.test.erezept.actions;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createOperationOutcome;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.hasWorkflowType;

import de.gematik.bbriccs.rest.fd.FhirBResponse;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.ErpActor;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.val;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class VerifyTest {

  private static ErpActor actor;

  @BeforeAll
  static void setup() {
    actor = new DoctorActor("MockDoc");
    CoverageReporter.getInstance().startTestcase("don't care");
  }

  @Test
  void shouldVerifyOperationOutcomeInteraction() {
    val response =
        FhirBResponse.forPayload(OperationOutcome.class, createOperationOutcome())
            .withStatusCode(404)
            .andValidationResult(createEmptyValidationResult());
    val interaction = new ErpInteraction<>(response);

    actor.attemptsTo(
        Verify.that(interaction)
            .withOperationOutcome(ErpAfos.A_19018)
            .hasResponseWith(returnCode(404))
            .isCorrect());
  }

  @Test
  void shouldVerifyWithExpectedType() {
    val task = new ErxTask();
    val coding = PrescriptionFlowType.FLOW_TYPE_160.asCoding(true);
    task.addExtension(ErpWorkflowStructDef.PRESCRIPTION_TYPE.getCanonicalUrl(), coding);

    val response =
        FhirBResponse.forPayload(ErxTask.class, task)
            .withStatusCode(201)
            .andValidationResult(createEmptyValidationResult());
    val interaction = new ErpInteraction<>(response);

    actor.attemptsTo(
        Verify.that(interaction)
            .withExpectedType(ErpAfos.A_19018)
            .hasResponseWith(returnCode(201))
            .and(hasWorkflowType(PrescriptionFlowType.FLOW_TYPE_160))
            .is(
                hasWorkflowType(
                    PrescriptionFlowType
                        .FLOW_TYPE_160)) // sounds stupid but covers an additional method call
            .isCorrect());
  }

  @Test
  void shouldVerifyWithoutBody() {
    val task = new ErxTask();
    val response =
        FhirBResponse.forPayload(ErxTask.class, task)
            .withStatusCode(201)
            .andValidationResult(createEmptyValidationResult());
    val interaction = new ErpInteraction<>(response);
    actor.attemptsTo(
        Verify.that(interaction).withoutBody().hasResponseWith(returnCode(201)).isCorrect());
    actor.attemptsTo(
        Verify.that(interaction).withoutBody().responseWith(returnCode(201)).isCorrect());
    actor.attemptsTo(
        Verify.that(interaction).withoutBody().andResponse(returnCode(201)).isCorrect());
  }

  @Test
  void shouldVerifyIsFromExpectedType() {
    val task = new ErxTask();
    val coding = PrescriptionFlowType.FLOW_TYPE_160.asCoding(true);
    task.addExtension(ErpWorkflowStructDef.PRESCRIPTION_TYPE.getCanonicalUrl(), coding);

    val response =
        FhirBResponse.forPayload(ErxTask.class, task)
            .withStatusCode(201)
            .andValidationResult(createEmptyValidationResult());
    val interaction = new ErpInteraction<>(response);
    actor.attemptsTo(Verify.that(interaction).isFromExpectedType());
  }
}
