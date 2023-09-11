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


import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;

import java.util.Optional;

/**
 * <a
 * href="https://www.cida.de/wp-content/uploads/sites/2/2020/07/IK_Liste_Krankenkassen.pdf">IK_Liste_Krankenkassen.pdf</a>
 * <a href="https://apm-it.de/iatropro/Benutzerhandbuch/BH/BG-Liste.html">BG-Liste</a>
 */
public enum BGInsuranceCoverageInfo implements InsuranceCoverageInfo {
  BG_BAU_SUED("BG BAU Süd", "81241 München, Am Knie 6", "120991009"),
  BG_BAU("BG Bau", "30519 Hannover, Hildesheimer Str. 3", "120390887"),
  BG_BAU_MITTE("BG Bau Mitte", "51065 Köln, Eulenbergstr. 13-21", "120590925"),
  BG_CHEMIE_HAM("BG Chemie", "22453 Hamburg, Borsteler Chaussee 5", "120290524"),
  BG_CHEMIE_HAL("BG Chemie", "06110 Halle (Saale), Merseburger Str. 52", "121590006"),
  BG_DRUCK("BG Druck u. Papierverarbeitung", "04001 Leipzig,  Gustav-Adolf-Str. 6", "121490027"),
  BG_ETE("BG Energie Textil Elektro (ETE)", "50968 Köln, Gustav-Heinemann-Ufer 130", "120590479"),
  GUVV_OLDENBURG("GUVV Oldenburg", "26122 Oldenburg, Gartenstr.9", "120391786"),
  GUVV_HANNOVER("GUVV Hannover", "30519 Hannover, Am Mittelfelde 169", "120391764"),
  BG_GEM_UNF(
      "BG Gemeindeunfallversicherung", "38102 Braunschweig, Berliner Platz 1 c", "120391742"),
  BGW("BG Gesundheitsdienst und Wohlfahrtspflege", "97070 Würzburg, Röntgenring 2", "120291466"),
  BG_HANDEL_BREMEN(
      "BG Handel und Warendistribution Bremen", "28195 Bremen, Falkenstrasse 7", "120291079"),
  BG_HANDEL_ESSEN(
      "BG Handel und Warendistribution Essen", "45116 ESSEN, HENRIETTENSTR. 2", "120591129"),
  BG_HANDEL_MAINZ(
      "BG Handel und Warendistribution Mainz", "55128 Mainz, Haifa Allee 36", "120791096"),
  BG_HANDEL_MUIMCHEN(
      "BG Handel und Warendistribution München", "80335 München, Linpunstr. 52", "120991167"),
  BG_HANDEL_BERLIN(
      "BG Handel und Warendistribution Berlin", "10715 Berlin, Bundesallee 57/58", "121191069"),
  BG_HANDEL_GERA("BG Handel und Warendistributio Gera", "07545 Gera, Bahnhofstr. 22", "121690041"),
  HOLZ_METALL_HANNOVER(
      "Berufsgenossenschaft Holz und Metall Hannover", "30173 Hannover, Seligmann Allee 4", "120392377"),
  BGHM_DUESSELDORF(
      "BG Holz und Metall Düsseldorf", "40010 Düsseldorf, Kreuzstr. 54", "120590264"),
  BGHM_KOELN("Berufsgenossenschaft Holz und Metall Köln", "50829 Köln, Hugo-Eckener-Str. 20", "120590275"),
  BGHM_MAINZ("Berufsgenossenschaft Holz und Metall Mainz", "55124 Mainz, Isaac-Fulda-Allee 18", "120790391"),
  BGHM_MUENCHEN("Berufsgenossenschaft Holz und Metall München", "81241 München, Am Knie 8", "120990623"),
  BGHM_BERLIN("Berufsgenossenschaft Holz und Metall Berlin", "10825 Berlin, Innsbrucker Strasse", "121590028"),
  BGHM_ERFURT(
      "Berufsgenossenschaft Holz und Metall Erfurt", "99097 Erfurt, Lucas-Chranach-Platz", "121690085"),
  BGHM_NORD(
          "Berufsgenossenschaft Holz und Metall",
          "20149 Hamburg, Rothenbaumchaussee Chaussee 145",
          "120491958"),
  BGHM_SUED("Berufsgenossenschaft Holz und Metall", "70563 Stuttgart, Vollmöllerstr. 11", "120890369"),
  BGHM("Berufsgenossenschaft Holz und Metall", "55124 Mainz, Isaac-Fulda-Allee 18", "120790335"),
  HUETTEN_WALZWERKE("BG Hütten-Walzwerke", "44263 Dortmund, Semerteichstr. 98", "120590208"),
  LAND_FORSTWIRTSCHAFT("BG Land- und Forstwirtschaft", "64289 Darmstadt, BARTNINGSTR.57", "120692791"),
  LANDWIRTSCHAFT_SAARLAND(
      "BG Landwirtschaft Saarland", "66121 Saarbrücken, HEINERSTR. 2-4", "121092811"),
  LANDESUNFALLKASSE_NDR_SACH(
      "BG Landesunfallkasse Niedersachsen", "30519 Hannover, Am Mittelfelde 169", "120391968"),
  LANDESUNFALLKASSE_BAYERN(
      "BG Landesunfallkasse Bayern (KUVB)", "80805 München, Ungererstr. 71", "120991737"),
  LANDWIRTSCHAFT_WUERZBURG(
      "BG Landwirtschaft", "97072 Würzburg, Friedr.Ebert-Ring 33", "120992841"),
  LANDWIRTSCHAFT_MUENCHEN("BG Landwirtschaft", "81673 München, Neumarkter Str. 35", "120992863"),
  LANDWIRTSCHAFT_KARLSRUHE(
      "BG Landwirtschaft Baden", "76135 Karlsruhe, Steinhäuserstr. 14", "120892873"),
  LANDWIRTSCHAFT_KASSEL("BG Landwirtschaftliche", "34121 Kassel, Frankfurterstr. 126", "120692702"),
  LEDERINDUTRIE_MAINZ("BG Lederindustrie", "55127 Mainz, Lortzingstr. 2", "120790723"),
  NAHRUNG_GASTSTAETTEN("BG Nahrungsmittel und Gastgewerbe", "68165 Mannheim, Dynamo-Str. 7-9", "120890780"),
  RCI("BG RCI", "90449 Nürnberg, Südwestpark 2 und 4", "120990075"),
  RCI_BERGBAU_BONN("BG RCI Bergbau", "53175 Bonn, Peter Hensen Str. 1", "120590015"),
  RCI_BERGBAU_SAARBRUECKEN("BG RCI Bergbau", "66119 Saarbrücken, Talstraße 15", "121090056"),
  RCI_HV("BG RCI HV", "69115 Heidelberg, Kurfürsten-Anl. 62", "120892588"),
  STEINBRUCH("BG Steinbruch", "01277 Dresden, Lud.-Hartmann-Str.40", "121490005"),
  POST_TELEKOM("BG Verkehr Post und Telekom", "72072 Tübingen, Europaplatz 2", "120892305"),
  UNFALLKASSE_BERLIN("Unfallkasse Berlin", "12277 Berlin, Culemeyerstr. 2", "121191913"),
  UNFALLKASSE_BRB(
      "BG Unfallkasse Brandenburg", "15236 Frankfurt / Oder, Müllroser Chaussee 7", "121290003"),
  UNFALLKASSE_BREMEN("BG Unfallkasse Bremen", "28217 Bremen, Konsul-Smith Str. 76", "120491754"),
  UNFALLKASSE_BAHN_BUND(
      "BG Unfallkasse Bund und Bahn", "26382 Wilhelmshaven, Weserstr. 47", "120392037"),
  UNFALLKASSE_HESSEN(
      "BG Unfallkasse Hessen", "60486 Frankfurt am Main, Leonardo-da-Vinci 20", "120692198"),
  UNFALLKASSE_MECKLENBURG(
      "Unfallkasse Mecklenburg-Vorpommern", "19053 Schwerin, Wismarsche Str. 199", "121390015"),
  UNFALLKASSE_NRW_RHEINLAND(
      "BG Unfallkasse NRW GUV", "40227 Düsseldorf, Moskauer Str. 18", "120591802"),
  UNFALLKASSE_NORD("BG Unfallkasse Nord", "22083 Hamburg, Spohrstr. 2", "120291934"),
  UNFALLKASSE_NRW_WESTF_LIPPE(
      "BG Unfallkasse Nordrhein-Westf", "48159 Münster, Salzmannstr.156", "120591824"),
  UNFALLKASSE_RHEINLAND_PFALZ(
      "BG Unfallkasse Rhld./Pf.", "56626 Andernach, Oransteinstr. 10", "120791791"),
  UNFALLKASSE_SAARLAND(
      "BG Unfallkasse Saarland", "66125 Saarbrücken, Beethovenstr. 41", "121091843"),
  UNFALLKASSE_SACHSEN_ANHALT(
      "BG Unfallkasse Sachs.-Anhalt", "39261 Zerbst/Anhalt, Käsperstraße 31", "121590039"),
  UNFALLKASSE_SACHSEN("BG Unfallkasse Sachsen", "01662 Meißen, Rosa-Luxemburgstr. 1", "121490118"),
  UNFALLKASSE_THUERINGEN("BG Unfallkasse Thüringen", "99867 Gotha, Humboldtstr. 111", "121690074"),
  UNFALLKASSE_WUERTEMBERG(
      "BG Unfallkasse Württemberg", "70329 Stuttgart, Augsburger Str. 700", "120891838"),
  DGUV_DVUA("BG Verbindungsstelle", "10117 Berlin, Glinkastraße 40", "121192377"),
  VERKEHR_HANNOVER("BG Verkehr Hannover", "30163 Hannover, Walderseestr. 5", "120391321"),
  VERKEHR_WUPPERTAL("BG Verkehr Wuppertal", "42103 Wuppertal, Aue 96", "120591334"),
  VERKEHR_MUENCHEN("BG Verkehr München", "81539 München, Dreisenhofenerstr. 7", "120991350"),
  VERKEHR_DRESDEN("BG Verkehr Dresden", "01187 Dresden, Hofmühlenstr. 4", "121490061"),
  VERKEHR_HAMBURG(
      "BG Verkehr (Fahrzeughaltung)", "22765 Hamburg, Ottenser Hauptstr.54", "120291319"),
  VERKEHR_WIESBADEN(
      "BG Verkehr / Fahrzeughaltung", "65197 Wiesbaden, Wiesbadener Str. 70", "120691346"),
  VERKEHR_BERLIN("BG Verkehr BV Berlin", "10969 Berlin, Axel-Springer-Str. 5", "121191309"),
  VERKEHR_HAMBURG_HV("BG Verkehr HV", "22765 Hamburg, Ottenser Hauptstr. 5", "120291295"),
  VERWALTUNG_MAINZ("BG Verwaltung", "55124 Mainz, Isaac-Fulda-Allee 3", "120791212"),
  VERWALTUNG_ERFURT("BG Verwaltung", "99084 Erfurt, Koenbergkstr. 1", "121690030"),
  ZUCKER("BG Zucker", "55127 Mainz, Lortzingstraße 2", "120390865");

  private final String naming;
  private final String contact;
  private final String iknr;

  BGInsuranceCoverageInfo(String naming, String contact, String iknr) {
    this.naming = naming;
    this.contact = contact;
    this.iknr = iknr;
  }

  @Override
  public String getName() {
    var name = this.naming;
    if (name.length() > 45) return name.substring(0, 45);
    return name;
  }

  public String getContact() {
    return this.contact;
  }

  @Override
  public String getIknr() {
    return this.iknr;
  }

  @Override
  public VersicherungsArtDeBasis getInsuranceType() {
    return VersicherungsArtDeBasis.BG;
  }

  static Optional<BGInsuranceCoverageInfo> getByIknr(String iknr) {
    return InsuranceCoverageInfo.getByIknr(BGInsuranceCoverageInfo.class, iknr);
  }
}
