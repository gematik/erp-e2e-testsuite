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

package de.gematik.test.erezept.screenplay.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import lombok.val;
import org.junit.jupiter.api.Test;

class DmcPrescriptionTest {

  @Test
  void shouldGenerateDmcAsOwner() {
    val dmc = DmcPrescription.ownerDmc(TaskId.from("taskId"), AccessCode.random());
    assertFalse(dmc.isRepresentative());
  }

  @Test
  void shouldGenerateDmcAsRepresentative() {
    val dmc = DmcPrescription.representativeDmc(TaskId.from("taskId"), AccessCode.random());
    assertTrue(dmc.isRepresentative());
  }
}
