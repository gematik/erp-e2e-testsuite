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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.hl7.fhir.r4.model.Practitioner.PractitionerQualificationComponent;

/** <a href="https://simplifier.net/for/kbvcsforqualificationtype">...</a> */
@Getter
public enum QualificationType implements FromValueSet {
  DOCTOR("00", "Arzt"),
  DENTIST("01", "Zahnarzt"),
  MIDWIFE("02", "Hebamme"),
  DOCTOR_IN_TRAINING("03", "Arzt in Weiterbildung"),
  DOCTOR_AS_REPLACEMENT("04", "Arzt als Vertreter"),
  ;

  public static final KbvCodeSystem CODE_SYSTEM = KbvCodeSystem.QUALIFICATION_TYPE;

  private final String code;
  private final String display;

  QualificationType(String code, String display) {
    this.code = code;
    this.display = display;
  }

  public static boolean matches(PractitionerQualificationComponent pqc) {
    return CODE_SYSTEM.matches(pqc.getCode());
  }

  public static Optional<QualificationType> from(List<PractitionerQualificationComponent> pqc) {
    return pqc.stream()
        .filter(QualificationType::matches)
        .map(it -> from(it.getCode().getCodingFirstRep().getCode()))
        .findFirst();
  }

  public static QualificationType from(String code) {
    return FromValueSet.fromCode(QualificationType.class, code);
  }

  public static QualificationType fromDisplay(String display) {
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
