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

package de.gematik.test.erezept.fhir.extensions.erp;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.profiles.definitions.PatientenrechnungStructDef;
import de.gematik.test.erezept.fhir.profiles.version.PatientenrechnungVersion;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class MarkingFlag {

  private static final String INSURANCE_PROVIDER = "insuranceProvider";
  private static final String SUBSIDY = "subsidy";
  private static final String TAX_OFFICE = "taxOffice";

  private final boolean insuranceProvider;
  private final boolean subsidy;
  private final boolean taxOffice;

  public static MarkingFlag with(boolean insuranceProvider, boolean subsidy, boolean taxOffice) {
    return new MarkingFlag(insuranceProvider, subsidy, taxOffice);
  }

  public Extension asExtension() {
    val markingFlag = PatientenrechnungStructDef.MARKING_FLAG.asExtension();
    val extInsurance = new Extension(INSURANCE_PROVIDER, new BooleanType(insuranceProvider));
    val extSubsidy = new Extension(SUBSIDY, new BooleanType(subsidy));
    val extTaxOffice = new Extension(TAX_OFFICE, new BooleanType(taxOffice));
    markingFlag.addExtension(extInsurance).addExtension(extSubsidy).addExtension(extTaxOffice);
    return markingFlag;
  }

  public Parameters asParameters() {
    return this.asParameters(PatientenrechnungVersion.getDefaultVersion());
  }

  public Parameters asParameters(PatientenrechnungVersion version) {
    val parameters = new Parameters();
    if (version == PatientenrechnungVersion.V1_0_0) {
      this.addParam(parameters, INSURANCE_PROVIDER, this.insuranceProvider);
      this.addParam(parameters, SUBSIDY, this.subsidy);
      this.addParam(parameters, TAX_OFFICE, this.taxOffice);
    } else {
      val meta = new Meta();
      val profile = PatientenrechnungStructDef.PATCH_CHARGEITEM_INPUT.asCanonicalType(version);

      meta.setProfile(List.of(profile));
      parameters.setMeta(meta);

      val markingFlag = parameters.addParameter();
      markingFlag.setName("markingFlag");
      markingFlag
          .addPart()
          .setName(INSURANCE_PROVIDER)
          .setValue(new BooleanType(insuranceProvider));
      markingFlag.addPart().setName(SUBSIDY).setValue(new BooleanType(subsidy));
      markingFlag.addPart().setName(TAX_OFFICE).setValue(new BooleanType(taxOffice));
    }
    return parameters;
  }

  private void addParam(Parameters parameters, String name, boolean value) {
    val structureDefinitionUrl = PatientenrechnungStructDef.MARKING_FLAG.getCanonicalUrl();
    val operation = parameters.addParameter().setName("operation");
    operation.addPart().setName("type").setValue(new CodeType("add"));
    operation
        .addPart()
        .setName("path")
        .setValue(
            new StringType(
                format(
                    "ChargeItem.extension(''{0}'').extension(''{1}'')",
                    structureDefinitionUrl, name)));

    operation.addPart().setName("name").setValue(new StringType("valueBoolean"));
    operation.addPart().setName("value").setValue(new BooleanType(value));
  }
}
