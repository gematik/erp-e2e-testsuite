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

package de.gematik.test.core.expectations.requirements;

import lombok.Getter;

public enum ErpAfos implements RequirementsSet {
  A_19018("A_19018", "Rollenprüfung Verordnender stellt Rezept ein"),
  A_19019("A_19019", "Generierung Rezept-ID"),
  A_19569_03("A_19569_03", "Task abrufen - Versicherter - Suchparameter Task"),
  A_19021("A_19021", "Generierung AccessCode"),
  A_19022("A_19022", "Rollenprüfung Verordnender aktiviert Rezept"),
  A_19112("A_19112", "Parametrierung Task für Workflow-Typ"),
  A_19113_01("A_19113-01","Rollenprüfung Versicherter oder Apotheker liest Rezept"),
  A_19114("A_19114", "Task im Status Draft nach Erstellung"),
  A_19128("A_19128", "Task im Status Ready nach Aktivierung"),
  A_19166("A_19166", "Rollenprüfung Abgebender ruft Rezept ab"),
  A_19169("A_19169", "Generierung Secret, Statuswechsel in Abgabe und Rückgabewert"),
  A_19230("A_19230", "Rollenprüfung Abgebender vollzieht Abgabe des Rezepts"),
  A_19248_02("A_19248-02", "Schemaprüfung und Speicherung MedicationDispense"),
  A_19445("A_19445-08", "FHIR FlowType für Prozessparameter"),
  A_19514_03("A_19514-03", "HTTP Status-Codes"),
  A_22110("A_22110", "Task akzeptieren - Flowtype 200/209 - Einwilligung ermitteln"),
  A_22487("A_22487", "Prüfregel Ausstellungsdatum"),
  A_22627_01("A_22627-01", "Mehrfachverordnung - zulässige Flowtype"),
  A_22628("A_22628", "Mehrfachverordnung - Numerator-Denominator kleiner 5"),
  A_22629("A_22629", "Mehrfachverordnung - Denominator größer 1"),
  A_22630("A_22630", "Mehrfachverordnung - Numerator kleiner / gleich Denominator"),
  A_22631("A_22631", "Mehrfachverordnung - Unzulässige Angaben"),
  A_22632("A_22632", "Mehrfachverordnung - kein Entlassrezept"),
  A_22633("A_22633", "Mehrfachverordnung - keine Ersatzverordnung"),
  A_22634("A_22634", "Mehrfachverordnung - Beginn Einlösefrist-Pflicht"),
  A_22704("A_22704", "Mehrfachverordnung - Numerator größer 0"),
  A_22927("A_22927", "Ausschluss unspezifizierter Extensions"),
  A_23443("A_23443", "Task aktivieren – Flowtype 160/169 - Prüfung Coverage Type"),
  A_22347("A_22347-01", "Task aktivieren – Flowtype 200/209 - Prüfung Coverage Type"),
  A_19284("A_19284-04", "Versichertenprotokoll zu Operationen"),
  A_20165("A_20165-05", "Performance – E-Rezept-Fachdienst - Bearbeitungszeit unter Last"),
  A_23455("A_23455", "E-Rezept-Fachdienst - Prüfung Prüfziffer - keine Prüfziffer"),
  A_23450(
      "A_23450",
      "E-Rezept-Fachdienst - Rezepte lesen - Apotheke - VSDM - Prüfung Prüfungsnachweis"),
  A_23454("A_23454", "E-Rezept-Fachdienst - Prüfung Prüfziffer"),
  A_23888(
      "A_23888",
      "E-Rezept-Fachdienst - Task aktivieren – Überprüfung der IK Nummer im Profil KBV_PR_FOR_Coverage, -> payor.identifier.value"),
  A_23890(
      "A_23890",
      "Überprüfung der KVNR in KBV_PR_FOR_Patient - gemäß der Anlage 1 der 'Prüfziffernberechnung für die Krankenversichertennummer nach § 290 SGB V' vom 26.02.2019"),

  A_23891("A_23891", "Validierung der ANR / ZANR"),
  A_23936("A_23936", "E-Rezept-Fachdienst - Task aktivieren - Versicherten-ID als Identifikator von Versicherten"),
  A_24030(
      "A_24030",
      "Validierung der IK Nummer im Profil KBV_PR_FOR_Coverage, bei einem Unfall (BG-ABrechnung) -> payor.identifier.extension:alternativeID.value[x]:valueIdentifier"),
  A_24032(
      "A_24032",
      "Auffälligkeit für ANR & ZANR validierung, invalide Prüfziffer bei Konfiguration Fehler"),
  A_24033(
      "A_24033",
      "Auffälligkeit für ANR & ZANR validierung, invalide Prüfziffer bei Konfiguration Warnung"),

  A_23892("A_23892", "Überprüfung der PZN-Prüfziffer im Medication_PZN"),
  A_24034("A_24034", "Überprüfung der PZN-Prüfziffer im Medication_Compounding");
  @Getter private final Requirement requirement;

  ErpAfos(String id, String description) {
    this.requirement = new Requirement(id, description);
  }

  @Override
  public String toString() {
    return requirement.toString();
  }
}
