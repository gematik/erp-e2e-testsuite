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
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.NonNull;

/**
 * <br>
 * <b>Profile:</b> kbv.basis (1.1.3) <br>
 * <b>File:</b> KBVVSBASEGemRSAnlage8.json <br>
 * <br>
 * <b>Publisher:</b> Kassenärztliche Bundesvereinigung <br>
 * <b>Published:</b> 2020-04-22 <br>
 * <b>Status:</b> active
 */
@Getter
public enum Country implements IValueSet {
  D("D", "Deutschland"),
  AFG("AFG", "Afghanistan"),
  ET("ET", "Ägypten"),
  AL("AL", "Albanien"),
  DZ("DZ", "Algerien"),
  AJ("AJ", "Amerik.-Jungferninseln"),
  AS("AS", "Amerik.-Samoa"),
  AND("AND", "Andorra"),
  AGO("AGO", "Angola"),
  ANG("ANG", "Anguilla"),
  ANT("ANT", "Antigua und Barbuda"),
  AQU("AQU", "Äquatorialguinea"),
  RA("RA", "Argentinien"),
  ARM("ARM", "Armenien"),
  ASE("ASE", "Aserbaidschan"),
  ETH("ETH", "Äthiopien"),
  AUS("AUS", "Australien"),
  BS("BS", "Bahamas"),
  BRN("BRN", "Bahrain"),
  BD("BD", "Bangladesch"),
  BDS("BDS", "Barbados"),
  B("B", "Belgien"),
  BH("BH", "Belize"),
  DY("DY", "Benin"),
  BER("BER", "Bermuda"),
  BHT("BHT", "Bhutan"),
  BOL("BOL", "Bolivien"),
  BIH("BIH", "Bosnien und Herzegowina"),
  RB("RB", "Botsuana"),
  BR("BR", "Brasilien"),
  BJ("BJ", "Brit.-Jungferninseln"),
  BRU("BRU", "Brunei Darussalam"),
  BG("BG", "Bulgarien"),
  HV("HV", "Burkina Faso"),
  RU("RU", "Burundi"),
  RCH("RCH", "Chile"),
  TJ("TJ", "China"),
  COI("COI", "Cookinseln"),
  CR("CR", "Costa Rica"),
  CI("CI", "Cote d ́Ivoire"),
  DK("DK", "Dänemark"),
  WD("WD", "Dominica"),
  DOM("DOM", "Dominikanische Republik"),
  DSC("DSC", "Dschibuti"),
  EC("EC", "Ecuador"),
  ES("ES", "El Salvador"),
  ERI("ERI", "Eritrea"),
  EST("EST", "Estland"),
  FAL("FAL", "Falklandinseln"),
  FR("FR", "Färöer"),
  FJI("FJI", "Fidschi"),
  FIN("FIN", "Finnland"),
  F("F", "Frankreich"),
  FG("FG", "Franz.-Guayana"),
  FP("FP", "Franz.-Polynesien"),
  GAB("GAB", "Gabun"),
  WAG("WAG", "Gambia"),
  GEO("GEO", "Georgien"),
  GH("GH", "Ghana"),
  GIB("GIB", "Gibraltar"),
  WG("WG", "Grenada"),
  GR("GR", "Griechenland"),
  GRO("GRO", "Grönland"),
  GB("GB", "Großbritannien und Nordirland"),
  GUA("GUA", "Guadeloupe"),
  GUM("GUM", "Guam"),
  GCA("GCA", "Guatemala"),
  RG("RG", "Guinea"),
  GUB("GUB", "Guinea-Bissau"),
  GUY("GUY", "Guyana"),
  RH("RH", "Haiti"),
  HCA("HCA", "Honduras"),
  HKG("HKG", "Hongkong"),
  IND("IND", "Indien"),
  RI("RI", "Indonesien"),
  MAN("MAN", "Insel Man"),
  IRQ("IRQ", "Irak"),
  IR("IR", "\"Iran, Islamische Republik\""),
  IRL("IRL", "Irland"),
  IS("IS", "Island"),
  IL("IL", "Israel"),
  I("I", "Italien"),
  JA("JA", "Jamaika"),
  J("J", "Japan"),
  YEM("YEM", "Jemen"),
  JOR("JOR", "Jordanien"),
  YU("YU", "Jugoslawien"),
  KAI("KAI", "Kaimaninseln"),
  K("K", "Kambodscha"),
  CAM("CAM", "Kamerun"),
  CDN("CDN", "Kanada"),
  KAN("KAN", "Kanalinseln"),
  CV("CV", "Kap Verde"),
  KAS("KAS", "Kasachstan"),
  QAT("QAT", "Katar/Qatar"),
  EAK("EAK", "Kenia"),
  KIS("KIS", "Kirgisistan"),
  KIB("KIB", "Kiribati"),
  CO("CO", "Kolumbien"),
  KOM("KOM", "Komoren"),
  RCB("RCB", "Kongo"),
  ZRE("ZRE", "\"Kongo, Dem. Republik\""),
  KOR("KOR", "\"Korea, Dem. Volksrepublik\""),
  ROK("ROK", "\"Korea, Republik\""),
  KOS("KOS", "Kosovo"),
  HR("HR", "Kroatien"),
  C("C", "Kuba"),
  KWT("KWT", "Kuwait"),
  LAO("LAO", "\"Laos, Dem. Volksrepublik\""),
  LS("LS", "Lesotho"),
  LV("LV", "Lettland"),
  RL("RL", "Libanon"),
  LB("LB", "Liberia"),
  LAR("LAR", "Libyen"),
  FL("FL", "Liechtenstein"),
  LT("LT", "Litauen"),
  L("L", "Luxemburg"),
  MAC("MAC", "Macau"),
  RM("RM", "Madagaskar"),
  MK("MK", "Makedonien / Mazedonien"),
  MW("MW", "Malawi"),
  MAL("MAL", "Malaysia"),
  BIO("BIO", "Malediven"),
  RMM("RMM", "Mali"),
  M("M", "Malta"),
  MA("MA", "Marokko"),
  MAR("MAR", "Marshallinseln"),
  MAT("MAT", "Martinique"),
  RIM("RIM", "Mauretanien"),
  MS("MS", "Mauritius"),
  MAY("MAY", "Mayotte"),
  MEX("MEX", "Mexiko"),
  MIK("MIK", "\"Mikronesien, Föderierte Staaten von\""),
  MD("MD", "Moldau"),
  MC("MC", "Monaco"),
  MON("MON", "Mongolei"),
  MNE("MNE", "Montenegro"),
  MOT("MOT", "Montserrat"),
  MOZ("MOZ", "Mosambik"),
  MYA("MYA", "Myanmar"),
  SWA("SWA", "Namibia"),
  NAU("NAU", "Nauru"),
  NEP("NEP", "Nepal"),
  NKA("NKA", "Neukaledonien"),
  NZ("NZ", "Neuseeland"),
  NIC("NIC", "Nicaragua"),
  NL("NL", "Niederlande"),
  NLA("NLA", "Niederländische Antillen"),
  RN("RN", "Niger"),
  WAN("WAN", "Nigeria"),
  NIU("NIU", "Niue"),
  NMA("NMA", "Nördliche Marianen"),
  N("N", "Norwegen"),
  MAO("MAO", "Oman"),
  A("A", "Österreich"),
  PK("PK", "Pakistan"),
  PSE("PSE", "Palästinensische Gebiete"),
  PAL("PAL", "Palau"),
  PA("PA", "Panama"),
  PNG("PNG", "Papua-Neugiunea"),
  PY("PY", "Paraguay"),
  PIN("PIN", "Pazifische Inseln (Marianen- und Karolineninseln)"),
  PE("PE", "Peru"),
  RP("RP", "Philippinen"),
  PIT("PIT", "Pitcairn-Insel"),
  PL("PL", "Polen"),
  P("P", "Portugal"),
  PRI("PRI", "Puerto Rico"),
  REU("REU", "Réunion"),
  RWA("RWA", "Ruanda"),
  RO("RO", "Rumänien"),
  RUS("RUS", "Russische Föderation"),
  PIE("PIE", "Saint Pierre und Miquelon"),
  SOL("SOL", "Salomonen"),
  Z("Z", "Sambia"),
  WS("WS", "Samoa"),
  RSM("RSM", "San Marino"),
  STP("STP", "Sao Tomé und Principe"),
  SAU("SAU", "Saudi-Arabien"),
  S("S", "Schweden"),
  CH("CH", "Schweiz"),
  SN("SN", "Senegal"),
  SRB("SRB", "Serbien (einschl. Kosovo)"),
  SCG("SCG", "Serbien und Montenegro"),
  SY("SY", "Seychellen"),
  WAL("WAL", "Sierra Leone"),
  ZW("ZW", "Simbabwe"),
  SGP("SGP", "Singapur"),
  SK("SK", "Slowakei"),
  SLO("SLO", "Slowenien"),
  SP("SP", "Somalia"),
  E("E", "Spanien"),
  CL("CL", "Sri Lanka"),
  HEL("HEL", "St. Helena einschl. Ascension"),
  SCN("SCN", "St. Kitts und Nevis"),
  WL("WL", "St. Lucia"),
  WV("WV", "St. Vincent und die Grenadinen"),
  ZA("ZA", "Südafrika"),
  SUD("SUD", "Sudan"),
  SDN("SDN", "Republik Sudan (ohne Südsudan)"),
  SSD("SSD", "Südsudan (Republik Südsudan)"),
  SME("SME", "Suriname"),
  SD("SD", "Swasiland"),
  SYR("SYR", "\"Syrien, Arabische Republik\""),
  TAD("TAD", "Tadschikistan"),
  TWN("TWN", "Taiwan"),
  EAT("EAT", "\"Tansania, Vereinigte Republik\""),
  T("T", "Thailand"),
  OTI("OTI", "Timor-Leste"),
  TG("TG", "Togo"),
  TOK("TOK", "Tokelau-Inseln"),
  TON("TON", "Tonga"),
  TT("TT", "Trinidad und Tobago"),
  CHD("CHD", "Tschad"),
  CZ("CZ", "Tschechische Republik"),
  TN("TN", "Tunesien"),
  TR("TR", "Türkei"),
  TUR("TUR", "Turkmenistan"),
  TUC("TUC", "Turks- und Caicosinseln"),
  TUV("TUV", "Tuvalu"),
  EAU("EAU", "Uganda"),
  UA("UA", "Ukraine"),
  H("H", "Ungarn"),
  ROU("ROU", "Uruguay"),
  USB("USB", "Usbekistan"),
  VAN("VAN", "Vanuatu"),
  V("V", "Vatikanstadt"),
  YV("YV", "Venezuela"),
  UAE("UAE", "Vereinigte Arabische Emirate"),
  USA("USA", "Vereinigte Staaten"),
  VN("VN", "Vietnam"),
  BY("BY", "Weißrußland (Belarus)"),
  RCA("RCA", "Zentralafrikanische Republik"),
  CY("CY", "Zypern"),
  ;

  public static final DeBasisCodeSystem CODE_SYSTEM = DeBasisCodeSystem.LAENDERKENNZEICHEN;
  public static final String VERSION = "1.1.3";
  public static final String DESCRIPTION = "Länderkennzeichen nach DEÜV";
  public static final String PUBLISHER = "Kassenärztliche Bundesvereinigung";

  private final String code;
  private final String display;
  private final String definition;

  Country(String code, String display) {
    this(code, display, "N/A definition in profile");
  }

  Country(String code, String display, String definition) {
    this.code = code;
    this.display = display;
    this.definition = definition;
  }

  @Override
  public DeBasisCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static Country fromCode(@NonNull String coding) {
    return Arrays.stream(Country.values())
        .filter(pt -> pt.code.equals(coding))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(Country.class, coding));
  }
}
