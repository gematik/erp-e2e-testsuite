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

package de.gematik.test.erezept.fhir.valuesets;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.NonNull;

/** https://simplifier.net/for/kbvcsforqualificationtype */
@Getter
public enum QualificationType implements IValueSet {
  DOCTOR("00", "Arzt"),
  DENTIST("01", "Zahnarzt"),
  MIDWIFE("02", "Hebamme"),
  DOCTOR_IN_TRAINING("03", "Arzt in Weiterbildung"),
  DOCTOR_AS_REPLACEMENT("04", "Arzt als Vertreter"),
  ;

  public static final KbvCodeSystem CODE_SYSTEM = KbvCodeSystem.QUALIFICATION_TYPE;
  public static final String VERSION = "1.0.1";
  public static final String DESCRIPTION = "Kennzeichnung/Kategorie der Person";
  public static final String PUBLISHER = "Kassenärztliche Bundesvereinigung";

  private final String code;
  private final String display;
  private final String definition = "N/A definition in profile";

  QualificationType(String code, String display) {
    this.code = code;
    this.display = display;
  }

  public static QualificationType fromCode(@NonNull String coding) {
    return Arrays.stream(QualificationType.values())
        .filter(qt -> qt.code.equals(coding))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(QualificationType.class, coding));
  }

  public static QualificationType fromDisplay(@NonNull String display) {
    return Arrays.stream(QualificationType.values())
        .filter(qt -> qt.display.equalsIgnoreCase(display))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(QualificationType.class, display));
  }

  @Override
  public KbvCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }
}
