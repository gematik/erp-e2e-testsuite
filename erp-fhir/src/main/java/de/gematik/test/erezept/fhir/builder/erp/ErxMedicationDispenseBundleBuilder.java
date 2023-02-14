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

package de.gematik.test.erezept.fhir.builder.erp;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumeration;

public class ErxMedicationDispenseBundleBuilder
    extends AbstractResourceBuilder<ErxMedicationDispenseBundleBuilder> {

  private final List<ErxMedicationDispense> medicationDispenses;

  private ErxMedicationDispenseBundleBuilder() {
    this(new ArrayList<>());
  }

  private ErxMedicationDispenseBundleBuilder(List<ErxMedicationDispense> medicationDispenses) {
    this.medicationDispenses = medicationDispenses;
  }

  public static ErxMedicationDispenseBundleBuilder faker(
      int amount, String kvid, String performerId, PrescriptionId prescriptionId) {
    val medicationDispenses = new ArrayList<ErxMedicationDispense>();
    IntStream.range(0, amount)
        .forEach(
            idx ->
                medicationDispenses.add(
                    ErxMedicationDispenseBuilder.faker(kvid, performerId, prescriptionId).build()));
    return of(medicationDispenses);
  }

  public static ErxMedicationDispenseBundleBuilder empty() {
    return new ErxMedicationDispenseBundleBuilder();
  }

  public static ErxMedicationDispenseBundleBuilder of(
      List<ErxMedicationDispense> medicationDispenses) {
    return new ErxMedicationDispenseBundleBuilder(medicationDispenses);
  }

  public ErxMedicationDispenseBundleBuilder add(ErxMedicationDispense medicationDispense) {
    this.medicationDispenses.add(medicationDispense);
    return self();
  }

  public Bundle build() {
    val bundleType =
        new Enumeration<>(new Bundle.BundleTypeEnumFactory(), Bundle.BundleType.COLLECTION);
    val mdBundle = new Bundle(bundleType);

    this.medicationDispenses.forEach(
        md -> {
          val entry = new Bundle.BundleEntryComponent();
          entry.setFullUrl(format("MedicationDispense/{0}", md.getId()));
          entry.setResource(md);
          mdBundle.addEntry(entry);
        });

    return mdBundle;
  }
}
