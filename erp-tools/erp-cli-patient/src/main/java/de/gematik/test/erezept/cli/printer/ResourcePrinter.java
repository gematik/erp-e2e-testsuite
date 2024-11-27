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

package de.gematik.test.erezept.cli.printer;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import java.io.PrintStream;
import java.util.List;

public class ResourcePrinter {

  private final PrintStream ps;

  @SuppressWarnings("java:S106") // by default print to System.out
  public ResourcePrinter() {
    this(System.out);
  }

  public ResourcePrinter(PrintStream ps) {
    this.ps = ps;
  }

  public void printMedicationDispenses(List<ErxMedicationDispense> mdList) {
    ps.println("\nMedicationDispense(s):");
    mdList.forEach(this::print);
  }

  public void print(ErxMedicationDispense md) {
    ps.println(
        format(
            "Abgabe durch: {0} am {1}", md.getPerformerIdFirstRep(), md.getZonedWhenHandedOver()));
    md.getErpMedication()
        .forEach(med -> ps.println(format("\tMedikament: {0}", med.getDescription())));
  }
}
