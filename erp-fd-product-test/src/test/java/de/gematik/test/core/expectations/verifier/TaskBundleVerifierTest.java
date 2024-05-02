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

package de.gematik.test.core.expectations.verifier;

import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.erp.ErxTaskBundle;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.val;
import org.hl7.fhir.r4.model.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskBundleVerifierTest {

  @BeforeEach
  void setupReporter() {
    // need to start a testcase manually as we are not using the ErpTestExtension here
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  @Test
  void doesContainsErxTasksWithoutQES() {
    val erxTaskBundle1 = new ErxTaskBundle();
    erxTaskBundle1.addEntry().setResource(new Binary());

    val erxTaskBundle2 = new ErxTaskBundle();
    val task = new ErxTask();
    task.getMeta().addProfile(ErpWorkflowStructDef.TASK_12.getCanonicalUrl());
    task.getContained().add(new Binary());
    erxTaskBundle2.addEntry().setResource(task);

    val step = TaskBundleVerifier.doesContainsErxTasksWithoutQES();
    assertThrows(AssertionError.class, () -> step.apply(erxTaskBundle1));
    assertThrows(AssertionError.class, () -> step.apply(erxTaskBundle2));
  }

  @Test
  void containsExclusivelyTasksWithGKVInsuranceType() {
    val erxTaskBundle1 = new ErxTaskBundle();
    val task = new ErxTask();
    task.getMeta().addProfile(ErpWorkflowStructDef.TASK_12.getCanonicalUrl());
    task.addExtension()
        .setUrl(ErpWorkflowStructDef.PRESCRIPTION_TYPE_12.getCanonicalUrl())
        .setValue(PrescriptionFlowType.FLOW_TYPE_200.asCoding());
    erxTaskBundle1.addEntry().setResource(task);

    val step = TaskBundleVerifier.containsExclusivelyTasksWithGKVInsuranceType();
    assertThrows(AssertionError.class, () -> step.apply(erxTaskBundle1));
  }
}
