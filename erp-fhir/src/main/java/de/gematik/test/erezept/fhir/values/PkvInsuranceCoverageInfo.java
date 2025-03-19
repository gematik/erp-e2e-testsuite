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
 */

package de.gematik.test.erezept.fhir.values;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import java.util.Optional;
import lombok.Getter;

/**
 * <a href="https://www.pkv.de/verband/ueber-uns/mitglieder-pkv-verband/">PKV Verband</a> <a
 * href="https://github.com/gematik/api-erp/blob/master/docs/pkv_ik_numbers.adoc">api-erp
 * pkv_ik_numbers.adoc</a>
 */
public enum PkvInsuranceCoverageInfo implements InsuranceCoverageInfo {
  ALLIANZ(
      "Allianz",
      "Private Krankenversicherungs-AG, Postfach 11 30, 85765 Unterföhring, Tel. +49 89 3800-1000,"
          + " Fax +49 800 4400103",
      "168140346"),
  ALTE_OLDENBURGER(
      "ALTE OLDENBURGER",
      "Krankenversicherung AG, Alte-Oldenburger-Platz 1, 49377 Vechta Tel. +49 4441 905-0, Fax +49"
          + " 4441 905-470",
      "168141427"),
  ARAG(
      "ARAG",
      "Krankenversicherungs-AG, Hollerithstraße 11, 81829 München, Tel.  +49 89 4124-8200, Fax  +49"
          + " 89 4124-9525",
      "168141121"),
  AXA(
      "AXA",
      "Krankenversicherung Aktiengesellschaft,  50592 Köln  ,Tel. +49 180 3556622 ,Fax +49 221"
          + " 148-36202",
      "168140950"),
  BARMENIA(
      "Barmenia",
      "Krankenversicherung AG Barmenia-Allee 1, 42119 Wuppertal, Tel. +49 202 438-00, Fax +49 202"
          + " 438-2846",
      "168140426"),
  BAYRISCHE_BEAMTENKASSE(
      "Bayerische Beamtenkrankenkasse",
      "Aktiengesellschaft, Maximilianstraße 53, 81537 München, Tel. +49 89 2160-0, Fax +49 89"
          + " 2160-2714",
      "168141347"),
  CONCORDIA(
      "CONCORDIA",
      "Krankenversicherungs-Aktiengesellschaft, 30621 Hannover, Tel.+49 511 5701-0, Fax +49 511"
          + " 5701-1905",
      "168141187"),
  CONTINENTALE(
      "Continentale",
      "Krankenversicherung a.G., 44118 Dortmund, Tel. +49 231 919-0, Fax +49 231 919-2913",
      "168140017"),
  DEBEKA(
      "Debeka",
      "Krankenversicherungsverein auf Gegenseitigkeit, 56058 Koblenz, Tel. +49 261 498-0, Fax +49"
          + " 261 498-55 55",
      "168140288"),
  DEVK(
      "DEVK",
      "Krankenversicherungs-Aktiengesellschaft, 50729 Köln, Tel. +49 221 757-0, Fax +49 221"
          + " 757-2200",
      "168141314"),
  DKV(
      "DKV",
      "DKV Deutsche Krankenversicherung AG, 50594 Köln, Tel. 0800 374 6444 (gebührenfrei), Fax +49"
          + " 221 578 3694",
      "168140448"),
  FREIE_ARZT_MEDIZINKASSE(
      "FREIE ARZT- UND MEDIZINKASSE",
      "der Angehörigen der Berufsfeuerwehr und der Polizei VVaG, Postfach 11 07 52, 60042 Frankfurt"
          + " / M., Tel. +49 69 97466-0, Fax +49 69 97466-130",
      "168140530"),
  GENERALI(
      "Generali",
      "Deutschland Krankenversicherung AG, 50593 Köln, Tel. +49 221 1636-0, Fax +49 221 1636-200",
      "168140040"),
  GOTHAER(
      "Gothaer",
      "KRANKENVERSICHERUNG AG, 50598 Köln, Tel. +49 221 308-00, Fax +49 221 308-103",
      "168141198"),
  HALLESCHE(
      "HALLESCHE",
      "Krankenversicherung auf Gegenseitigkeit, 70166 Stuttgart, Tel. +49 711 6603-0, Fax +49 711"
          + " 6603-290",
      "168140437"),
  HANSE_MERKUR(
      "HanseMerkur",
      "Krankenversicherung AG, Postfach 13 06 93, 20352 Hamburg, Tel. +49 40 4119-0, Fax +49 40"
          + " 4119-3257",
      "168140186"),
  HUK_COBURG(
      "HUK-COBURG",
      "Krankenversicherung AG, 96447 Coburg, Tel. 0800 2 153-153, Fax 0800 2 153-486",
      "168141176"),
  INTER(
      "INTER",
      "Krankenversicherung AG, 68120 Mannheim, Tel. +49 621 427-427, Fax +49 621 427-944",
      "168141450"),
  KVB(
      "KVB Krankenversorgung der Bundesbahnbeamten",
      "KVB Bezirksleitung Karlsruhe, Südendstraße 44, 76135 Karlsruhe, Telefon (0721) 8243 - 0, Fax"
          + " (0721) 8243 – 159",
      "950686021"),
  KUK(
      "KUK",
      "Krankenunterstützungskasse Hannover (KUK Hannover), Karl-Wiechert-Allee 60 b, 30625"
          + " Hannover, Tel. 0511 912-1680, Fax 0511 912-1682",
      "168141041"),
  LIGA(
      "LIGA",
      "Krankenversicherung katholischer Priester VVaG, Weißenburgstraße 17, 93055 Regensburg, Tel."
          + " +49 941 70 81 84-0, Fax +49 941 70 81 84-79",
      "168140518"),
  LANDESKRANKERNHILFE(
      "Landeskrankenhilfe V.V.a.G.",
      "Landeskrankenhilfe V.V.a.G., 21332 Lüneburg, Tel. +49 4131 725-0, Fax +49 4131 403402",
      "168140119"),
  LVM(
      "LVM",
      "Krankenversicherungs-AG, 48126 Münster, Tel. +49 251 702-0, Fax +49 251 702-1099v",
      "168141096"),
  MECKLENBURGISCHE(
      "Mecklenburgische",
      "Krankenversicherungs-AG, 30619 Hannover, Tel. +49 511 5351-0, Fax +49 511 5351-444",
      "168141416"),
  MUENCHENER(
      "Münchener Verein Versicherungsgruppe",
      "Versicherungsgruppe, 80283 München, Tel. +49 89 5152-0, Fax +49 89 5152-1501",
      "168140379"),
  NUERNBERGER(
      "NÜRNBERGER Krankenversicherung AG",
      "Krankenversicherung AG, 90334 Nürnberg, Tel. +49 911 531-0, Fax +49 911 531-3206",
      "168141256"),
  OTTONOVA(
      "ottonova Krankenversicherung AG",
      "Krankenversicherung AG, Ottostraße 4, 80333 München, Tel. +49 89 12 14 07 12",
      "168141461"),
  POST(
      "PBeaKK Postbeamtenkrankenkasse",
      "Körperschaft des öffentlichen Rechts, Hauptverwaltung, Nauheimer Straße 98, <2ws1270372"
          + " Stuttgart",
      "950585030"),
  PROVINCZIAL(
      "Provinzial",
      "Krankenversicherung Hannover AG, 30140 Hannover, Tel. +49 511 362-0, Fax +49 511 362-2960",
      "168141358"),
  R_V(
      "R + V Krankenversicherung AG",
      "Krankenversicherung AG, 65181 Wiesbaden, Tel. +49 611 533-0, Fax +49 611 533-4500",
      "168141165"),
  SIGNAL_IDUNA(
      "SIGNAL IDUNA Krankenversicherung a.G.",
      "Krankenversicherung a.G., 44121 Dortmund, Tel. +49 231 135-0, Fax +49 231 135-4638",
      "168140028"),

