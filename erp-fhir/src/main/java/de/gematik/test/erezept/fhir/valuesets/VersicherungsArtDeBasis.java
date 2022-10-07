/*
 * Copyright (c) 2022 gematik GmbH
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
import de.gematik.test.erezept.fhir.parser.profiles.ErpCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.NonNull;

/** https://simplifier.net/packages/de.basisprofil.r4/1.0.0/files/397841 */
@Getter
public enum VersicherungsArtDeBasis implements IValueSet {
  GKV("GKV", "gesetzliche Krankenversicherung"),
  PKV("PKV", "private Krankenversicherung"),
  BG("BG", "Berufsgenossenschaft"),
  SEL("SEL", "Selbstzahler"),
  SOZ("SOZ", "Sozialamt"),
  GPV("GPV", "gesetzliche Pflegeversicherung"),
  PPV("PPV", "private Pflegeversicherung"),
  BEI("BEI", "Beihilfe"),
  ;

  public static final ErpCodeSystem CODE_SYSTEM = ErpCodeSystem.VERSICHERUNGSART_DE_BASIS;
  public static final String VERSION = "0.9.13";
  public static final String DESCRIPTION =
      "ValueSet zur Codierung der Versicherungsart in Deutschland";
  public static final String PUBLISHER = "HL7 Deutschland e.V. (Technisches Komitee FHIR)";

  private final String code;
  private final String display;
  private final String definition = "N/A";

  VersicherungsArtDeBasis(String code, String display) {
    this.code = code;
    this.display = display;
  }

  public static VersicherungsArtDeBasis fromCode(@NonNull String code) {
    return Arrays.stream(VersicherungsArtDeBasis.values())
        .filter(scp -> scp.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(VersicherungsArtDeBasis.class, code));
  }

  @Override
  public ErpCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }
}
