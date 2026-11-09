/*
 * Copyright (Change Date see Readme), gematik GmbH
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

import de.gematik.test.erezept.fhir.profiles.definitions.PatientenrechnungStructDef;
import de.gematik.test.erezept.fhir.profiles.version.PatientenrechnungVersion;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Parameters;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class MarkingFlag {

  private static final String INSURANCE_PROVIDER = "insuranceProvider";
  private static final String SUBSIDY = "subsidy";
  private static final String TAX_OFFICE = "taxOffice";

  private final boolean insuranceProvider;
  private final boolean subsidyFlag;
  private final boolean taxOffice;

  public static MarkingFlag with(
      boolean insuranceProvider, boolean subsidyFlag, boolean taxOffice) {
    return new MarkingFlag(insuranceProvider, subsidyFlag, taxOffice);
  }

  public Extension asExtension() {
    val markingFlag = PatientenrechnungStructDef.MARKING_FLAG.asExtension();
    val extInsurance = new Extension(INSURANCE_PROVIDER, new BooleanType(insuranceProvider));
    val extSubsidy = new Extension(SUBSIDY, new BooleanType(subsidyFlag));
    val extTaxOffice = new Extension(TAX_OFFICE, new BooleanType(taxOffice));
    markingFlag.addExtension(extInsurance).addExtension(extSubsidy).addExtension(extTaxOffice);
    return markingFlag;
  }

  public Parameters asParameters() {
    return this.asParameters(PatientenrechnungVersion.getDefaultVersion());
  }

  public Parameters asParameters(PatientenrechnungVersion version) {
    val parameters = new Parameters();
    val meta = new Meta();
    val profile = PatientenrechnungStructDef.PATCH_CHARGEITEM_INPUT.asCanonicalType(version);

    meta.setProfile(List.of(profile));
    parameters.setMeta(meta);

    val markingFlag = parameters.addParameter();
    markingFlag.setName("markingFlag");
    markingFlag.addPart().setName(INSURANCE_PROVIDER).setValue(new BooleanType(insuranceProvider));
    markingFlag.addPart().setName(SUBSIDY).setValue(new BooleanType(subsidyFlag));
    markingFlag.addPart().setName(TAX_OFFICE).setValue(new BooleanType(taxOffice));

    return parameters;
  }
}
