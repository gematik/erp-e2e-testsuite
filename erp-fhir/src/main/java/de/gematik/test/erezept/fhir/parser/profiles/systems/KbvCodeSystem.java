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

package de.gematik.test.erezept.fhir.parser.profiles.systems;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.parser.profiles.ICodeSystem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KbvCodeSystem implements ICodeSystem {
  STATUS_CO_PAYMENT("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_StatusCoPayment"),
  STATUS_CO_PAYMENT_FOR(
      "https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_StatusCoPayment"), // the new one!
  FORMULAR_ART("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_FORMULAR_ART"),
  SECTION_TYPE("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type"),
  MEDICATION_CATEGORY("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category"),
  MEDICATION_TYPE("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Type"),
  DARREICHUNGSFORM("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM"),
  QUALIFICATION_TYPE("https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Qualification_Type"),
  BERUFSBEZEICHNUNG("https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Berufsbezeichnung"),
  PERSON_GROUP("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PERSONENGRUPPE"),
  WOP("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP"),
  VERSICHERTEN_STATUS("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS"),
  STATUSKENNZEICHEN("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN"),
  DMP("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DMP"),
  PAYOR_TYPE("https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Payor_Type_KBV"),
  PKV_TARIFF("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PKV_TARIFF"),
  URSACHE_TYPE("https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Ursache_Type"),
  ;

  private final String canonicalUrl;

  @Override
  public String toString() {
    return format("{0}({1})", this.name(), this.canonicalUrl);
  }
}
