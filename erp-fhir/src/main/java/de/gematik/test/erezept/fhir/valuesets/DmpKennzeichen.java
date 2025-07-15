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

package de.gematik.test.erezept.fhir.valuesets;

import de.gematik.bbriccs.fhir.coding.FromValueSet;
import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.KbvCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Extension;

/**
 * <a href="https://applications.kbv.de/S_KBV_DMP.xhtml">S_KBV_DMP</a> <a
 * href="https://www.kbv.de/html/dmp.php">Disease-Management-Programme</a>
 *
 * <p>DMP-Kennzeichen: gibt an, in welchen DMPs ein Versicherter eingeschrieben ist (§ 267 Abs. 2
 * Satz 4 SGB V). Die Angabe ist auf der EGK vorhanden und auf der KVK Teil des Feldes:
 * Statusergänzung.
 */
@Getter
@RequiredArgsConstructor
public enum DmpKennzeichen implements FromValueSet {
  NOT_SET("00", "Nicht gesetzt"),
  DM2("01", "DM2"),
  BRK("02", "BRK"),
  KHK("03", "KHK"),
  DM1("04", "DM1"),
  ASTHMA("05", "Asthma"),
  COPD("06", "COPD"),
  HI("07", "HI"),
  DEPRESSION("08", "Depression"),
  RUECKENSCHMERZ("09", "Rückenschmerz"),
  RHEUMA("10", "Rheuma"),
  OSTEOPOROSE("11", "Osteoporose"),
  ADIPOSITAS("12", "Adipositas"),
  DIABETES_TYP_2_UND_KHK("30", "Diabetes Typ 2 und KHK"),
  ASTHMA_UND_DIABETES_TYP_2("31", "Asthma und Diabetes Typ 2"),
  COPD_UND_DIABETES_TYP_2("32", "COPD und Diabetes Typ 2"),
  COPD_UND_KHK("33", "COPD und KHK"),
  COPD_UND_DIABETES_TYP_2_UND_KHK("34", "COPD, Diabetes Typ 2 und KHK"),
  ASTHMA_UND_KHK("35", "Asthma und KHK"),
  ASTHMA_UND_DIABETES_TYP_2_UND_KHK("36", "Asthma, Diabetes Typ 2 und KHK"),
  BRUSTKREBS_UND_DIABETES_TYP_2("37", "Brustkrebs und Diabetes Typ 2"),
  DIABETES_TYP_1_UND_KHK("38", "Diabetes Typ 1 und KHK"),
  ASTHMA_UND_DIABETES_TYP_1("39", "Asthma und Diabetes Typ 1"),
  ASTHMA_UND_BRUSTKREBS("40", "Asthma und Brustkrebs"),
  BRUSTKREBS_UND_KHK("41", "Brustkrebs und KHK"),
  BRUSTKREBS_UND_COPD("42", "Brustkrebs und COPD"),
  COPD_UND_DIABETES_TYP_1("43", "COPD und Diabetes Typ 1"),
  BRUSTKREBS_UND_DIABETES_TYP_2_UND_KHK("44", "Brustkrebs, Diabetes Typ 2 und KHK"),
  ASTHMA_UND_BRUSTKREBS_UND_DIABETES_TYP_2("45", "Asthma, Brustkrebs und Diabetes Typ 2"),
  BRUSTKREBS_UND_DIABETES_TYP_1("46", "Brustkrebs und Diabetes Typ 1"),
  COPD_UND_DIABETES_TYP_1_UND_KHK("47", "COPD, Diabetes Typ 1 und KHK"),
  BRUSTKREBS_UND_COPD_UND_DIABETES_TYP_2("48", "Brustkrebs, COPD und Diabetes Typ 2"),
  ASTHMA_UND_DIABETES_TYP_1_UND_KHK("49", "Asthma, Diabetes Typ 1 und KHK"),
  ASTHMA_UND_BRUSTKREBS_UND_KHK("50", "Asthma, Brustkrebs und KHK"),
  BRUSTKREBS_UND_COPD_UND_KHK("51", "Brustkrebs, COPD und KHK"),
  BRUSTKREBS_UND_COPD_UND_DIABETES_TYP_2_UND_KHK("52", "Brustkrebs, COPD, Diabetes Typ 2 und KHK"),
  ASTHMA_UND_BRUSTKREBS_UND_DIABETES_TYP_2_UND_KHK(
      "53", "Asthma, Brustkrebs, Diabetes Typ 2 und KHK"),
  BRUSTKREBS_UND_DIABETES_TYP_1_UND_KHK("54", "Brustkrebs, Diabetes Typ 1 und KHK"),
  ASTHMA_UND_BRUSTKREBS_UND_DIABETES_TYP_1("55", "Asthma, Brustkrebs und Diabetes Typ 1"),
  ASTHMA_UND_BRUSTKREBS_UND_DIABETES_TYP_1_UND_KHK(
      "56", "Asthma, Brustkrebs, Diabetes Typ 1 und KHK"),
  BRUSTKREBS_UND_COPD_UND_DIABETES_TYP_1("57", "Brustkrebs, COPD und Diabetes Typ 1"),
  BRUSTKREBS_UND_COPD_UND_DIABETES_TYP_1_UND_KHK("58", "Brustkrebs, COPD, Diabetes Typ 1 und KHK");

  public static final KbvCodeSystem CODE_SYSTEM = KbvCodeSystem.DMP;
  private final String code;
  private final String display;

  @Override
  public KbvCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public Extension asExtension() {
    return new Extension(
        DeBasisProfilStructDef.GKV_DMP_KENNZEICHEN.getCanonicalUrl(), this.asCoding());
  }

  public static DmpKennzeichen fromCode(String code) {
    return Arrays.stream(DmpKennzeichen.values())
        .filter(dmp -> dmp.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(DmpKennzeichen.class, code));
  }
}
