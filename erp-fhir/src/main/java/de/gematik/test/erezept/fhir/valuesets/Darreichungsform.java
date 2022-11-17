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
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import java.util.Arrays;
import lombok.Getter;
import lombok.NonNull;

/**
 * kal: Well, couldn't find a proper english translation for that <br>
 * <br>
 * <b>Note:</b> manually generated from <a
 * href="https://applications.kbv.de/S_KBV_DARREICHUNGSFORM_V1.08.xhtml">S_KBV_DARREICHUNGSFORM_V1.08</a>
 * and is not complete yet. Extend on demand!
 */
@Getter
public enum Darreichungsform implements IValueSet {
  AEO("AEO", "Ätherisches Öl"),
  AMP("AMP", "Ampullen"),
  APA("APA", "Ampullenpaare"),
  ASN("ASN", "Augen- und Nasensalbe"),
  ASO("ASO", "Augen- und Ohrensalbe"),
  ATO("ATO", "Augen- und Ohrentropfen"),
  ATR("ATR", "Augentropfen"),
  AUB("AUB", "Augenbad"),
  AUC("AUC", "Augencreme"),
  AUG("AUG", "Augengel"),
  AUS("AUS", "Augensalbe"),
  BAD("BAD", "Bad"),
  BAL("BAL", "Balsam"),
  BAN("BAN", "Bandage"),
  BEU("BEU", "Beutel"),
  BIN("BIN", "Binden"),
  BON("BON", "Bonbons"),
  BPL("BPL", "Basisplatte"),
  BRE("BRE", "Brei"),
  BTA("BTA", "Brausetabletten"),
  CRE("CRE", "Creme"),
  DFL("DFL", "Durchstechflaschen"),
  //  DIG("DIG", "Digitale Gesundheitsanwendungen"),  // not supported yet by IBM
  DIL("DIL", "Dilution"),
  DIS("DIS", "Depot-Injektionssuspension"),
  DKA("DKA", "Dragees in Kalenderpackung"),
  DOS("DOS", "Dosieraerosol"),
  DRA("DRA", "Dragees"),
  DRM("DRM", "Dragees magensaftresistent"),
  DSC("DSC", "Dosierschaum"),
  DSS("DSS", "Dosierspray"),
  EDP("EDP", "Einzeldosis-Pipetten"),
  EIN("EIN", "Einreibung"),
  ELE("ELE", "Elektroden"),
  ELI("ELI", "Elixier"),
  EMU("EMU", "Emulsion"),
  ESS("ESS", "Essenz"),
  ESU("ESU", "Erwachsenen-Suppositorien"),
  EXT("EXT", "Extrakt"),
  FBE("FBE", "Filterbeutel"),
  FBW("FBW", "Franzbranntwein"),
  FDA("FDA", "Filmdragees"),
  FER("FER", "Fertigspritzen"),
  FET("FET", "Fettsalbe"),
  FLA("FLA", "Flasche"),
  FLE("FLE", "Flüssigkeit zum Einnehmen"),
  FLU("FLU", "Flüssig"),
  FMR("FMR", "Filmtabletten magensaftresistent"),
  FOL("FOL", "Folie"),
  FRB("FRB", "Beutel mit retardierten Filmtabletten"),
  FSE("FSE", "Flüssigseife"),
  FTA("FTA", "Filmtabletten"),
  GEK("GEK", "Granulat zur Entnahme aus Kapseln"),
  GEL("GEL", "Gel"),
  GLI("GLI", "Gas und Lösungsmittel zur Herstellung einer Injektions-/Infusionsdispersion"),
  GLO("GLO", "Globuli"),
  GMR("GMR", "Magensaftresistentes Granulat"),
  GPA("GPA", "Gelplatte"),
  GRA("GRA", "Granulat"),
  GSE("GSE", "Granulat zur Herstellung einer Suspension zum Einnehmen"),
  GUL("GUL", "Gurgellösung"),
  HAS("HAS", "Handschuhe"),
  HKM("HKM", "Magensaftresistente Hartkapseln"),
  HKP("HKP", "Hartkapseln"),
  HPI("HPI", "Hartkapseln mit Pulver zur Inhalation"),
  HVW("HVW", "Hartkapseln mit veränderter Wirkstofffreisetzung"),
  IFA("IFA", "Infusionsampullen"),
  IFB("IFB", "Infusionsbeutel"),
  IFD("IFD", "Infusionsdispersion"),
  IFE("IFE", "Injektionslösung in einer Fertigspritze"),
  IFF("IFF", "Infusionsflasche"),
  IFK("IFK", "Infusionslösungskonzentrat"),
  IFL("IFL", "Injektionsflaschen"),
  IFS("IFS", "Infusionsset"),
  IHA("IHA", "Inhalationsampullen"),
  IHP("IHP", "Inhalationspulver"),
  IIL("IIL", "Injektions-, Infusionslösung"),
  IIM("IIM", "Injektionslösung zur intramuskulären Anwendung"),
  IKA("IKA", "Inhalationskapseln"),
  ILO("ILO", "Injektionslösung"),
  IMP("IMP", "Implantat"),
  INF("INF", "Infusionslösung"),
  INH("INH", "Inhalat"),
  INI("INI", "Injektions-, Infusionsflaschen"),
  INL("INL", "Inhalationslösung"),
  INS("INS", "Instant-Tee"),
  IST("IST", "Instillation"),
  ISU("ISU", "Injektionssuspension"),
  IUP("IUP", "Intrauterinpessar"),
  KAN("KAN", "Kanülen"),
  KAP("KAP", "Kapseln"),
  KAT("KAT", "Katheter"),
  KDA("KDA", "Kaudragees"),
  KEG("KEG", "Kegel"),
  KER("KER", "Kerne"),
  KGU("KGU", "Kaugummi"),
  KID("KID", "Konzentrat zur Herstellung einer Infusionsdispersion"),
  KII("KII", "Konzentrat zur Herstellung einer Injektions- oder Infusionslösung"),
  KKS("KKS", "Kleinkinder-Suppositorien"),
  KLI("KLI", "Klistiere"),
  KLT("KLT", "Klistier-Tabletten"),
  KMP("KMP", "Hartkapseln mit magensaftresistent überzogenen Pellets"),
  KMR("KMR", "Kapseln magensaftresistent"),
  KOD("KOD", "Kondome"),
  KOM("KOM", "Kompressen"),
  KON("KON", "Konzentrat"),
  KPG("KPG", "Kombipackung"),
  KRI("KRI", "Kristallsuspension"),
  KSS("KSS", "Kinder- und Säuglings-Suppositorien"),
  KSU("KSU", "Kinder-Suppositorien"),
  KTA("KTA", "Kautabletten"),
  LAN("LAN", "Lanzetten"),
  LII("LII", "Lösung zur Injektion, Infusion und Inhalation"),
  LIQ("LIQ", "Liquidum"),
  LOE("LOE", "Lösung"),
  LOT("LOT", "Lotion"),
  LOV("LOV", "Lösung für einen Vernebler"),
  LSE("LSE", "Lösung zum Einnehmen"),
  LTA("LTA", "Lacktabletten"),
  LUP("LUP", "Lutschpastillen"),
  LUT("LUT", "Lutschtabletten"),
  MIL("MIL", "Milch"),
  MIS("MIS", "Mischung"),
  MIX("MIX", "Mixtur"),
  MRG("MRG", "Magensaftresistentes Retardgranulat"),
  MRP("MRP", "Magensaftresistente Pellets"),
  MTA("MTA", "Manteltabletten"),
  MUW("MUW", "Mundwasser"),
  NAG("NAG", "Nasengel"),
  NAO("NAO", "Nasenöl"),
  NAS("NAS", "Nasenspray"),
  NAW("NAW", "Wirkstoffhaltiger Nagellack"),
  NDS("NDS", "Nasendosierspray"),
  NSA("NSA", "Nasensalbe"),
  NTR("NTR", "Nasentropfen"),

