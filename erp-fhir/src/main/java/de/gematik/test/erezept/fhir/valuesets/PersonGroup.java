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
import de.gematik.bbriccs.fhir.de.DeBasisProfilStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.KbvCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Extension;

/** <a href="https://applications.kbv.de/S_KBV_PERSONENGRUPPE.xhtml">KBV Personengruppe</a> */
@Getter
@RequiredArgsConstructor
public enum PersonGroup implements FromValueSet {
  NOT_SET("00", "Nicht gesetzt"),
  SOZ("04", "SOZ"),
  BVG("06", "BVG"),
  SVA_1("07", "SVA1"),
  SVA_2("08", "SVA2"),
  ASY("09", "ASY");

  public static final KbvCodeSystem CODE_SYSTEM = KbvCodeSystem.PERSON_GROUP;
  private final String code;
  private final String display;

  @Override
  public KbvCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public Extension asExtension() {
    return new Extension(
        DeBasisProfilStructDef.GKV_PERSON_GROUP.getCanonicalUrl(), this.asCoding());
  }

  public static PersonGroup fromCode(String coding) {
    return Arrays.stream(PersonGroup.values())
        .filter(pg -> pg.code.equals(coding))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(PersonGroup.class, coding));
  }
}
