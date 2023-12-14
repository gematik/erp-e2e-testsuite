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

package de.gematik.test.erezept.cli.cmd.generate.param;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.valuesets.*;
import lombok.*;
import org.junit.jupiter.api.*;
import picocli.*;

class MedicationParameterTest {

  @Test
  void shouldNotRequireAnyOptions() {
    val mp = new MedicationParameter();
    val cmdline = new CommandLine(mp);
    assertDoesNotThrow(() -> cmdline.parseArgs());

    assertFalse(mp.getIsVaccine());
    assertEquals(MedicationCategory.C_00, mp.getCategory());

    assertNotNull(mp.getStandardSize());
    assertNotNull(mp.getSupplyForm());
    assertNotNull(mp.getQuantity());
    assertNotNull(mp.getPzn());
    assertNotNull(mp.getDrugName());
    assertNotNull(mp.createMedication());
  }
}
