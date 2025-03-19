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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import lombok.val;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class PatientParameterTest extends ErpFhirBuildingTest {

  @Test
  void shouldNotRequireAnyOptions() {
    val pp = new PatientParameter();
    val cmdline = new CommandLine(pp);
    assertDoesNotThrow(() -> cmdline.parseArgs());
    assertNotNull(pp.getFullName());
    assertNotNull(pp.getAssignerOrganization());
    assertNotNull(pp.getBirthDate());
    assertNotNull(pp.getKvnrParameter().getKvnr());
    assertNotNull(pp.createPatient());
    assertEquals(InsuranceTypeDe.GKV, pp.getKvnrParameter().getInsuranceType());
  }

  @Test
  void shouldAcceptPkv() {
    val pp = new PatientParameter();
    val cmdline = new CommandLine(pp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--insurance-type", "PKV"));
    assertEquals(InsuranceTypeDe.PKV, pp.getKvnrParameter().getInsuranceType());
    assertNotNull(pp.createPatient());
  }
}
