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
import de.gematik.bbriccs.fhir.de.DeBasisProfilStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Extension;

/**
 * <a
 * href="https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS">Versichertenstatus</a>
 */
@Getter
@RequiredArgsConstructor
public enum VersichertenStatus implements FromValueSet {
  MEMBERS("1", "Mitglieder"),
  FAMILY_MEMBERS("3", "Familienangehoerige"),
  PENSIONER("5", "Rentner");

  public static final KbvCodeSystem CODE_SYSTEM = KbvCodeSystem.VERSICHERTEN_STATUS;
  private final String code;
  private final String display;

  @Override
  public KbvCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public Extension asExtension() {
    return new Extension(
        DeBasisProfilStructDef.GKV_VERSICHERTENART.getCanonicalUrl(), this.asCoding());
  }

  public static VersichertenStatus fromCode(String code) {
    return Arrays.stream(VersichertenStatus.values())
        .filter(mc -> mc.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(VersichertenStatus.class, code));
  }
}
