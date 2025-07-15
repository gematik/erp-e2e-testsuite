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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.cli.cmd.generate.param;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.builder.kbv.KbvPatientFaker;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

class InsuranceCoverageParameterTest extends ErpFhirBuildingTest {

  @Test
  void shouldNotRequireAnyOptions() {
    val icp = new InsuranceCoverageParameter();
    val cmdline = new CommandLine(icp);
    assertDoesNotThrow(() -> cmdline.parseArgs());

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

    val patient = KbvPatientFaker.builder().fake();
    icp.setPatient(patient);
    val coverage = icp.createCoverage();
    // only possible to check if insurance kind is the same as we don't have a getter for patient
    // reference
    assertEquals(patient.getInsuranceType(), coverage.getInsuranceKind());
  }

  @Test
  void shouldCreateWithRealCoverageFromIknr() {
    val icp = new InsuranceCoverageParameter();
    val cmdline = new CommandLine(icp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--iknr", "104127692"));

    val coverage = icp.createCoverage();
    assertEquals("104127692", coverage.getIknrOrThrow().getValue());
    assertEquals("actimonda krankenkasse", coverage.getName());
  }

  @Test
  void shouldCreateWithRealCoverageFromIknrWithType() {
    val icp = new InsuranceCoverageParameter();
    val cmdline = new CommandLine(icp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--iknr", "950585030", "--coverage-type", "PKV"));

    val coverage = icp.createCoverage();
    assertEquals("950585030", coverage.getIknrOrThrow().getValue());
    assertEquals("PBeaKK Postbeamtenkrankenkasse", coverage.getName());
    assertEquals(InsuranceTypeDe.PKV, coverage.getInsuranceKind());
  }

  @Test
  void shouldCreateWithRealCoverageFromIknrWitCustomName() {
    val icp = new InsuranceCoverageParameter();
    val cmdline = new CommandLine(icp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--iknr", "104127692", "--insurance-name", "ABC"));

    val coverage = icp.createCoverage();
    assertEquals("104127692", coverage.getIknrOrThrow().getValue());
    assertEquals("ABC", coverage.getName());
  }

  @Test
  void shouldCreateWithRealCoverageFromTypeWitCustomName() {
    val icp = new InsuranceCoverageParameter();
    val cmdline = new CommandLine(icp);
    assertDoesNotThrow(
        () -> cmdline.parseArgs("--coverage-type", "PKV", "--insurance-name", "ABC"));

    val coverage = icp.createCoverage();
    assertNotNull(coverage.getIknrOrThrow());
    assertEquals("ABC", coverage.getName());
    assertEquals(InsuranceTypeDe.PKV, coverage.getInsuranceKind());
  }

  @ParameterizedTest(name = "Create random Coverage of Type {0}")
  @ValueSource(strings = {"GKV", "PKV", "BG"})
  void shouldCreateRealCoverageFromType(String type) {
    val icp = new InsuranceCoverageParameter();
    val cmdline = new CommandLine(icp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--coverage-type", type));

    val coverage = icp.createCoverage();
    assertNotNull(coverage.getIknrOrThrow().getValue());
    assertNotNull(coverage.getName());
    assertEquals(InsuranceTypeDe.fromCode(type), coverage.getInsuranceKind());
  }

  @Test
  void shouldCreateWithRealCoverageFromIknrEvenOnDifferentInsuranceKind() {
    val icp = new InsuranceCoverageParameter();
    val cmdline = new CommandLine(icp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--iknr", "104127692"));

    val patient =
        KbvPatientFaker.builder()
            .withKvnrAndInsuranceType(KVNR.random(), InsuranceTypeDe.PKV)
            .fake();
    icp.setPatient(patient);
    val coverage = icp.createCoverage();
    assertEquals("104127692", coverage.getIknrOrThrow().getValue());
    // should not equal because patient is PKV though actimondia is a GKV insurance: -> random
    // default name
    assertEquals("actimonda krankenkasse", coverage.getName());
  }
}
