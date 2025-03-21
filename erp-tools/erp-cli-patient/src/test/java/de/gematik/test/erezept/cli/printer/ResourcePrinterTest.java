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

package de.gematik.test.erezept.cli.printer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.common.base.Strings;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseFaker;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.stream.IntStream;
import lombok.val;
import org.junit.jupiter.api.Test;

class ResourcePrinterTest extends ErpFhirParsingTest {

  @Test
  void shouldPrintMedicationDispensesWithoutThrowingExceptions() throws IOException {
    val meddisps =
        IntStream.range(0, 2).mapToObj(i -> ErxMedicationDispenseFaker.builder().fake()).toList();
    try (val os = new ByteArrayOutputStream();
        val ps = new PrintStream(os)) {
      // set System.out to ps to implicitly check the default output
      System.setOut(ps);

      val rp = new ResourcePrinter();
      assertDoesNotThrow(() -> rp.printMedicationDispenses(meddisps));
      os.flush();
      assertFalse(Strings.isNullOrEmpty(os.toString()));
    }
  }
}
