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
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisCodeSystem;
import de.gematik.test.erezept.fhir.values.BGInsuranceCoverageInfo;
import de.gematik.test.erezept.fhir.values.GkvInsuranceCoverageInfo;
import de.gematik.test.erezept.fhir.values.InsuranceCoverageInfo;
import de.gematik.test.erezept.fhir.values.PkvInsuranceCoverageInfo;
import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;

/** <a href="https://simplifier.net/packages/de.basisprofil.r4/1.0.0/files/397841">de.basisprofil.r4</a> */
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

  public static final DeBasisCodeSystem CODE_SYSTEM = DeBasisCodeSystem.VERSICHERUNGSART_DE_BASIS;
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

  @Override
  public DeBasisCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public <T extends InsuranceCoverageInfo> Optional<Class<T>> getCoverageOptions() {
    return switch (this) {
      case GKV -> Optional.of((Class<T>) GkvInsuranceCoverageInfo.class);
      case PKV -> Optional.of((Class<T>) PkvInsuranceCoverageInfo.class);
      case BG -> Optional.of((Class<T>) BGInsuranceCoverageInfo.class);
      default -> Optional.empty();
    };
  }
  
  public static VersicherungsArtDeBasis fromCode(@NonNull String code) {
    return Arrays.stream(VersicherungsArtDeBasis.values())
        .filter(scp -> scp.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(VersicherungsArtDeBasis.class, code));
  }


}
