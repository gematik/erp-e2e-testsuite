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

package de.gematik.test.erezept.fhir.valuesets.dav;

import de.gematik.bbriccs.fhir.coding.FromValueSet;
import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.profiles.systems.AbdaCodeSystem;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KostenVersicherterKategorie implements FromValueSet {
  ZUZAHLUNG("0", "Zuzahlung"),
  MEHRKOSTEN("1", "Mehrkosten (Apothekenverkaufspreis minus Festbetrag)"),
  EIGENBETEILIGUNG(
      "2",
      "Eigenbeteiligung des Versicherten bei Verordnungen im Rahmen der künstlichen Befruchtung");

  public static final AbdaCodeSystem CODE_SYSTEM = AbdaCodeSystem.KOSTEN_VERSICHERTER_KATEGORIE;
  public static final String DESCRIPTION = "Category of the cost";

  private final String code;
  private final String display;

  @Override
  public AbdaCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static KostenVersicherterKategorie fromCode(String code) {
    return Arrays.stream(KostenVersicherterKategorie.values())
        .filter(kvk -> kvk.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(KostenVersicherterKategorie.class, code));
  }

  public static KostenVersicherterKategorie fromName(String name) {
    return Arrays.stream(KostenVersicherterKategorie.values())
        .filter(kvk -> kvk.name().equalsIgnoreCase(name))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(KostenVersicherterKategorie.class, name));
  }
}
