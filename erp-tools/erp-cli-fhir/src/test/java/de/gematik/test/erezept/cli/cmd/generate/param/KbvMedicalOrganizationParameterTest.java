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
import lombok.*;
import org.junit.jupiter.api.*;
import picocli.*;

class KbvMedicalOrganizationParameterTest extends ErpFhirBuildingTest {

  @Test
  void shouldNotRequireAnyOptions() {
    val mop = new MedicalOrganizationParameter();
    val cmdline = new CommandLine(mop);
    assertDoesNotThrow(() -> cmdline.parseArgs());

    assertNotNull(mop.getOrganizationName());
    assertNotNull(mop.getBsnr());
    assertNotNull(mop.createMedicalOrganization());

    assertFalse(mop.hasCLIOrganizationName());
  }

  @Test
  void shouldChangeOrganizationName() {
    val mop = new MedicalOrganizationParameter();
    val cmdline = new CommandLine(mop);
    assertDoesNotThrow(() -> cmdline.parseArgs());

    val defaultName = mop.getOrganizationName();
    mop.changeOrganizationName("Test Organization");
    assertFalse(mop.getOrganizationName().equalsIgnoreCase(defaultName));
  }
}
