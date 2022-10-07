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
public enum AbrechnungsTyp implements IValueSet {
  STANDARD("1", "Standard"),
  DIRECT("2", "Direktabrechnung");

  public static final ErpCodeSystem CODE_SYSTEM = ErpCodeSystem.DAV_PKV_CS_ERP_ABRECHNUNGSTYP;
  public static final String VERSION = "1.1";
  public static final String DESCRIPTION = "Type of the Invoice";
  public static final String PUBLISHER = "DAV";

  private final String code;
  private final String display;

  @Override
  public String getDefinition() {
    return "Typ der Abrechnung";
  }

  @Override
  public ErpCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static AbrechnungsTyp fromCode(@NonNull String code) {
    return Arrays.stream(AbrechnungsTyp.values())
        .filter(at -> at.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(AbrechnungsTyp.class, code));
  }
}
