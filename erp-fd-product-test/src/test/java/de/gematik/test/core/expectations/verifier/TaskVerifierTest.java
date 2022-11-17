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

package de.gematik.test.core.expectations.verifier;

import static de.gematik.test.core.expectations.verifier.TaskVerifier.hasValidPrescriptionId;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.hasWorkflowType;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskVerifierTest {

  @BeforeEach
  void setupReporter() {
    // need to start a testcase manually as we are not using the ErpTestExtension here
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  @Test
  void shouldThrowOnInvalidPrescriptionFlowType() {
    val task = new ErxTask();
    val coding = PrescriptionFlowType.FLOW_TYPE_169.asCoding(true);
    task.addExtension(ErpWorkflowStructDef.PRESCRIPTION_TYPE.getCanonicalUrl(), coding);

    val step = hasWorkflowType(PrescriptionFlowType.FLOW_TYPE_160);
    assertThrows(AssertionError.class, () -> step.apply(task));
  }

  @Test
  void shouldPassOnCorrectPrescriptionFlowType() {
    val task = new ErxTask();
    val coding = PrescriptionFlowType.FLOW_TYPE_160.asCoding(true);
    task.addExtension(ErpWorkflowStructDef.PRESCRIPTION_TYPE.getCanonicalUrl(), coding);

    val step = hasWorkflowType(PrescriptionFlowType.FLOW_TYPE_160);
    step.apply(task);
  }

  @Test
  void shouldFailOnMissingPrescriptionId() {
    val task = new ErxTask();

    val step = hasValidPrescriptionId();
    assertThrows(AssertionError.class, () -> step.apply(task));
  }
}
