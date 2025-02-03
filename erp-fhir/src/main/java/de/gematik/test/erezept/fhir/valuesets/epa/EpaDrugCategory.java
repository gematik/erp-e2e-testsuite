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

package de.gematik.test.erezept.fhir.valuesets.epa;

import de.gematik.test.erezept.eml.fhir.parser.profiles.EpaMedStructDef;
import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.parser.profiles.systems.EpaMedicationCodeSystem;
import de.gematik.test.erezept.fhir.valuesets.IValueSet;
import java.util.Arrays;
import lombok.Getter;
import org.hl7.fhir.r4.model.Extension;

@Getter
public enum EpaDrugCategory implements IValueSet {
  C_00(
      "00", "Arzneimittel oder in die Arzneimittelversorgung nach § 31 SGB V einbezogenes Produkt"),
  C_01("01", "BtM"),
  C_02("02", "AMVV § 3a Abs. 1 (Thalidomid o. ä.)"),
  C_03("03", "Sonstiges");

  public static final EpaMedicationCodeSystem CODE_SYSTEM = EpaMedicationCodeSystem.DRUG_CATEGORY;

  private final String code;
  private final String display;
  private final String definition;

  EpaDrugCategory(String code, String display) {
    this(code, display, "N/A definition in profile");
  }

  EpaDrugCategory(String code, String display, String definition) {
    this.code = code;
    this.display = display;
    this.definition = definition;
  }

  @Override
  public EpaMedicationCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public Extension asExtension() {
    return new Extension(EpaMedStructDef.DRUG_CATEGORY_EXT.getCanonicalUrl(), this.asCoding());
  }

  public static EpaDrugCategory fromCode(String coding) {
    return Arrays.stream(EpaDrugCategory.values())
        .filter(mc -> mc.code.equals(coding))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(EpaDrugCategory.class, coding));
  }
}
