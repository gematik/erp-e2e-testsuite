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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.BaseANR.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.stream.*;
import lombok.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import picocli.*;

class PractitionerParameterTest extends ErpFhirBuildingTest {

  @Test
  void shouldNotRequireAnyOptions() {
    val pp = new PractitionerParameter();
    val cmdline = new CommandLine(pp);
    assertDoesNotThrow(() -> cmdline.parseArgs());
    assertNotNull(pp.getFullName());
    assertNotNull(pp.getANR());
    assertNotNull(pp.getQualificationType());
    assertNotNull(pp.createPractitioner());
  }

  @Test
  void shouldGenerateLastName() {
    val pp = new PractitionerParameter();
    val cmdline = new CommandLine(pp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--doc", "Bernd"));
    val name = pp.getFullName();
    assertEquals("Bernd", name.getFirstName());
    assertNotNull(name.getLastName());
  }

  @Test
  void shouldGenerateNameOnEmptyString() {
    val pp = new PractitionerParameter();
    val cmdline = new CommandLine(pp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--doc", ""));
    val name = pp.getFullName();
    assertNotNull(name.getFirstName());
    assertNotNull(name.getLastName());
  }

  @ParameterizedTest(name = "Create Practitioner with QualificationType {0} expecting {1}")
  @MethodSource("qualificationTypes")
  void shouldGenerateWithGivenBaseAnr(QualificationType qt, ANRType expectedType) {
    val anrValue = "123456789";

    val pp = new PractitionerParameter();
    val cmdline = new CommandLine(pp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--anr", anrValue, "--qualification", qt.name()));

    val baseAnr = pp.getANR();
    assertEquals(anrValue, baseAnr.getValue());
    assertEquals(expectedType, baseAnr.getType());
  }

  static Stream<Arguments> qualificationTypes() {
    return Stream.of(
        arguments(QualificationType.DOCTOR, ANRType.LANR),
        arguments(QualificationType.DENTIST, ANRType.ZANR));
  }
}
