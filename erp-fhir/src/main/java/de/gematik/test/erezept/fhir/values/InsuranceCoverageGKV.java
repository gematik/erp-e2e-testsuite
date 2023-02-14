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

// nach https://institut-ba.de/service/schluesselverzeichnisse/S_8a_ABRIK_001.PDF 14.12.22//

public enum InsuranceCoverageGKV implements InsuranceCoverage {
  ACTIMONDA(" actimonda krankenkasse", "104127692"),
  AOK_BAD_WUERT("AOK Baden‐Württemberg", "108018007"),
  AOK_BAYERN("AOK Bayern", "108310400"),
  AOK_BREMEN("AOK Bremen / Bremerhaven", "103119199"),
  AOK_NIEDERS("AOK ‐ Die Gesundheitskasse für Niedersachsen", "102114819"),
  AOK_HESSEN("AOK ‐ Die Gesundheitskasse in Hessen", "105313145"),
  AOK_NORDOST("AOK Nordost ‐ Die Gesundheitskasse", "100696012"),
  AOK_NORDWEST("AOK NordWest ‐ Die Gesundheitskasse", "103411401"),
  AOK_PLUS("AOK PLUS ‐ Die Gesundheitskasse für Sachsen und Thüringen", "107299005"),
  AOK_HAMBURG("AOK Rheinland/Hamburg", "104212505"),
  AOK_PFALZ_SAARLAND("AOK Rheinland‐Pfalz/Saarland‐Die Gesundheitskasse", "107310373"),
  AOK_SACHSENANHALT("AOK Sachsen‐Anhalt", "101097008"),
  ATLAS("atlas BKK ahlmann", "103121013"),
  AUDI(" Audi BKK", "108534160"),
  BAHN("BAHN‐BKK", "109938503"),
  BARMER_GEK("BARMER GEK", "104940005"),
  BERTELSMANN("Bertelsmann BKK", "103725342"),
  BIG(" BIG direkt gesund", "103501080"),
  BKK_24("BKK 24", "102122660"),
  BKK_ACHENBACH("BKK Achenbach Buschhütten", "103525909"),
  BKK_ADVITA("BKK advita", "108029306"),
  BKK_AESCULAP("BKK Aesculap", "107536171"),
  BKK_AKZO("BKK Akzo Nobel ‐Bayern‐", "108833355"),
  BKK_ATU("BKK A.T.U", "108591499"),
  BKK_BASEL("BKK Basell", "104626889"),
  BKK_MELSUNG("BKK B. Braun Melsungen AG", "105530422"),
  BKK_BEIERSDORF("BKK Beiersdorf AG", "101532301"),
  BKK_BJB("BKK BJB", "103524101"),
  BKK_BERGISCHE_ACHSEN(" BKK BPW Bergische Achsen KG Wiehl", "104626903"),
  BKK_GILETTE("BKK Braun‐Gillette", "105330157"),
  BKK_DEMAG(" BKK DEMAG KRAUSS‐MAFFEI", "104424794"),
  BKK_PFAFF(" BKK der G. M. PFAFF AG", "106431572"),
  BKK_MTU_FR_HAFEN("BKK der MTU Friedrichshafen GmbH", "107835333"),
  BKK_SIEMAG("BKK der SIEMAG", "103525567"),
  BKK_ENER_THUERINGEN("BKK der Thüringer Energieversorgung", "105928809"),
  BKK_DEUT_BAHN("BKK Deutsche Bank AG", "104224634"),
  BKK_DIAKONIE(" BKK Diakonie", "103724294"),
  BKK_DUERKOPP("BKK DürkoppAdler", "103724249"),
  BKK_ESSANLELLE("BKK ESSANELLE", "104239915"),
  BKK_EUREGIO("BKK EUREGIO", "104125509"),
  BKK_EWE("BKK EWE", "102429648"),
  BKK_EXCLUSIV("BKK exklusiv", "102122557"),
  BKK_FABER("BKK Faber‐Castell und Partner", "109033393"),
  BKK_FIRMUS("BKK firmus", "103121137"),
  BKK_FREUDENBERG("BKK Freudenberg", "107036370"),
  BKK_GILDEMEISTER("BKK Gildemeister SSeidensticker", "103724272"),
  BKK_GLILLO(" BKK GRILLO‐WERKE AG", "104424830"),
  BKK_GROZ("BKK Groz‐Beckert", "107835071"),
  BKK_G_V("BKK G+V", "107835743"),
  BKK_HEIMBACH(" BKK Heimbach", "104124597"),
  BKK_HENSCHEL("BKK HENSCHEL Plus", "105530364"),
  BKK_HERFORD("BKK Herford Minden Ravensberg", "103725547"),
  BKK_HERKULES("BKK Herkules", "105530331"),
  BKK_IHV("BKK IHV", "105830539"),
  BKK_KARL_MAYER(" BKK KARL MAYER", "105330431"),
  BKK_KASSANA("BKK Kassana", "108633433"),
  BKK_KBA("BKK KBA", "108833674"),
  BKK_KEVAG_KOBLENZ("BKK KEVAG KOBLENZ", "106331593"),
  BKK_KRONES("BKK Krones", "108934142"),
  BKK_LINDE("BKK Linde", "105830517"),
  BKK_MAHLE("BKK MAHLE", "108036145"),
  BKK_MEDICUS("BKK MEDICUS", "107923192"),
  BKK_MELITTA("BKK Melitta Plus", "103726081"),
  BKK_MEM("BKK MEM", "106020600"),
  BKK_MERCK("BKK MERCK", "105230076"),
  BKK_MIELE(" BKK Miele", "103725364"),
  BKK_OIL("BKK MOBIL OIL", "101520078"),
  BKK_PFALZ("BKK Pfalz", "106431652"),
  BKK_PHOENIX("BKK PHOENIX", "101520181"),
  BKK_PWC("BKK PricewaterhouseCoopers", "105723301"),
  BKK_PUBLIC("BKK Publik ‐ Partner der BKK Salzgitter", "101931440"),
  BKK_RIEKER("BKK Rieker.Ricosta.Weisser", "107532042"),
  BKK_REWE("BKK RWE", "102131240"),
  BKK_SALZGITTER("BKK Salzgitter", "101922757"),
  BKK_SCHEUFELEN("BKK Scheufelen", "108035576"),
  BKK_SCHLESWIG("BKK Schleswig‐Holstein", "101320043"),
  BKK_SCHWARZWALD("BKK Schwarzwald‐Baar‐Heuberg", "107531187"),
  BKK_BRK("BKK Schwesternschaft vom BRK", "108433099"),
  BKK_AUGSBURG("BKK STADT AUGSBURG", "109132678"),
  BKK_TECHNOFORM("BKK Technoform", "102031410"),
  BKK_TEXTIL_HOF("BKK Textilgruppe Hof", "108632900"),
  BKK_VDN("BKK VDN", "103526615"),
  BKK_VERBUND_PLUS(" BKK VerbundPlus", "107832012"),
  BKK_VERKEHRSBAU("BKK Verkehrsbau Union (VBU)", "109723913"),
  BKK_VICTORIA("BKK VICTORIA ‐ D.A.S.", "104229606"),
  BKK_VITAL("BKK VITAL", "106432038"),
  BKK_VORALB("BKK Voralb HELLER*LEUZE*TRAUB", "108031424"),
  BKK_VORORT("BKK vor Ort", "104526376"),
  BKK_WERRA("BKK Werra‐Meissner", "105530126"),
  BKK_WIELAND("BKK Wieland‐Werke", "107836243"),
  BKK_WIRTSCH("BKK Wirtschaft und Finanzen", "105734543"),
  BKK_WUERTH("BKK Würth", "108036577"),
  BKK_ZF(" BKK ZF und Partner", "107829563"),
  BMW_BKK("BMW BKK", "109034270"),
  BKK_BOSCH("Bosch BKK", "108036123"),
  BKK_BRB("Brandenburgische BKK", "100820488"),
  CONTINENTALE("Continentale Betriebskrankenkasse", "103523440"),
  DAIMLER_BKK("Daimler BKK", "108030775"),
  DAK("DAK‐Gesundheit", "101560000"),
  DEBEKA_BKK("Debeka BKK", "106329225"),
  DEUTSCHE_BKK("Deutsche BKK", "109939003"),
  BERGISCHE_KK("DIE BERGISCHE KRANKENKASSE", "104926702"),
  SCHWEININGER_BKK("Die Schwenninger Betriebskrankenkasse", "107536262"),
  ENERGIE_BKK("energie‐BKK", "102129930"),
  E_ON_BKK("E.ON BKK", "104525057"),
  ERNST_YOUNG_BKK("Ernst und Young BKK", "105732324"),
  ESSO_BKK(" ESSO BKK", "101520329"),
  HANSEATISCHE("Hanseatische Krankenkasse", "101570104"),
  HEAG_BKK("HEAG BKK", "105230101"),
  HEIMAT("Heimat Krankenkasse", "103724238"),
  HKK(" hkk Erste Gesundhei", "103170002"),
  HYPO_BNK_BKK("HypoVereinsbank BKK", "108428980"),
  IKK_B_B("IKK Brandenburg und Berlin", "100602360"),
  IKK_CLASSIC(" IKK classic", "107202793"),
  IKK_GESUNDPLUS(" IKK gesund plus", "101202961"),
  IKK_NORD("IKK Nord", "101300129"),
  IKK_SUEDWEST("IKK Südwest", "109303301"),
  KKH("Kaufmännische Krankenkasse ‐ KKH", "102171012"),
  KNAPPSCHAFT("Knappschaft", "109905003"),
  KK_GARTENBAU(" Krankenkasse für den Gartenbau", "105508890"),
  LKK_BADEN_WUERT("LKK Baden‐Württemberg", "108008880"),
  LKK_FRANKEN(" LKK Franken und Oberbayern", "108608820"),
  LKK_HES_RHEINL_SAARL("LKK Hessen, Rheinland‐Pfalz und Saarland", "105508787"),
  LKK_MITTEL_OST_DEUTSCH(" LKK Mittel‐ und Ostdeutschland", "100609049"),
  LKK_NIEDERB_OBERPF_SCHWA("LKK Niederbayern, Oberpfalz und Schwaben", "109008837"),
  LKK_NIEDERS_BREM("LKK Niedersachsen‐Bremen", "102108731"),
  LKK_NORDR_MUENSTER("LKK Nordrhein‐Westfalen Münster", "103708773"),
  LKK_SCHLESWIG_HAMB("LKK Schleswig‐Holstein und Hamburg", "101308719"),
  MHPLUS_BKK("mhplus Betriebskrankenkasse", "108035612"),
  NOVITAS_BKK(" Novitas BKK", "104491707"),
  PRONOVA_BKK("pronova BKK", "106492393"),
  R_V_BKK("R+V BKK", "105823040"),
  SAINT_GOBAIN_BKK("AINT‐GOBAIN BKK", "104124029"),
  SALUS_BKK("Salus BKK ‐ Die Gutfühlversicherung", "105330168"),
  SECURVITA("SECURVITA BKK", "101320032"),
  SHELL_BKK("Shell BKK/LIFE", "101520147"),
  SBK("Siemens‐Betriebskrankenkasse SBK", "108433248"),
  SDK("SKD BKK", "108833505"),
  SUEDZUCKER_BKK("Südzucker‐BKK", "106936311"),
  TK("Techniker Krankenkasse", "101575519"),
  TUI_BKK(" TUI BKK", "102137985"),
  VAILLANT_BKK("Vaillant BKK", "104926494"),
  VEREINIGTE_BKK("Vereinigte BK", "105330191"),
  WMF_BKK(" WMF BKK", "108036441");

  private final String naming;
  private final String iknr;

  InsuranceCoverageGKV(String naming, String iknr) {
    this.naming = naming;
    this.iknr = iknr;
  }

  public String getNaming() {
    var name = this.naming;
    if (name.length() > 45) return name.substring(0, 45);
    return name;
  }

  public String getIknr() {
    return this.iknr;
  }
}
