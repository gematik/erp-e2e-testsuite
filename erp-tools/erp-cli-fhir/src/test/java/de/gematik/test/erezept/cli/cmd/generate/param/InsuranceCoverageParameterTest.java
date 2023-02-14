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

package de.gematik.test.erezept.cli.cmd.generate.param;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.builder.kbv.*;
import lombok.*;
import org.junit.jupiter.api.*;
import picocli.*;

class InsuranceCoverageParameterTest {

  @Test
  void shouldNotRequireAnyOptions() {
    val icp = new InsuranceCoverageParameter();
    val cmdline = new CommandLine(icp);
    assertDoesNotThrow(() -> cmdline.parseArgs());

    assertNotNull(icp.getIknr());
    assertNotNull(icp.getInsuranceName());
    assertNotNull(icp.getPersonGroup());
    assertNotNull(icp.getDmp());
    assertNotNull(icp.getWop());
    assertNotNull(icp.getStatus());
    assertNotNull(icp.createCoverage());
  }

  @Test
  void shouldCreateWithProvidedPatient() {
    val icp = new InsuranceCoverageParameter();
    val cmdline = new CommandLine(icp);
    assertDoesNotThrow(() -> cmdline.parseArgs());

    val patient = PatientBuilder.faker().build();
    icp.setPatient(patient);
    val coverage = icp.createCoverage();
    // only possible to check if insurance kind is the same as we don't have a getter for patient
    // reference
    assertEquals(patient.getInsuranceKind(), coverage.getInsuranceKind());
  }
}
