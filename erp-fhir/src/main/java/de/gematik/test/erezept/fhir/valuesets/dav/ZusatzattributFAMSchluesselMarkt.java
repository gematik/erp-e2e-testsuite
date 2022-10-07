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

package de.gematik.test.erezept.fhir.valuesets.dav;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.parser.profiles.ErpCodeSystem;
import de.gematik.test.erezept.fhir.valuesets.IValueSet;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public enum ZusatzattributFAMSchluesselMarkt implements IValueSet {
  NICHT_BETROFFEN("0", "nicht betroffen"),
  GENERIKA("1", "Generika"),
  SOLITAER("2", "Solitär"),
  MEHRFACHVERTRIEB("3", "Mehrfachvertrieb"),
  AUT_IDEM("4", "aut-idem gesetzt"),
  SUBSTITUTIONS_AUSSCHLUSS("5", "Produkt der Substitutionsausschlussliste"),
  ;

  public static final ErpCodeSystem CODE_SYSTEM =
      ErpCodeSystem.DAV_CS_ERP_ZUSATZATTRIBUTE_FAM_SCHLUESSEL_MARKT;
  public static final String VERSION = "1.2";
  public static final String DESCRIPTION = "";
  public static final String PUBLISHER = "DAV";

  private final String code;
  private final String display;
  private final String definition;

  ZusatzattributFAMSchluesselMarkt(String code, String display) {
    this(code, display, "n/a");
  }

  @Override
  public ErpCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static ZusatzattributFAMSchluesselMarkt fromCode(@NonNull String code) {
    return Arrays.stream(ZusatzattributFAMSchluesselMarkt.values())
        .filter(zafamsm -> zafamsm.code.equals(code))
        .findFirst()
        .orElseThrow(
            () -> new InvalidValueSetException(ZusatzattributFAMSchluesselMarkt.class, code));
  }
}
