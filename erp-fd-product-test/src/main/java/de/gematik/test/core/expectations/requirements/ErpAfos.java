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

package de.gematik.test.core.expectations.requirements;

import lombok.Getter;

@Getter
public enum ErpAfos implements RequirementsSet {
  A_19018("A_19018", "Rollenprüfung Verordnender stellt Rezept ein"),
  A_19019("A_19019", "Generierung Rezept-ID"),
  A_19569("A_19569-03", "Task abrufen - Versicherter - Suchparameter Task"),
  A_19021("A_19021", "Generierung AccessCode"),
  A_19022("A_19022", "Rollenprüfung Verordnender aktiviert Rezept"),
  A_19112("A_19112", "Parametrierung Task für Workflow-Typ"),
  A_19113("A_19113-01", "Rollenprüfung Versicherter oder Apotheker liest Rezept"),
  A_19114("A_19114", "Task im Status Draft nach Erstellung"),
  A_19128("A_19128", "Task im Status Ready nach Aktivierung"),
  A_19166("A_19166", "Rollenprüfung Abgebender ruft Rezept ab"),
  A_19168("A_1968-01", "Task akzeptieren - Rezept bereits in Abgabe oder Bearbeitung"),
  A_19169("A_19169", "Generierung Secret, Statuswechsel in Abgabe und Rückgabewert"),
  A_19230("A_19230", "Rollenprüfung Abgebender vollzieht Abgabe des Rezepts"),
  A_19248("A_19248-02", "Schemaprüfung und Speicherung MedicationDispense"),
  A_19233(
      "A_19233-05",
      "Base64Binary-Ressource mit Binary.data = <base64-codierter Hashwert aus der QES-Signatur des"
          + " dem Task zugrunde liegenden Verordnungsdatensatzes>"),