  SDK(
      "SDK",
      "Süddeutsche Krankenversicherung a.G., Postfach 19 23, 70709 Fellbach, Tel. +49 711"
          + " 7372-7777, Fax +49 711 7372-7788",
      "168140391"),
  UKV(
      "Union Krankenversicherung AG",
      "Krankenversicherung AG, 66099 Saarbrücken, Tel.+49 681 844-7000, Fax +49 681 844-2509",
      "168141085"),
  UNIVERSA(
      "uniVersa Krankenversicherung a.G.",
      "Krankenversicherung a.G., 90333 Nürnberg, Tel. +49 911 5307-0, Fax +49 911 5307-1676",
      "168140459"),
  VRK(
      "Versicherer im Raum der Kirchen Krankenversicherung AG",
      "Krankenversicherung AG, Doktorweg 2–4, 32756 Detmold, Tel. +49 800 21 53 45 6, Fax +49 800"
          + " 28 75 18 2",
      "168141438"),
  VIGO(
      "Vigo Krankenversicherung VVaG",
      "Krankenversicherung VVaG, Werdener Straße 4, 40227 Düsseldorf, Tel. +49 211 355900-0, Fax"
          + " +49 211 355900-20",
      "168141154"),
  WUERTHEMBERGISCHE(
      "Württembergische",
      "Krankenversicherung AG, 70163 Stuttgart, Tel. +49 711 662-0, Fax +49 711 662-2520",
      "168141392");

  private final String name;
  @Getter private final String contact;
  @Getter private final String iknr;
  @Getter private final InsuranceTypeDe insuranceType = InsuranceTypeDe.PKV;

  PkvInsuranceCoverageInfo(String name, String contact, String iknr) {
    this.name = name;
    this.contact = contact;
    this.iknr = iknr;
  }

  public String getName() {
    return InsuranceCoverageInfo.shortenName(this.name);
  }

  static Optional<PkvInsuranceCoverageInfo> getByIknr(String iknr) {
    return InsuranceCoverageInfo.getByIknr(PkvInsuranceCoverageInfo.class, iknr);
  }
}
