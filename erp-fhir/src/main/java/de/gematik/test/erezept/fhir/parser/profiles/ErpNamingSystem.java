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

package de.gematik.test.erezept.fhir.parser.profiles;

import static java.text.MessageFormat.format;

import lombok.Getter;

@Getter
public enum ErpNamingSystem implements IWithSystem {
  PRESCRIPTION_ID("https://gematik.de/fhir/NamingSystem/PrescriptionID"),
  ACCESS_CODE("https://gematik.de/fhir/NamingSystem/AccessCode"),
  SECRET("https://gematik.de/fhir/NamingSystem/Secret"),
  TELEMATIK_ID("https://gematik.de/fhir/NamingSystem/TelematikID"),

  IKNR("http://fhir.de/NamingSystem/arge-ik/iknr"),
  KVID("http://fhir.de/NamingSystem/gkv/kvid-10"),
  ZAHNARZTNUMMER("http://fhir.de/NamingSystem/kzbv/zahnarztnummer"),
  TEAMNUMMER("http://fhir.de/NamingSystem/asv/teamnummer"),
  KZVA_ABRECHNUNGSNUMMER("http://fhir.de/NamingSystem/kzbv/kzvabrechnungsnummer"),

  KBV_PRUEFNUMMER("https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer"),
  KBV_NS_BASE_BSNR("https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR"),
  KBV_NS_BASE_ANR("https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR"),

  ACME_IDS_PATIENT("http://www.acme.com/identifiers/patient");

  private final String canonicalUrl;

  ErpNamingSystem(String url) {
    this.canonicalUrl = url;
  }

  @Override
  public String toString() {
    return format("{0}({1})", this.name(), this.canonicalUrl);
  }
}
