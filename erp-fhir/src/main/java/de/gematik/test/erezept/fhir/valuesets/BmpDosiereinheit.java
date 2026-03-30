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
import de.gematik.test.erezept.fhir.profiles.systems.KbvCodeSystem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Extension;

@Getter
@RequiredArgsConstructor
public enum BmpDosiereinheit implements FromValueSet {
  MESSLOEFFEL("#", "Messlöffel", true, 8),
  MESSBECHER("0", "Messbecher", true, 9),
  STUECK("1", "Stück", true, 1),
  PKG("2", "Pkg.", false, 22),
  FLASCHE("3", "Flasche", true, 15),
  BEUTEL("4", "Beutel", true, 16),
  HUB("5", "Hub", true, 4),
  TROPFEN("6", "Tropfen", true, 2),
  TEELOEFFEL("7", "Teelöffel", true, 26),
  ESSLOEFFEL("8", "Esslöffel", true, 27),
  E("9", "E", true, 13),
  TASSE("a", "Tasse", true, 29),
  APPLIKATORFUELLUNG("b", "Applikatorfüllung", true, 11),
  AUGENBADEWANNE("c", "Augenbadewanne", true, 33),
  DOSIERBRIEFCHEN("d", "Dosierbriefchen", true, 23),
  DOSIERPIPETTE("e", "Dosierpipette", true, 24),
  DOSIERSPRITZE("f", "Dosierspritze", true, 25),
  EINZELDOSIS("g", "Einzeldosis", true, 12),
  GLAS("h", "Glas", true, 28),
  LIKOERGLAS("i", "Likörglas", true, 32),
  MESSKAPPE("j", "Messkappe", true, 30),
  MESSSCHALE("k", "Messschale", true, 31),
  MIO_E("l", "Mio E", true, 17),
  MIO_IE("m", "Mio IE", true, 18),
  PIPETTENTEILSTRICH("n", "Pipettenteilstrich", true, 14),
  SPRUEHSTOSS("o", "Sprühstoß", true, 5),
  IE("p", "IE", true, 3),
  CM("q", "cm", true, 7),
  L("r", "l", true, 20),
  ML("s", "ml", true, 6),
  G("t", "g", true, 19),
  KG("u", "kg", false, 21),
  MG("v", "mg", false, 10);

  public static final KbvCodeSystem CODE_SYSTEM = KbvCodeSystem.SFHIR_BMP_DOSIEREINHEIT;
  public static final String CANONICAL_URL = CODE_SYSTEM.getCanonicalUrl();

  private final String code;
  private final String display;
  private final boolean unterstuetzt;
  private final int sortierung;

  public static BmpDosiereinheit fromCode(String code) {
    return java.util.Arrays.stream(BmpDosiereinheit.values())
        .filter(e -> e.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(BmpDosiereinheit.class, code));
  }

  @Override
  public KbvCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  // Optional: nur verwenden, falls es eine passende Extension-Definition gibt.
  // Andernfalls bitte entfernen oder auf die korrekte StructureDefinition-URL anpassen.
  public Extension asExtension() {
    return new Extension(CANONICAL_URL, this.asCoding());
  }
}
