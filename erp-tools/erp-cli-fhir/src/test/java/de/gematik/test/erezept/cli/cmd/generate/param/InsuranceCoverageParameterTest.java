/*
 * Copyright 2024 gematik GmbH
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

import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import lombok.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.*;

class InsuranceCoverageParameterTest {

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

    val patient = PatientFaker.builder().fake();
    icp.setPatient(patient);
    val coverage = icp.createCoverage();
    // only possible to check if insurance kind is the same as we don't have a getter for patient
    // reference
    assertEquals(patient.getInsuranceKind(), coverage.getInsuranceKind());
  }

  @Test
  void shouldCreateWithRealCoverageFromIknr() {
    val icp = new InsuranceCoverageParameter();
    val cmdline = new CommandLine(icp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--iknr", "104127692"));

    val coverage = icp.createCoverage();
    assertEquals("104127692", coverage.getIknr().getValue());
    assertEquals("actimonda krankenkasse", coverage.getName());
  }

  @Test
  void shouldCreateWithRealCoverageFromIknrWithType() {
    val icp = new InsuranceCoverageParameter();
    val cmdline = new CommandLine(icp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--iknr", "950585030", "--coverage-type", "PKV"));

    val coverage = icp.createCoverage();
    assertEquals("950585030", coverage.getIknr().getValue());
    assertEquals("PBeaKK Postbeamtenkrankenkasse", coverage.getName());
    assertEquals(VersicherungsArtDeBasis.PKV, coverage.getInsuranceKind());
  }

  @Test
  void shouldCreateWithRealCoverageFromIknrWitCustomName() {
    val icp = new InsuranceCoverageParameter();
    val cmdline = new CommandLine(icp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--iknr", "104127692", "--insurance-name", "ABC"));

    val coverage = icp.createCoverage();
    assertEquals("104127692", coverage.getIknr().getValue());
    assertEquals("ABC", coverage.getName());
  }

  @Test
  void shouldCreateWithRealCoverageFromTypeWitCustomName() {
    val icp = new InsuranceCoverageParameter();
    val cmdline = new CommandLine(icp);
    assertDoesNotThrow(
        () -> cmdline.parseArgs("--coverage-type", "PKV", "--insurance-name", "ABC"));

    val coverage = icp.createCoverage();
    assertNotNull(coverage.getIknr());
    assertEquals("ABC", coverage.getName());
    assertEquals(VersicherungsArtDeBasis.PKV, coverage.getInsuranceKind());
  }

  @ParameterizedTest(name = "Create random Coverage of Type {0}")
  @ValueSource(strings = {"GKV", "PKV", "BG"})
  void shouldCreateRealCoverageFromType(String type) {
    val icp = new InsuranceCoverageParameter();
    val cmdline = new CommandLine(icp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--coverage-type", type));

    val coverage = icp.createCoverage();
    assertNotNull(coverage.getIknr().getValue());
    assertNotNull(coverage.getName());
    assertEquals(VersicherungsArtDeBasis.fromCode(type), coverage.getInsuranceKind());
  }

  @Test
  void shouldCreateWithRealCoverageFromIknrEvenOnDifferentInsuranceKind() {
    val icp = new InsuranceCoverageParameter();
    val cmdline = new CommandLine(icp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--iknr", "104127692"));

    val patient =
        PatientFaker.builder()
            .withKvnrAndInsuranceType(KVNR.random(), VersicherungsArtDeBasis.PKV)
            .fake();
    icp.setPatient(patient);
    val coverage = icp.createCoverage();
    assertEquals("104127692", coverage.getIknr().getValue());
    // should not equal because patient is PKV though actimondia is a GKV insurance: -> random
    // default name
    assertEquals("actimonda krankenkasse", coverage.getName());
  }
}
