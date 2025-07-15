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

package de.gematik.test.erezept.cli.printer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvEvdgaBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ResourcePrinterTest extends ErpFhirParsingTest {

  // set to false if you want see the printer's output
  private static final boolean HIDE_OUTPUT = true;
  private PrintStream out;

  @BeforeEach
  void setUp() {
    // set System.out to ps to implicitly check the default output
    if (HIDE_OUTPUT) {
      val os = new ByteArrayOutputStream();
      this.out = new PrintStream(os);
      System.setOut(this.out);
    } else {
      this.out = System.out;
    }
  }

  @AfterEach
  void tearDown() {
    this.out.close();
  }

  @ParameterizedTest
  @MethodSource
  void shouldPrintWithoutThrowingExceptions(File input) {
    val content = ResourceLoader.readString(input);
    val bundle = parser.decode(ErxMedicationDispenseBundle.class, content);

    val rp = new ResourcePrinter();
    assertDoesNotThrow(() -> rp.print(bundle));
  }

  static Stream<Arguments> shouldPrintWithoutThrowingExceptions() {
    return ResourceLoader.getResourceDirectoryStructure(
            "fhir/valid/erp/1.4.0/medicationdispensebundle/")
        .stream()
        .map(Arguments::of);
  }

  @Test
  void shouldPrintHealthAppRequestWithoutThrowingExceptions() {
    val content = ResourceLoader.readFileFromResource("fhir/valid/kbv_evdga/1.1/EVDGA_Bundle.xml");
    val bundle = parser.decode(KbvEvdgaBundle.class, content);

    val har = bundle.getHealthAppRequest();
    val rp = new ResourcePrinter();
    assertDoesNotThrow(() -> rp.print(har));
  }

  @Test
  void shouldPrintKbvMedicationWithoutThrowingExceptions() {
    val content =
        ResourceLoader.readFileFromResource(
            "fhir/valid/kbv/1.1.0/bundle/1f339db0-9e55-4946-9dfa-f1b30953be9b.xml");
    val bundle = parser.decode(KbvErpBundle.class, content);

    val medication = bundle.getMedication();
    val rp = new ResourcePrinter();
    assertDoesNotThrow(() -> rp.print(medication));
  }
}
