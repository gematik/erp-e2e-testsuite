/*
 * Copyright 2023 gematik GmbH
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

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.DeBasisStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.NonNull;
import org.hl7.fhir.r4.model.Extension;

/** https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS */
@Getter
public enum VersichertenStatus implements IValueSet {
  MEMBERS("1", "Mitglieder"),
  FAMILY_MEMBERS("3", "Familienangehoerige"),
  PENSIONER("5", "Rentner");

  public static final KbvCodeSystem CODE_SYSTEM = KbvCodeSystem.VERSICHERTEN_STATUS;
  public static final String VERSION = "1.02";
  public static final String DESCRIPTION =
      "Versichertenstatus gibt an, ob ein Versicherter ein Familienversicherter, Mitglied oder Rentner ist. Auf der KVK ist diese Angabe Teil des Feldes VERSICHERTENSTATUS - die 1. Stelle.";
  public static final String PUBLISHER = "Kassenärztliche Bundesvereinigung";

  private final String code;
  private final String display;
  private final String definition = "N/A";

  VersichertenStatus(String code, String display) {
    this.code = code;
    this.display = display;
  }

  @Override
  public KbvCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public Extension asExtension() {
    return new Extension(DeBasisStructDef.GKV_VERSICHERTENART.getCanonicalUrl(), this.asCoding());
  }

  public static VersichertenStatus fromCode(@NonNull String coding) {
    return Arrays.stream(VersichertenStatus.values())
        .filter(mc -> mc.code.equals(coding))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(VersichertenStatus.class, coding));
  }
}
