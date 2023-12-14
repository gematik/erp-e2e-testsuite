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

package de.gematik.test.erezept.fhir.valuesets;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.NonNull;
import org.hl7.fhir.r4.model.Extension;

/**
 * <br>
 * <b>Profile:</b> kbv.ita.erp (1.0.1) <br>
 * <b>File:</b> KBV_VS_ERP_Medication_Category.json <br>
 * <br>
 * <b>Publisher:</b> Kassenärztliche Bundesvereinigung <br>
 * <b>Published:</b> 2021-02-23 <br>
 * <b>Status:</b> active
 */
@Getter
public enum MedicationCategory implements IValueSet {
  C_00(
      "00", "Arzneimittel oder in die Arzneimittelversorgung nach § 31 SGB V einbezogenes Produkt"),
  C_01("01", "BtM"),
  C_02("02", "AMVV § 3a Abs. 1 (Thalidomid o. ä.)"),
  ;

  public static final KbvCodeSystem CODE_SYSTEM = KbvCodeSystem.MEDICATION_CATEGORY;
  public static final String VERSION = "1.0.1";
  public static final String DESCRIPTION = "NO DESCRIPTION";
  public static final String PUBLISHER = "Kassenärztliche Bundesvereinigung";

  private final String code;
  private final String display;
  private final String definition;

  MedicationCategory(String code, String display) {
    this(code, display, "N/A definition in profile");
  }

  MedicationCategory(String code, String display, String definition) {
    this.code = code;
    this.display = display;
    this.definition = definition;
  }

  @Override
  public KbvCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public Extension asExtension() {
    return new Extension(KbvItaErpStructDef.MEDICATION_CATEGORY.getCanonicalUrl(), this.asCoding());
  }

  public static MedicationCategory fromCode(@NonNull String coding) {
    return Arrays.stream(MedicationCategory.values())
        .filter(mc -> mc.code.equals(coding))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(MedicationCategory.class, coding));
  }
}
