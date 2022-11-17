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
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.NonNull;

/**
 * <br>
 * <b>Profile:</b> de.basisprofil.r4 (0.9.13) <br>
 * <b>File:</b> ValueSet-identifier-type-de-basis.json <br>
 * <br>
 * <b>Publisher:</b> HL7 Deutschland e.V. (Technisches Komitee FHIR) <br>
 * <b>Published:</b> 2020-01-10 <br>
 * <b>Status:</b> draft
 */
@Getter
public enum IdentifierTypeDe implements IValueSet {
  GKV("GKV", "Gesetzliche Krankenversicherung"),
  PKV("PKV", "Private Krankenversicherung"),
  LANR("LANR", "Lebenslange Arztnummer"),
  ZANR("ZANR", "Zahnarztnummer"),
  BSNR("BSNR", "Betriebsstättennummer"),
  KZVA("KZVA", "KZVAbrechnungsnummer"),
  ;

  public static final DeBasisCodeSystem CODE_SYSTEM = DeBasisCodeSystem.IDENTIFIER_TYPE_DE_BASIS;
  public static final String VERSION = "0.9.13";
  public static final String DESCRIPTION = "ValueSet zur Codierung des Identifier-Typs";
  public static final String PUBLISHER = "HL7 Deutschland e.V. (Technisches Komitee FHIR)";

  private final String code;
  private final String display;
  private final String definition;

  IdentifierTypeDe(String code, String display) {
    this(code, display, "N/A definition in profile");
  }

  IdentifierTypeDe(String code, String display, String definition) {
    this.code = code;
    this.display = display;
    this.definition = definition;
  }

  @Override
  public DeBasisCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static IdentifierTypeDe fromCode(@NonNull String value) {
    return Arrays.stream(IdentifierTypeDe.values())
        .filter(itd -> itd.code.equals(value))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(IdentifierTypeDe.class, value));
  }
}
