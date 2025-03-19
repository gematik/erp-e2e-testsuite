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
 */

package de.gematik.test.erezept.arguments;

import static de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer.workflowAndMedicationComposer;
import static de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer.workflowPharmacyOnlyAndMedicationComposer;
import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.jupiter.api.Test;

class WorkflowAndMedicationComposerTest {

  @Test
  void shouldBuildWorkflowAndMedicationCorrect() {
    val wAMC = workflowAndMedicationComposer().create().toList();
    assertFalse(wAMC.isEmpty());
    assertEquals(16, wAMC.size());
  }

  @Test
  void shouldBuildWorkflowPharmacyOnlyAndMedicationCorrect() {
    val wAMC = workflowPharmacyOnlyAndMedicationComposer().toList();
    assertFalse(wAMC.isEmpty());
    assertEquals(8, wAMC.size());
  }
}
