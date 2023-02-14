/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.values;

// nach Thieme
// https://www.thieme.de/viamedici/arzt-im-beruf-weiterbildungs-coach-fachaerzte-1571/a/welche-facharztrichtungen-gibt-es-4563.htm
// 08.12.22 10.30h MEZ

public enum DoctorProfession {
  ALLGEMEINMEDIZIN("Hausarzt:in"),
  ANAESTHESIE(
      "Anästhesist:in",
      "Böse (Chirurgen-)Zungen bezeichnen die Anästhesisten als die Schlafmützen mit der Kaffeetasse"),
  ANATOMIE("Anatom:in", "Anatomie ist die Lehre vom Aufbau des menschlichen Körpers."),
  ARBEITSMEDIZIN(
      "Arbeitsmediziner:in",
      " präventivmedizinische Ärzte und zuständig für die Förderung der Gesundheit der arbeitenden Bevölkerung"),
  AUGENHEILKUNDE("Ophthalmolog:in"),
  BIOCHEMIE(
      "Doktor:in der Biochemie",
      "Die medizinische Biochemie beschäftigt sich mit der organischen Chemie, z.B. Kohlenhydrate, Fette, Nukleinsäuren, oder Proteine und Aminosäuren und deren Stoffwechsel, sowie Erkrankungen die darauf zurückzuführen sind, z.B. die berühmte „Gicht“."),
  CHIRURGIE("Allgemeine Chirurg:in"),
  GEFAESSCHIRURGIE("Gefäßchirurg:in"),
  HANDCHIRURGIE("Handchirurg:in"),
  HERZCHIRURGIE("Herzchirurg:in"),
  KINDERCHIRURGIE("Kinderchirurg:in"),
  ORTHOPAEDIE(
      "Ortopäd:in",
      "Orthopädie heißt „gerades Kind“ – behandelt werden sozusagen eher mechanische Erkrankungen des Stütz- und Bewegungsapparates"),
  PLASTISCHE_CHIRURGIE(
      "Plastischer Chirurg",
      "Rekonstruktion und Wiederherstellung von ungewollt abweichenden Strukturen"),
  PLASTISCHE_CHIRURGIE_IN(
      "Plastische Chirurgin",
      "Rekonstruktion und Wiederherstellung von ungewollt abweichenden Strukturen"),
  AESTHETISCHE_CHIRURGIE(
      "Ästhetischer Chirurg",
      "Rekonstruktion und Wiederherstellung von ungewollt abweichenden Strukturen"),
  THORAXCHIRURGIE(
      "Thorax Chirurg:in",
      "Rekonstruktion und Wiederherstellung von ungewollt abweichenden Strukturen"),
  NEUROCHIRURGIE(
      "Neurochirurgie:in",
      "Operationen des Zentralen Nervensystems inklusive Gefäßen und Hüllen, und mit Operationen des peripheren und vegetativen Nervensystems"),
  MUND_KIEFER_GESICHTSCHIRURGIE("Mund Kiefer Gesichts Chirurg:in"),
  VISZERALCHIRURGIE("Viszeral Chirurg:in", "Chirurgie des Ober- und Unterbauch"),
  FRAUENHEILKUNDE(
      "Gynäkolog:in",
      "Frauenheilkunde_und_Geburtshilfe. Es geht um den Körper und die geschlechtsspezifischen Krankheiten der Frau. Gynäkologen arbeiten eng mit dem Allgemeinarzt zusammen. Ihr Hauptaugenmerk bezieht sich auf die weiblichen Geschlechtsorgane, den Hormonhaushalt (Endokrinologie), dessen Auswirkungen auf Verhütung und Schwangerschaft"),
  HALS_NASEN_OHREN_HEILKUNDE("HNO Arzt:in"),
  HAUT_UND_GESCHLECHTSKRANKHEITEN(
      "Dermatolog:in / Venerolog:in",
      "Der Dermato- und Venerologe beschäftigt sich mit der Haut und deren Erkrankungen."),
  INNERE_MEDIZIN("Internist:in"),
  ANGIOLOGIE("Internist:in", " Gefäßkrankheiten"),
  HUMANGENETIK("Humangenetiker:in"),
  UMWELTMEDIZIN("Umweltmediziner:in"),
  ENDOKRINOLOGIE("Innere_Medizin", "Hormone und Zuckerkrankheit"),
  GASTROENTEROLOGIE("Innere_Medizin", "Magen-Darm-Trakt, also alles, was mit der Verdauung zu tun"),
  // hat
  KARDIOLOGIE("Innere_Medizin", "Herz und Kreislauf"),
  ONKOLOGIE("Innere_Medizin", "Krankheiten der Blutzellen und Tumoren"),
  RHEUMATOLOGIE("Innere_Medizin", "Sammelbezeichnung für Krankheiten des Binde- und Stützgewebes"),
  NEPHROLOGIE("Innere_Medizin", "die Nieren plus Harnwege, jedoch getrennt von der Urologie"),
  KINDER_UND_JUGENDMEDIZIN("Pädiater:in"),
  NEUROLOGIE(
      "Neurolog:in",
      "zentrale und periphere Nervensystem betreffen. In diesem Fach werden jedoch im Gegensatz zur Neurochirurgie keine Operationen durchgeführt."),
  PHARMAKOLOGIE("Pharmakolog:in"),
  PSYCHOLOGIE("Physiolog:in"),
  PSYCHIATRIE("Psychiater:in"),
  RADIOLOGIE("Radiolog:in"),
  UROLOGIE("Urolog:in"),
  PATHOLGIE(
      "Patholog:in",
      "Unter dem Mikroskop untersucht der Pathologie die von den MTAs unterschiedlich gefärbten Gewebsschnitte und diagnostiziert auf diese Weise zum Beispiel Tumoren oder Entzündungen");

  private final String naming;
  private final String description;

  DoctorProfession(String naming) {
    this(naming, "no description");
  }

  DoctorProfession(String naming, String description) {
    this.naming = naming;
    this.description = description;
  }

  public String getNaming() {
    return naming;
  }

  public String getDescription() {
    return description;
  }
}
