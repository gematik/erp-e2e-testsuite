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

package de.gematik.test.erezept.fhir.values;

import de.gematik.bbriccs.fhir.coding.SemanticValue;
import de.gematik.test.erezept.fhir.profiles.systems.KbvNamingSystem;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.r4.model.Practitioner.PractitionerQualificationComponent;

public class AsvFachgruppennummer extends SemanticValue<String, KbvNamingSystem> {

  private static final KbvNamingSystem NAMING_SYSTEM = KbvNamingSystem.ASV_FACHGRUPPENNUMMER;

  private AsvFachgruppennummer(String value) {
    super(NAMING_SYSTEM, value);
  }

  public static boolean matches(PractitionerQualificationComponent pqc) {
    return NAMING_SYSTEM.matches(pqc.getCode());
  }

  public static Optional<AsvFachgruppennummer> from(List<PractitionerQualificationComponent> pqc) {
    return pqc.stream()
        .filter(AsvFachgruppennummer::matches)
        .map(it -> from(it.getCode().getCodingFirstRep().getCode()))
        .findFirst();
  }

  public static AsvFachgruppennummer from(String s) {
    return new AsvFachgruppennummer(s);
  }
}