  A_19284("A_19284-04", "Versichertenprotokoll zu Operationen"),
  A_19405("A_19405-01", "E-Rezept-Fachdienst - Rollenprüfung Versicherter liest MedicationDispens"),
  A_19445("A_19445-08", "FHIR FlowType für Prozessparameter"),
  A_19514("A_19514-03", "HTTP Status-Codes"),
  A_19520("A_19520-01", "E-Rezept-Fachdienst - Nachrichten abrufen - für Empfänger filtern"),
  A_19521("A_19521", "E-Rezept-Fachdienst - Nachrichten als abgerufen markieren"),
  A_19522("A_19522-01", " E-Rezept-Fachdienst - Nachrichtenabruf Suchparameter"),
  A_20165("A_20165-05", "Performance – E-Rezept-Fachdienst - Bearbeitungszeit unter Last"),
  A_20513("A_20513", "E-Rezept-Fachdienst - nicht mehr benötigte Einlösekommunikation"),
  A_21782("A_21782-01", "E-Rezept-Fachdienst - Schnittstellenadressierung Internet"),
  A_22110("A_22110", "Task akzeptieren - Flowtype 200/209 - Einwilligung ermitteln"),
  A_22136_01("A_22136_01", "Abrechnungsinformation bereitstellen – FHIR-Validierung ChargeItem"),
  A_22154("A_22154", " Consent löschen - alles Löschen verbieten ohne QuerryParam ?category"),
  A_22155("A_22155", "E-Rezept-Fachdienst - Consent löschen - Rollenprüfung Versicherter"),
  A_22158("A_22158", "E-Rezept-Fachdienst - Consent löschen - Löschen der Consent"),
  A_22159("A_22159", "E-Rezept-Fachdienst - Consent lesen - Rollenprüfung Versicherter"),
  A_22160(
      "A_22160", "E-Rezept-Fachdienst - Consent lesen - Filter Consent auf KVNR des Versicherten"),
  A_22161("A_22161", "E-Rezept-Fachdienst - Consent schreiben - Rollenprüfung Versicherter"),
  A_22162(
      "A_22162",
      "E-Rezept-Fachdienst - Consent schreiben – nur eine Einwilligung CHARGCONS pro KVNR"),
  A_22289("A_22289", "E-Rezept-Fachdienst - Consent schreiben - Prüfung KVNR"),
  A_22487("A_22487", "Prüfregel Ausstellungsdatum"),
  A_22627("A_22627-01", "Mehrfachverordnung - zulässige Flowtype"),
  A_22628("A_22628", "Mehrfachverordnung - Numerator-Denominator kleiner 5"),
  A_22629("A_22629", "Mehrfachverordnung - Denominator größer 1"),
  A_22630("A_22630", "Mehrfachverordnung - Numerator kleiner / gleich Denominator"),
  A_22631("A_22631", "Mehrfachverordnung - Unzulässige Angaben"),
  A_22632("A_22632", "Mehrfachverordnung - kein Entlassrezept"),
  A_22633("A_22633", "Mehrfachverordnung - keine Ersatzverordnung"),
  A_22634("A_22634", "Mehrfachverordnung - Beginn Einlösefrist-Pflicht"),
  A_22635_02(
      "A_22635_02",
      "E-Rezept-Fachdienst - Task akzeptieren Mehrfachverordnung - Beginn Einlösefrist prüfen"),
  A_22704("A_22704", "Mehrfachverordnung - Numerator größer 0"),
  A_22874("A_22874-01", "E-Rezept-Fachdienst - Consent löschen - Prüfung category"),
  A_22927("A_22927", "Ausschluss unspezifizierter Extensions"),
  A_23443("A_23443", "Task aktivieren – Flowtype 160/169 - Prüfung Coverage Type"),
  A_22347("A_22347-01", "Task aktivieren – Flowtype 200/209 - Prüfung Coverage Type"),
  A_22350("A_22350", "E-Rezept-Fachdienst - Consent schreiben – Persistieren"),
  A_22351("A_22351", "E-Rezept-Fachdienst - Consent schreiben - FHIR-Validierung"),
  A_23455("A_23455", "E-Rezept-Fachdienst - Prüfung Prüfziffer - keine Prüfziffer"),
  A_23450(
      "A_23450-01",
      "E-Rezept-Fachdienst - Rezepte lesen - Apotheke - VSDM - Prüfung Prüfungsnachweis"),
  A_23451(
      "A_23451-01",
      "E-Rezept-Fachdienst - Rezepte lesen - Apotheke - VSDM - Zeitraum Akzeptanz"
          + " Prüfungsnachweis"),
  A_23452(
      "A_23452-03",
      "E-Rezept-Fachdienst - Rezepte lesen - Apotheke - VSDM - Filter Status, KVNR und"
          + " Workflowtype"),
  A_23454("A_23454", "E-Rezept-Fachdienst - Prüfung Prüfziffer"),
  A_23456(
      "A_23456-01", "E-Rezept-Fachdienst - Prüfung Prüfziffer - Berechnung HMAC der Prüfziffer"),
  A_23878("A_23878", "E-RezeptFachdienst, validierung des Payload des DispenseRequest"),
  A_23879("A_23879", "E-RezeptFachdienst, validierung des Payload des CommunicationReply"),
  A_23888(
      "A_23888",
      "E-Rezept-Fachdienst - Task aktivieren – Überprüfung der IK Nummer im Profil"
          + " KBV_PR_FOR_Coverage, -> payor.identifier.value"),
  A_23890(
      "A_23890",
      "Überprüfung der KVNR in KBV_PR_FOR_Patient - gemäß der Anlage 1 der 'Prüfziffernberechnung"
          + " für die Krankenversichertennummer nach § 290 SGB V' vom 26.02.2019"),

  A_23891(
      "A_23891",
      "E-Rezept-Fachdienst - Task aktivieren – Überprüfung der ANR und ZANR im Profil"
          + " KBV_PR_FOR_Practitioner"),
  A_23892("A_23892", "Überprüfung der PZN-Prüfziffer im Medication_PZN"),
  A_23936(
      "A_23936",
      "E-Rezept-Fachdienst - Task aktivieren - Versicherten-ID als Identifikator von Versicherten"),
  A_24030(
      "A_24030",
      "Validierung der IK Nummer im Profil KBV_PR_FOR_Coverage, bei einem Unfall (BG-Abrechnung) ->"
          + " payor.identifier.extension:alternativeID.value[x]:valueIdentifier"),
  A_24032(
      "A_24032",
      "Auffälligkeit für ANR & ZANR validierung, invalide Prüfziffer bei Konfiguration Fehler"),
  A_24033(
      "A_24033",
      "Auffälligkeit für ANR & ZANR validierung, invalide Prüfziffer bei Konfiguration Warnung"),

