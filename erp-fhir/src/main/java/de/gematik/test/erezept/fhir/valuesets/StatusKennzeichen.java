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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.coding.FromValueSet;
import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import org.hl7.fhir.r4.model.Extension;

/**
 * <a href="https://applications.kbv.de/S_KBV_STATUSKENNZEICHEN_V1.01.xhtml">KBV
 * Statuskennzeichen</a>
 */
@Getter
public enum StatusKennzeichen implements FromValueSet {
  NONE("00", "ohne Ersatzverordnungskennzeichen"),
  ASV("01", "ASV-Kennzeichen"),
  RELEASE_MGMT("04", "Entlassmanagement-Kennzeichen"),
  TSS("07", "TSS-Kennzeichen"),
  SUBSTITUTE("10", "nur Ersatzverordnungskennzeichen"),
  ASV_SUBSTITUTE("11", "ASV-Kennzeichen mit Ersatzverordnungskennzeichen"),
  RELEASE_SUBSTITUTE("14", "Entlassmanagement-Kennzeichen mit Ersatzverordnungskennzeichen"),
  TSS_SUBSTITUTE("17", "TSS-Kennzeichen mit Ersatzverordnungskennzeichen"),
  ;

  public static final KbvCodeSystem CODE_SYSTEM = KbvCodeSystem.STATUSKENNZEICHEN;
  public static final String VERSION = "1.01";
  public static final String DESCRIPTION =
      "Das Statuskennzeichen wird im Statusfeld des Personalienfeldes auf den KBV-Mustern"
          + " angegeben. Weitere Informationen dazu siehe: technische Anlage zur Anlage 4a des"
          + " BMV-Ä";
  public static final String PUBLISHER = "Kassenärztliche Bundesvereinigung";

  private final String code;
  private final String display;
  private final String definition = "N/A";

  StatusKennzeichen(String code, String display) {
    this.code = code;
    this.display = display;
  }

  @Override
  public KbvCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public Extension asExtension() {
    return new Extension(KbvItaForStructDef.BASIS.getCanonicalUrl(), this.asCoding());
  }

  @Override
  public String toString() {
    return format("{0} ({1})", code, display);
  }

  public static StatusKennzeichen fromCode(String code) {
    return Arrays.stream(StatusKennzeichen.values())
        .filter(sk -> sk.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(StatusKennzeichen.class, code));
  }
}
