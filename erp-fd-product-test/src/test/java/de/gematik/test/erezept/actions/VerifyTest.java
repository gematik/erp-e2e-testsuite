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

package de.gematik.test.erezept.actions;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.hasWorkflowType;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.ErpActor;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.Map;
import lombok.val;
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
    val cmd = new TaskCreateCommand();
    val response = new ErpResponse(404, Map.of(), FhirTestResourceUtil.createOperationOutcome());
    val interaction = new ErpInteraction<>(cmd, response);

    actor.attemptsTo(
        Verify.that(interaction)
            .withOperationOutcome(ErpAfos.A_19018)
            .hasResponseWith(returnCode(404))
            .isCorrect());
  }

  @Test
  void shouldVerifyWithExpectedType() {
    val cmd = new TaskCreateCommand();
    val task = new ErxTask();
    val coding = PrescriptionFlowType.FLOW_TYPE_160.asCoding(true);
    task.addExtension(ErpStructureDefinition.GEM_PRESCRIPTION_TYPE.getCanonicalUrl(), coding);

    val response = new ErpResponse(201, Map.of(), task);
    val interaction = new ErpInteraction<>(cmd, response);

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
}
