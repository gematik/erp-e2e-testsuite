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

import java.util.stream.*;
import lombok.*;
import org.junit.jupiter.api.*;
import picocli.*;

class MedicationRequestParameterTest {

  @Test
  void shouldNotRequireAnyOptions() {
    val mrp = new MedicationRequestParameter();
    val cmdline = new CommandLine(mrp);
    assertDoesNotThrow(() -> cmdline.parseArgs());

    assertFalse(mrp.isEmergencyServiceFee());
    assertFalse(mrp.isMvoOnly());
    assertFalse(mrp.isSubstitution());

    assertNotNull(mrp.getPatient());
    assertNotNull(mrp.getInsurance());
    assertNotNull(mrp.getPractitioner());
    assertNotNull(mrp.getMedication());
    assertNotNull(mrp.getDosage());
    assertNotNull(mrp.getPackagesQuantity());
    assertNotNull(mrp.createMedicationRequest());
  }

  @Test
  void shouldCreateMvoOnly() {
    val mrp = new MedicationRequestParameter();
    val cmdline = new CommandLine(mrp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--mvo"));

    IntStream.range(0, 5)
        .forEach(
            idx -> {
              val mr = mrp.createMedicationRequest();
              assertNotNull(mr);
              assertTrue(mr.isMultiple());
            });
  }

  @Test
  void shouldCreateAutIdem() {
    val mrp = new MedicationRequestParameter();
    val cmdline = new CommandLine(mrp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--aut-idem"));

    IntStream.range(0, 5)
        .forEach(
            idx -> {
              val mr = mrp.createMedicationRequest();
              assertNotNull(mr);
              assertTrue(mr.allowSubstitution());
            });
  }
}
