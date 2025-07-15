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

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Parameters;

@Slf4j
public class GemDispenseCloseOperationPharmaceuticalsBuilder<P extends Parameters>
    extends ResourceBuilder<P, GemDispenseCloseOperationPharmaceuticalsBuilder<P>> {

  private final ErpWorkflowStructDef structureDefinition;
  private final Supplier<P> constructor;
  private final boolean requiresParameters;

  private final List<Pair<ErxMedicationDispense, GemErpMedication>> pharmaceuticalDispensations =
      new LinkedList<>();
  private ErpWorkflowVersion version = ErpWorkflowVersion.getDefaultVersion();

  protected GemDispenseCloseOperationPharmaceuticalsBuilder(
      ErpWorkflowStructDef structureDefinition,
      Supplier<P> constructor,
      boolean requiresParameters) {
    this.structureDefinition = structureDefinition;
    this.constructor = constructor;
    this.requiresParameters = requiresParameters;
  }

  public GemDispenseCloseOperationPharmaceuticalsBuilder<P> version(ErpWorkflowVersion version) {
    this.version = version;
    return this;
  }

  public GemDispenseCloseOperationPharmaceuticalsBuilder<P> with(
      ErxMedicationDispense medicationDispense, GemErpMedication medication) {
    this.pharmaceuticalDispensations.add(Pair.of(medicationDispense, medication));
    return this;
  }

  @Override
  public P build() {
    checkRequired();
    val parameters = this.createResource(constructor, structureDefinition, version);

    this.pharmaceuticalDispensations.forEach(
        disp -> {
          val rxDispensation = parameters.addParameter().setName("rxDispensation");
          val md = disp.getLeft();
          val medication = disp.getRight();

          rxDispensation.addPart().setName("medicationDispense").setResource(md);
          rxDispensation.addPart().setName("medication").setResource(medication);
        });

    return parameters;
  }

  private void checkRequired() {
    if (requiresParameters) {
      checkRequiredList(
          this.pharmaceuticalDispensations,
          1,
          "At least one pair of MedicationDispense and Medication is required for dispensing"
              + " pharmaceuticals");
    }
  }
}
