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

package de.gematik.test.core.expectations.requirements;

import lombok.Getter;

public enum EmlAfos implements RequirementsSet {
  A_25925(
      "A_25925",
      "E-Rezept-Fachdienst - Task aktivieren - Daten ePA Medication Service bereitstellen"
          + " (Verordnungsdatensatz)"),
  A_25930(
      "A_25930",
      "E-Rezept-Fachdienst - E-Rezept löschen - Löschinformation ePA Medication Service"
          + " bereitstellen (Verordnungsdatensatz) - Leistungserbringerinstitution"),
  A_25931(
      "A_25931",
      "E-Rezept-Fachdienst - E-Rezept löschen - Löschinformation ePA Medication Service"
          + " bereitstellen (Verordnungsdatensatz) - Versicherter"),
  A_25946("A_25946", "E-Rezept-Fachdienst - ePA - Mapping"),
  A_25948(
      "A_25948", "E-Rezept-Fachdienst - ePA - Mapping - Übernahme von Werten zwischen Profilen"),
  A_25949("A_25949", "E-Rezept-Fachdienst - ePA - Mapping - Handhabung von Extensions"),
  A_25951("A_25951", "E-Rezept-Fachdienst - ePA - Prüfung des Widerspruchs vor Übermittlung"),
  A_25952(
      "A_25952", "E-Rezept-Fachdienst - ePA - Übermittlung - Bereitstellung von Verordnungsdaten"),
  A_25953(
      "A_25953",
      "E-Rezept-Fachdienst - ePA - Übermittlung - Löschinformation von Verordnungsdaten"),
  A_25955(
      "A_25955",
      "E-Rezept-Fachdienst - ePA - Übermittlung - Löschinformation von Dispensierinformationen"),
  ;

  @Getter private final Requirement requirement;

  EmlAfos(String id, String description) {
    this.requirement = new Requirement(id, description);
  }

  @Override
  public String toString() {
    return requirement.toString();
  }
}
