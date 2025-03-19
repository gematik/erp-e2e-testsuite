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

package de.gematik.test.erezept.fhir.valuesets;

import de.gematik.bbriccs.fhir.coding.FromValueSet;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvBasisStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.CommonCodeSystem;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;

@Getter
@RequiredArgsConstructor
public enum BaseMedicationType implements FromValueSet {
  MEDICAL_PRODUCT("763158003", "Medicinal product (product)"),
  PHARM_BIO_PRODUCT(
      "373873005:860781008=362943005",
      "Pharmaceutical / biologic product (product) : Has product characteristic (attribute) ="
          + " Manual method (qualifier value)");

  public static final CommonCodeSystem CODE_SYSTEM = CommonCodeSystem.SNOMED_SCT;
  public static final String VERSION = "http://snomed.info/sct/900000000000207008/version/20220331";
  public static final String DESCRIPTION = "The Scope of a Consent";

  private final String code;
  private final String display;

  @Override
  public CommonCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  @Override
  public CodeableConcept asCodeableConcept() {
    val coding = asCoding(true);
    coding.setVersion(VERSION);
    return new CodeableConcept().setCoding(List.of(coding));
  }

  public Extension asExtension() {
    return new Extension(
        KbvBasisStructDef.BASE_MEDICATION_TYPE.getCanonicalUrl(), this.asCodeableConcept());
  }
}
