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

public enum KbvProfileRules implements RequirementsSet {
  AUTHORED_ON_DATEFORMAT(
      "AUTHORED_ON_DATEFORMAT", "Begrenzung der Datumsangabe auf 10 Zeichen JJJJ-MM-TT"),
  ACCIDENT_EXTENSION(
      "UNFALLKENNZEICHEN", "Unfallkennzeichen müssen den Regeln der Profiling entsprechen"),
  EXTENDED_VALUE_SET_DARREICHUNGSFORMEN(
      "EXTENDED_DARREICHUNG", "Aktualisierte Schlüsseltabelle für Darreichungsformen"),
  EXTENDED_VALUE_SET_DMPKENNZEICHEN(
      "EXTENDED_DMPKENNZEICHEN", "Aktualisierte Schlüsseltabelle für DmpKennzeichen"),

  SUPPLY_REQUEST_AND_MEDICATION_REQUEST(
      "VERORDNUNGSDATENSATZ_OHNE_MEDICATION_REQUEST",
      "A_23384, Mit 1.12.0 wird A_23384 \"E-Rezept-Fachdienst - Prüfung Gültigkeit FHIR"
          + " Ressourcen\" ->Bundles ohne MedicationRequest oder MedicationRequest.authoredOn"
          + " werden abgewiesen "),

  IKNR_VALIDATION(
      "IKNR_LENGTH_VALIDATION",
      "Die IK-Nr. muss genau 9 Stellen lang sein ('/KBV_PR_FOR_Coverage') und darf nur aus Zahlen"
          + " bestehen (HL7-DE 'identifier-iknr' ('[0-9]{9}')"),

  KVNR_VALIDATION(
      "KVNR_VALIDATION",
      "Die KVNR muss (laut KBV.ita.for.Patient) 10 stellig sein und mit einem Großbuchstaben"
          + " beginnen matches('^[A-Z][0-9]{9}$')");

  @Getter private final Requirement requirement;

  KbvProfileRules(String id, String description) {
    this.requirement = new Requirement(id, description);
  }

  @Override
  public String toString() {
    return requirement.toString();
  }
}
