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

import java.util.List;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

@Getter
public enum ErpCodeSystem implements IWithSystem {
  FLOW_TYPE("https://gematik.de/fhir/CodeSystem/Flowtype"),
  AVAILABILITY_STATUS("https://gematik.de/fhir/CodeSystem/AvailabilityStatus"),
  DOCUMENT_TYPE("https://gematik.de/fhir/CodeSystem/Documenttype"),
  CONSENT_TYPE("https://gematik.de/fhir/CodeSystem/Consenttype"),

  PERFORMER_TYPE("urn:ietf:rfc:3986"),
  ORGANIZATION_TYPE("urn:ietf:rfc:3986"),
  UCUM("http://unitsofmeasure.org"), // Unified Code for Units of Measure
  ISO_31662_DE("urn:iso:std:iso:3166-2:de"), // Federal States Germany

  STATUS_CO_PAYMENT("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_StatusCoPayment"),
  FORMULAR_ART("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_FORMULAR_ART"),
  SECTION_TYPE("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type"),
  MEDICATION_CATEGORY("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category"),
  MEDICATION_TYPE("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Type"),
  DARREICHUNGSFORM("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM"),
  QUALIFICATION_TYPE("https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Qualification_Type"),
  PERSON_GROUP("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PERSONENGRUPPE"),
  WOP("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP"),
  VERSICHERTEN_STATUS("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS"),
  STATUSKENNZEICHEN("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN"),
  DMP("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DMP"),
  PAYOR_TYPE("https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Payor_Type_KBV"),

  KBV_PKV_TARIFF("https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PKV_TARIFF"),

  DAV_CS_ERP_INVOICE_TYPE(
      "http://fhir.abda.de/eRezeptAbgabedaten/CodeSystem/DAV-CS-ERP-InvoiceTyp"),
  DAV_CS_ERP_MEDICATIONDISPENSE_TYPE(
      "http://fhir.abda.de/eRezeptAbgabedaten/CodeSystem/DAV-CS-ERP-MedicationDispenseTyp"),
  DAV_PKV_CS_ERP_KOSTEN_VERSICHERTER_KATEGORIE(
      "http://fhir.abda.de/eRezeptAbgabedaten/CodeSystem/DAV-PKV-CS-ERP-KostenVersicherterKategorie"),
  DAV_CS_ERP_ZUSATZATTRIBUTE_GRUPPE(
      "http://fhir.abda.de/eRezeptAbgabedaten/CodeSystem/DAV-CS-ERP-ZusatzattributGruppe"),
  DAV_CS_ERP_ZUSATZATTRIBUTE_FAM_SCHLUESSEL_MARKT(
      "http://fhir.abda.de/eRezeptAbgabedaten/CodeSystem/DAV-CS-ERP-ZusatzattributFAMSchluesselMarkt"),
  DAV_PKV_CS_ERP_ABRECHNUNGSTYP(
      "http://fhir.abda.de/eRezeptAbgabedaten/CodeSystem/DAV-PKV-CS-ERP-AbrechnungsTyp"),
  DAV_CS_ERP_COMPOSITION_TYPES(
      "http://fhir.abda.de/eRezeptAbgabedaten/CodeSystem/DAV-CS-ERP-CompositionTypes"),

  DEUEV_ANLAGE_8_LAENDERKENNZEICHEN("http://fhir.de/CodeSystem/deuev/anlage-8-laenderkennzeichen"),
  VERSICHERUNGSART_DE_BASIS("http://fhir.de/CodeSystem/versicherungsart-de-basis"),
  IDENTIFIER_TYPE_DE_BASIS("http://fhir.de/CodeSystem/identifier-type-de-basis"),
  NORMGROESSE("http://fhir.de/CodeSystem/normgroesse"),
  PZN("http://fhir.de/CodeSystem/ifa/pzn"),

  HL7_V2_0203("http://terminology.hl7.org/CodeSystem/v2-0203"),
  CONSENT_SCOPE("http://terminology.hl7.org/CodeSystem/consentscope"),
  ACT_CODE("http://terminology.hl7.org/CodeSystem/v3-ActCode"),
  DATA_ABSENT("http://terminology.hl7.org/CodeSystem/data-absent-reason"),
  ;

  private final String canonicalUrl;

  ErpCodeSystem(String url) {
    this.canonicalUrl = url;
  }

  public CodeableConcept asCodeableConcept(String code) {
    val coding = new Coding();
    coding.setCode(code);
    coding.setSystem(this.getCanonicalUrl());

    return new CodeableConcept().setCoding(List.of(coding));
  }
}