  RET("RET", "Retard-Tabletten"),
  PAM("PAM", "Packungsmasse"),
  PAS("PAS", "Pastillen"),
  PEL("PEL", "Pellets"),
  PEN("PEN", "Injektionslösung in einem Fertigpen"),
  PER("PER", "Perlen"),
  PFL("PFL", "Pflaster"),
  PFT("PFT", "Pflaster transdermal"),
  PHI("PHI", "Pulver zur Herstellung einer Injektions-, Infusions- oder Inhalationslösung"),
  PHV(
      "PHV",
      "Pulver zur Herstellung einer Injektions- bzw. Infusionslösung oder Pulver und Lösungsmittel zur Herstellung einer Lösung zur intravesikalen Anwendung"),
  PIE(
      "PIE",
      "Pulver für ein Konzentrat zur Herstellung einer Infusionslösung, Pulver zur Herstellung einer Lösung zum Einnehmen"),
  PIF(
      "PIF",
      "Pulver für ein Konzentrat zur Herstellung einer Infusionslösung, Pulver zur Herstellung einer Lösung zum Einnehmen"),
  PII("PII", "Pulver zur Herstellung einer Injektions- oder Infusionslösung"),
  PIJ("PIJ", "Pulver zur Herstellung einer Injektionslösung"),
  PIK("PIK", "Pulver zur Herstellung eines Infusionslösungskonzentrates"),
  PIS("PIS", "Pulver zur Herstellung einer Infusionssuspension"),
  PIV(
      "PIV",
      "Pulver zur Herstellung einer Injektions- bzw. Infusionslösung oder einer Lösung zur intravesikalen Anwendung"),
  PKI("PKI", "Pulver für ein Konzentrat zur Herstellung einer Infusionslösung"),
  PLE("PLE", "Pulver zur Herstellung einer Lösung zum Einnehmen"),
  PLF("PLF", "Pulver und Lösungsmittel zur Herstellung einer Infusionslösung"),
  PLG("PLG", "Perlongetten"),
  PLH("PLH", "Pulver und Lösungsmittel zur Herstellung einer Injektions- bzw. Infusionslösung"),
  PLI("PLI", "Pulver und Lösungsmittel zur Herstellung einer Injektionslösung"),
  PLK("PLK", "Pulver und Lösungsmittel für ein Konzentrat zur Herstellung einer Infusionslösung"),
  PLS("PLS", "Pulver und Lösungsmittel zur Herstellung einer Injektionssuspension"),
  PLV("PLV", "Pulver und Lösungsmittel zur Herstellung einer Lösung zur intravesikalen Anwendung"),
  PPL("PPL", "Pumplösung"),
  PRS("PRS", "Presslinge"),
  PSE("PSE", "Pulver zur Herstellung einer Suspension zum Einnehmen"),
  PST("PST", "Paste"),
  PUD("PUD", "Puder"),
  PUL("PUL", "Pulver"),
  SUP("SUP", "Suppositorien"),
  SMT("SMT", "Schmelztabletten"),
  SMU("SMU", "Suppositorien mit Mulleinlage"),
  SPA("SPA", "Spritzampullen"),
  SPF("SPF", "Sprühflasche"),
  SPL("SPL", "Spüllösung"),
  SPR("SPR", "Spray"),
  TAB("TAB", "Tabletten"),
  TAE("TAE", "Täfelchen"),
  TAM("TAM", "Trockenampullen"),
  TEE("TEE", "Tee"),
  TEI("TEI", "Tropfen zum Einnehmen"),
  TES("TES", "Test"),
  TKA("TKA", "Tabletten in Kalenderpackung"),
  TLE("TLE", "Tabletten zur Herstellung einer Lösung zum Einnehmen"),
  TMR("TMR", "Tabletten magensaftresisten"),
  TON("TON", "Tonikum"),
  TPN("TPN", "Tampon"),
  TPO("TPO", "Tamponade"),
  TRA("TRA", "Trinkampullen"),
  TRI("TRI", "Trituration"),
  TRO("TRO", "Tropfen"),
  TRS("TRS", "Trockensubstanz mit Lösungsmittel"),
  TRT("TRT", "Trinktabletten"),
  TSA("TSA", "Trockensaft"),
  TSD("TSD", "Tabletten zur Herstellung einer Suspension zum Einnehmen für einen Dosierspender"),
  TSE("TSE", "Tablette zur Herstellung einer Suspension zum Einnehmen"),
  TSS("TSS", "Trockensubstanz ohne Lösungsmittel"),
  TST("TST", "Teststäbchen"),
  TSY("TSY", "Transdermales System"),
  TTR("TTR", "Teststreifen"),
  TUB("TUB", "Tube"),
  TUE("TUE", "Tücher"),
  TUP("TUP", "Tupfer"),
  TVW("TVW", "Tablette mit veränderter Wirkstofffreisetzung"),
  UTA("UTA", "Überzogene Tabletten"),
  WKA("WKA", "Weichkapseln"),
  WKM("WKM", "Magensaftresistente Weichkapseln"),
  WUE("WUE", "Würfel"),
  ;

  public static final KbvCodeSystem CODE_SYSTEM = KbvCodeSystem.DARREICHUNGSFORM;
  public static final String VERSION = "1.07";
  public static final String DESCRIPTION = "Darreichungsform Arzneimittel/Rezeptur";
  public static final String PUBLISHER = "Kassenärztliche Bundesvereinigung";

  private final String code;
  private final String display;
  private final String definition = "N/A definition in profile";

  Darreichungsform(String code, String display) {
    this.code = code;
    this.display = display;
  }

  @Override
  public KbvCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static Darreichungsform fromCode(@NonNull String coding) {
    return Arrays.stream(Darreichungsform.values())
        .filter(pt -> pt.code.equals(coding))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(Darreichungsform.class, coding));
  }
}
