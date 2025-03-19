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

import de.gematik.bbriccs.fhir.coding.FromValueSet;
import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.fhir.de.DeBasisProfilStructDef;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Extension;

/** <a href="https://applications.kbv.de/S_KBV_NORMGROESSE_V1.00.xhtml">Normgröße</a> */
@Getter
@RequiredArgsConstructor
public enum StandardSize implements FromValueSet {
  KA("KA", "Kein Angabe"),
  KTP("KTP", "Keine therapiegerechte Packungsgröße"),
  N1("N1", "Normgröße 1"),
  N2("N2", "Normgröße 2"),
  N3("N3", "Normgröße 3"),
  NB("NB", "Nicht betroffen"),
  SONSTIGES("Sonstiges", "Sonstiges");

  public static final DeBasisProfilCodeSystem CODE_SYSTEM = DeBasisProfilCodeSystem.NORMGROESSE;

  private final String code;
  private final String display;

  @Override
  public DeBasisProfilCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public Extension asExtension() {
    return DeBasisProfilStructDef.NORMGROESSE.asCodeExtension(this.getCode());
  }

  public static StandardSize fromCode(String code) {
    return Arrays.stream(StandardSize.values())
        .filter(size -> size.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(StandardSize.class, code));
  }
}
