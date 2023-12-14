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

package de.gematik.test.erezept.fhir.extensions.erp;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.parser.profiles.IStructureDefinition;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.PatientenrechnungStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.PatientenrechnungVersion;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class MarkingFlag {

  private final boolean insuranceProvider;
  private final boolean subsidy;
  private final boolean taxOffice;

  public static MarkingFlag with(boolean insuranceProvider, boolean subsidy, boolean taxOffice) {
    return new MarkingFlag(insuranceProvider, subsidy, taxOffice);
  }

  public Extension asExtension() {
    return asExtension(ErpWorkflowStructDef.MARKING_FLAG);
  }

  public Extension asExtension(
      PatientenrechnungVersion version) { // NOSONAR version for now only required for overloading
    return asExtension(PatientenrechnungStructDef.MARKING_FLAG);
  }

  public Parameters asParameters() {
    return asParameters(ErpWorkflowStructDef.MARKING_FLAG);
  }

  public Parameters asParameters(
      PatientenrechnungVersion version) { // NOSONAR version for now only required for overloading
    return asParameters(PatientenrechnungStructDef.MARKING_FLAG);
  }

  private Extension asExtension(IStructureDefinition<?> structureDefinition) {
    val markingFlag = new Extension(structureDefinition.getCanonicalUrl());
    val extInsurance = new Extension("insuranceProvider", new BooleanType(insuranceProvider));
    val extSubsidy = new Extension("subsidy", new BooleanType(subsidy));
    val extTaxOffice = new Extension("taxOffice", new BooleanType(taxOffice));
    markingFlag.addExtension(extInsurance).addExtension(extSubsidy).addExtension(extTaxOffice);
    return markingFlag;
  }

  private Parameters asParameters(IStructureDefinition<?> structureDefinition) {
    val parameters = new Parameters();

    this.addParam(parameters, structureDefinition, "insuranceProvider", this.insuranceProvider);
    this.addParam(parameters, structureDefinition, "subsidy", this.subsidy);
    this.addParam(parameters, structureDefinition, "taxOffice", this.taxOffice);

    return parameters;
  }

  private void addParam(
      Parameters parameters,
      IStructureDefinition<?> structureDefinition,
      String name,
      boolean value) {
    val operation = parameters.addParameter().setName("operation");
    operation.addPart().setName("type").setValue(new CodeType("add"));
    operation
        .addPart()
        .setName("path")
        .setValue(
            new StringType(
                format(
                    "ChargeItem.extension(''{0}'').extension(''{1}'')",
                    structureDefinition.getCanonicalUrl(), name)));

    operation.addPart().setName("name").setValue(new StringType("valueBoolean"));
    operation.addPart().setName("value").setValue(new BooleanType(value));
  }
}
