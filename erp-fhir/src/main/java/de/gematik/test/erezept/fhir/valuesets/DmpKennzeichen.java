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

import de.gematik.test.erezept.fhir.parser.profiles.definitions.DeBasisStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import lombok.Getter;
import org.hl7.fhir.r4.model.Extension;

/** https://applications.kbv.de/S_KBV_DMP.xhtml https://www.kbv.de/html/dmp.php */
@Getter
public enum DmpKennzeichen implements IValueSet {
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
  ;

  public static final KbvCodeSystem CODE_SYSTEM = KbvCodeSystem.DMP;
  public static final String VERSION = "1.05";
  public static final String DESCRIPTION =
      "DMP-Kennzeichen: gibt an, in welchen DMPs ein Versicherter eingeschrieben ist (§ 267 Abs. 2 Satz 4 SGB V). Die Angabe ist auf der EGK vorhanden und auf der KVK Teil des Feldes: Statusergänzung.";
  public static final String PUBLISHER = "Kassenärztliche Bundesvereinigung";

  private final String code;
  private final String display;
  private final String definition = "N/A definition in profile";

  DmpKennzeichen(String code, String display) {
    this.code = code;
    this.display = display;
  }

  @Override
  public KbvCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public Extension asExtension() {
    return new Extension(DeBasisStructDef.GKV_DMP_KENNZEICHEN.getCanonicalUrl(), this.asCoding());
  }
}
