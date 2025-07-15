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
public enum AbrechnungsTyp implements FromValueSet {
  STANDARD("1", "Standard"),
  DIRECT("2", "Direktabrechnung");

  public static final AbdaCodeSystem CODE_SYSTEM = AbdaCodeSystem.ABRECHNUNGSTYP;
  public static final String DESCRIPTION = "Type of the Invoice";

  private final String code;
  private final String display;

  @Override
  public AbdaCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static AbrechnungsTyp fromCode(String code) {
    return Arrays.stream(AbrechnungsTyp.values())
        .filter(at -> at.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(AbrechnungsTyp.class, code));
  }
}
