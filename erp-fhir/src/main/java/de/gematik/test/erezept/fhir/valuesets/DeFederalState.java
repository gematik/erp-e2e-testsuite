/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.valuesets;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.parser.profiles.systems.CommonCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.NonNull;

/**
 * <br>
 * <b>Profile:</b> de.basisprofil.r4 (0.9.13) <br>
 * <b>File:</b> ValueSet-iso-3166-2-de-laendercodes.json <br>
 * <br>
 * <b>Publisher:</b> HL7 Deutschland e.V. (Technisches Komitee FHIR) <br>
 * <b>Published:</b> 2020-12-08 <br>
 * <b>Status:</b> draft
 */
@Getter
public enum DeFederalState implements IValueSet {
  DE_BW("DE-BW", "Baden-Württemberg"),
  DE_BY("DE-BY", "Bayern"),
  DE_BE("DE-BE", "Berlin"),
  DE_BB("DE-BB", "Brandenburg"),
  DE_HB("DE-HB", "Bremen"),
  DE_HH("DE-HH", "Hamburg"),
  DE_HE("DE-HE", "Hessen"),
  DE_MV("DE-MV", "Mecklenburg-Vorpommern"),
  DE_NI("DE-NI", "Niedersachsen"),
  DE_NW("DE-NW", "Nordrhein-Westfalen"),
  DE_RP("DE-RP", "Rheinland-Pfalz"),
  DE_SL("DE-SL", "Saarland"),
  DE_SN("DE-SN", "Sachsen"),
  DE_ST("DE-ST", "Sachsen-Anhalt"),
  DE_SH("DE-SH", "Schleswig-Holstein"),
  DE_TH("DE-TH", "Thüringen"),
  ;

  public static final CommonCodeSystem CODE_SYSTEM = CommonCodeSystem.ISO_31662_DE;
  public static final String VERSION = "0.9.13";
  public static final String DESCRIPTION =
      "Enthält die ISO-3166-2:DE Codes für die deutschen Bundesländer.";
  public static final String PUBLISHER = "HL7 Deutschland e.V. (Technisches Komitee FHIR)";

  private final String code;
  private final String display;
  private final String definition;

  DeFederalState(String code, String display) {
    this(code, display, "N/A definition in profile");
  }

  DeFederalState(String code, String display, String definition) {
    this.code = code;
    this.display = display;
    this.definition = definition;
  }

  @Override
  public CommonCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static DeFederalState fromCode(@NonNull String coding) {
    return Arrays.stream(DeFederalState.values())
        .filter(dfs -> dfs.code.equals(coding))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(DeFederalState.class, coding));
  }

  public static DeFederalState fromDisplay(@NonNull String displayValue) {
    return Arrays.stream(DeFederalState.values())
        .filter(dfs -> dfs.display.equalsIgnoreCase(displayValue))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(DeFederalState.class, displayValue));
  }
}
