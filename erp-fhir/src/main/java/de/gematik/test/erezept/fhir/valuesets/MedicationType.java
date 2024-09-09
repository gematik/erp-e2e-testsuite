/*
 * Copyright 2024 gematik GmbH
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
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.NonNull;

@Getter
public enum MedicationType implements IValueSet {
  INGREDIENT("wirkstoff", "Wirkstoff"),
  FREETEXT("freitext", "Freitext"),
  COMPOUNDING("rezeptur", "Rezeptur");

  public static final KbvCodeSystem CODE_SYSTEM = KbvCodeSystem.MEDICATION_TYPE;
  public static final String VERSION = "1.0.1";
  public static final String DESCRIPTION =
      "Klassifizierung von Medikamenten soweit keine PZN-Verordnung erfolgt";
  public static final String PUBLISHER = "Kassenärztliche Bundesvereinigung";

  private final String code;
  private final String display;
  private final String definition;

  MedicationType(String code, String display) {
    this.code = code;
    this.display = display;
    this.definition = "N/A definition in profile";
  }

  public static MedicationType fromCode(@NonNull String coding) {
    return Arrays.stream(MedicationType.values())
        .filter(mc -> mc.code.equals(coding))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(MedicationType.class, coding));
  }

  @Override
  public KbvCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }
}