  A_24034("A_24034", "Überprüfung der PZN-Prüfziffer im Medication_Compounding"),
  A_24175("A_24175", "Löschen des Owner beim FD nach Reject"),
  A_24176(
      "A_24176",
      "Apotheke - Verordnung abrufen - Prüfung der Telematik-ID in ACCESS_TOKEN und Task.owner"),
  A_24177(
      "A_24177",
      "E-Rezept-Fachdienst - Task abrufen - Apotheke - Verordnung abrufen - Prüfung AccessCode"),
  A_24178(
      "A_24178",
      "E-Rezept-Fachdienst - Task abrufen - Apotheke - Verordnung abrufen - Prüfung Status"
          + " in-progress"),
  A_24285_01(
      "A_24285_01", "E-Rezept-Fachdienst - Dispensierinformationen bereitstellen - Zeitstempel"),
  A_24434(
      "A_24434",
      "E-Rezept-Fachdienst - Handhabung der Rückgabe von mehreren FHIR-Objekten - Betroffene"
          + " Endpunkte"),
  A_24436(
      "A_24436",
      "E-Rezept-Fachdienst - Handhabung der Rückgabe von mehreren FHIR-Objekten - Filter- und"
          + " Sortierkriterien"),
  A_24438(
      "A_24438",
      "E-Rezept-Fachdienst - Handhabung der Rückgabe von mehreren FHIR-Objekten - Sortieren von"
          + " Einträgen"),
  A_24441(
      "A_24441-01",
      "E-Rezept-Fachdienst - Handhabung der Rückgabe von mehreren FHIR-Objekten - URL-Parameter"
          + " für Paginierung"),
  A_24442(
      "A_24442-01",
      "Handhabung der Rückgabe von mehreren FHIR-Objekten - Link Relations für Paginierung"),
  A_24443(
      "A_24443",
      " E-Rezept-Fachdienst - Handhabung der Rückgabe von mehreren FHIR-Objekten - Paginierung"),
  A_24444(
      "A_24444",
      "E-Rezept-Fachdienst - Handhabung der Rückgabe von mehreren FHIR-Objekten - Erhalten von"
          + " URL-Parametern"),
  A_24465("A_24465", "E-Rezept-Fachdienst - Bereitstellung von CA-Zertifikaten"),
  A_24466("A_24466", "E-Rezept-Fachdienst - Bereitstellung von Cross Zertifikaten"),
  A_24467("A_24467", "E-Rezept-Fachdienst - Bereitstellung von OCSP Responses"),
  A_24471(
      "A_24471",
      "E-Rezept-Fachdienst - Abrechnungsinformation bereitstellen - ChargeItem-ID=Rezept-ID"),
  A_24901("A_24901", "E-Rezept-Fachdienst - Task aktivieren - Mehrfachverordnung - Schema ID"),
  A_25057("A_25057-1", "Deprecation Header für die Endpunkte /OCSPList und /CertList"),
  A_25206("A_25206", "E-Rezept-Fachdienst - Rezepte lesen - Apotheke - VSDM - PN3"),
  A_25207(
      "A_25207", "E-Rezept-Fachdienst - Rezepte lesen - Apotheke - VSDM - PN3 - AcceptPN3 false"),
  A_25208("A_25208-01", "E-Rezept-Fachdienst - Rezepte lesen - Apotheke - VSDM - PN3 - URL kvnr"),
  A_25209(
      "A_25209",
      "E-Rezept-Fachdienst - Rezepte lesen - Apotheke - VSDM - PN3 - AcceptPN3 true- Filter Status,"
          + " KVNR und Workflowtype"),
  A_25515(
      "A_25515",
      "E-Rezept-Fachdienst - Handhabung der Rückgabe von mehreren FHIR-Objekten - Filteroperatoren"
          + " für Datumsabfragen"),
  A_25962("A_25962", "E-Rezept-Fachdienst - ePA - Medication Service - Versichertenprotokoll"),
  A_26320(
      "A_26320",
      " E-Rezept-Fachdienst - Nachricht einstellen - Dispense Request - Prüfung Status Task"),
  A_26321(
      "A_26321 ",
      "E-Rezept-Fachdienst - Nachricht einstellen - Dispense Request - Prüfung Ende Gültigkeit"
          + " Task"),
  A_26327(
      "A_26327",
      "E-Rezept-Fachdienst - Nachricht einstellen - Dispense Request - Prüfung Beginn Gültigkeit"
          + " Task"),
  A_26337("A_26337", "E-Rezept-Fachdienst - Task schließen - Zeitstempel MedicationDispense"),
  A_27287("A_27287", "E-Rezept-Fachdienst - Rezepte lesen - Apotheke - VSDM - Vergleich KVNR"),
  A_27346("A_27346", "E-Rezept-Fachdienst - Rezepte lesen - Apotheke - VSDM - URL hcv"),
  A_27347("A_27347", "E-Rezept-Fachdienst - Rezepte lesen - Apotheke - VSDM - Vergleich hcv"),
  A_27446(
      "A_27446",
      "E-Rezept-Fachdienst - Rezepte lesen - Apotheke - Ratelimit pro Telematik-ID prüfen");
  private final Requirement requirement;

  ErpAfos(String id, String description) {
    this.requirement = new Requirement(id, description);
  }

  @Override
  public String toString() {
    return requirement.toString();
  }
}
