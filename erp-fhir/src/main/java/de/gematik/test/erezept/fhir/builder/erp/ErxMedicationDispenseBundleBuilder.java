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

package de.gematik.test.erezept.fhir.builder.erp;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import java.util.ArrayList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumeration;

public class ErxMedicationDispenseBundleBuilder
    extends ResourceBuilder<Bundle, ErxMedicationDispenseBundleBuilder> {

  private ErpWorkflowVersion erpWorkflowVersion = ErpWorkflowVersion.getDefaultVersion();
  private final List<ErxMedicationDispense> medicationDispenses;

  private ErxMedicationDispenseBundleBuilder() {
    this(new ArrayList<>());
  }

  private ErxMedicationDispenseBundleBuilder(List<ErxMedicationDispense> medicationDispenses) {
    this.medicationDispenses = medicationDispenses;
  }

  public static ErxMedicationDispenseBundleBuilder empty() {
    return new ErxMedicationDispenseBundleBuilder();
  }

  public static ErxMedicationDispenseBundleBuilder of(
      List<ErxMedicationDispense> medicationDispenses) {
    return new ErxMedicationDispenseBundleBuilder(medicationDispenses);
  }

  public ErxMedicationDispenseBundleBuilder version(ErpWorkflowVersion version) {
    this.erpWorkflowVersion = version;
    return this;
  }

  public ErxMedicationDispenseBundleBuilder add(ErxMedicationDispense medicationDispense) {
    this.medicationDispenses.add(medicationDispense);
    return self();
  }

  @Override
  public Bundle build() {
    val bundleType =
        new Enumeration<>(new Bundle.BundleTypeEnumFactory(), Bundle.BundleType.COLLECTION);
    val mdBundle = new Bundle(bundleType);

    if (erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_1_1) > 0
        && erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_4_0) < 0) {
      mdBundle
          .getMeta()
          .addProfile(
              ErpWorkflowStructDef.CLOSE_OPERATION_BUNDLE.getVersionedUrl(erpWorkflowVersion));

      // Note: HAPI seems to skip the tags on serialization, why?
      mdBundle
          .getMeta()
          .addTag()
          .setDisplay(
              "MedicationDispense Bundle for $close-Operation on dispensation of multiple"
                  + " medications");
    }

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
