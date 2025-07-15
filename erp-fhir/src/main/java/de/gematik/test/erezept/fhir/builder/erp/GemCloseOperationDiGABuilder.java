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
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseDiGA;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Parameters;

@Slf4j
public class GemCloseOperationDiGABuilder<P extends Parameters>
    extends ResourceBuilder<P, GemCloseOperationDiGABuilder<P>> {

  private final ErpWorkflowStructDef structureDefinition;
  private final Supplier<P> constructor;

  private final List<ErxMedicationDispenseDiGA> dispensations = new LinkedList<>();
  private ErpWorkflowVersion version = ErpWorkflowVersion.getDefaultVersion();

  protected GemCloseOperationDiGABuilder(
      ErpWorkflowStructDef structureDefinition, Supplier<P> constructor) {
    this.structureDefinition = structureDefinition;
    this.constructor = constructor;
  }

  public GemCloseOperationDiGABuilder<P> version(ErpWorkflowVersion version) {
    this.version = version;
    return this;
  }

  public GemCloseOperationDiGABuilder<P> with(ErxMedicationDispenseDiGA medicationDispense) {
    this.dispensations.add(medicationDispense);
    return this;
  }

  @Override
  public P build() {
    val parameters = this.createResource(constructor, structureDefinition, version);

    this.dispensations.forEach(
        disp -> {
          val rxDispensation = parameters.addParameter().setName("rxDispensation");
          rxDispensation.addPart().setName("medicationDispense").setResource(disp);
        });

    return parameters;
  }
}
