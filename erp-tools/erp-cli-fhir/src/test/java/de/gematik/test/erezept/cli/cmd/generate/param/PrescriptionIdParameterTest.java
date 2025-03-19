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

package de.gematik.test.erezept.cli.cmd.generate.param;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.val;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class PrescriptionIdParameterTest extends ErpFhirBuildingTest {

  @Test
  void shouldNotRequireAnyOptions() {
    val pidp = new PrescriptionIdParameter();
    val cmdline = new CommandLine(pidp);
    assertDoesNotThrow(() -> cmdline.parseArgs());

    assertNotNull(pidp.getPrescriptionId());
  }

  @Test
  void shouldSetPrescriptionId() {
    val pidp = new PrescriptionIdParameter();
    val cmdline = new CommandLine(pidp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--prescriptionid", "123"));

    assertNotNull(pidp.getPrescriptionId());
    assertEquals("123", pidp.getPrescriptionId().getValue());
  }

  @Test
  void shouldSetPrescriptionIdWithFlowType() {
    val pidp = new PrescriptionIdParameter();
    val cmdline = new CommandLine(pidp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--flowtype", "160"));

    assertNotNull(pidp.getPrescriptionId());
    assertEquals(PrescriptionFlowType.FLOW_TYPE_160, pidp.getPrescriptionId().getFlowType());
  }
}
