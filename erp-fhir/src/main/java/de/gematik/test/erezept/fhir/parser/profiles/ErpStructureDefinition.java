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

import lombok.Getter;
import org.hl7.fhir.r4.model.CanonicalType;

@Getter
public enum ErpStructureDefinition implements IWithSystem {
  KBV_BUNDLE(StructureDefinitionFixedUrls.KBV_PR_ERP_BUNDLE),
  KBV_PRESCRIPTION(StructureDefinitionFixedUrls.KBV_PR_ERP_PRESCRIPTION),
  KBV_PRACTITIONER(StructureDefinitionFixedUrls.KBV_PR_FOR_PRACTITIONER),
  KBV_ORGANIZATION(StructureDefinitionFixedUrls.KBV_PR_FOR_ORGANIZATION),
  KBV_COVERAGE(StructureDefinitionFixedUrls.KBV_PR_FOR_COVERAGE),
  KBV_FOR_PATIENT(StructureDefinitionFixedUrls.KBV_PR_FOR_PATIENT),
  KBV_COMPOSITION(StructureDefinitionFixedUrls.KBV_PR_ERP_COMPOSITION),
  KBV_EMERGENCY_SERVICES_FEE(StructureDefinitionFixedUrls.KBV_EX_ERP_EMERGENCY_SERVICES_FEE),
  KBV_DOSAGE_FLAG(StructureDefinitionFixedUrls.KBV_EX_ERP_DOSAGE_FLAG),
  KBV_BVG(StructureDefinitionFixedUrls.KBV_EX_ERP_BVG),
  KBV_MULTIPLE_PRESCRIPTION(StructureDefinitionFixedUrls.KBV_EX_ERP_MULTIPLE_PRESCRIPTION),
  KBV_STATUS_CO_PAYMENT(StructureDefinitionFixedUrls.KBV_EX_ERP_STATUS_CO_PAYMENT),
  KBV_MEDICATION_PZN(StructureDefinitionFixedUrls.KBV_PR_ERP_MEDICATION_PZN),
  KBV_MEDICATION_FREETEXT(StructureDefinitionFixedUrls.KBV_PR_ERP_MEDICATION_FREETEXT),
  KBV_MEDICATION_CATEGORY(StructureDefinitionFixedUrls.KBV_EX_ERP_MEDICATION_CATEGORY),
  KBV_MEDICATION_VACCINE(StructureDefinitionFixedUrls.KBV_EX_ERP_MEDICATION_VACCINE),
  KBV_LEGAL_BASIS(StructureDefinitionFixedUrls.KBV_EX_FOR_LEGAL_BASIS),
  KBV_PKV_TARIFF("https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_PKV_Tariff"),

  DAV_ABGABEDATENSATZ(StructureDefinitionFixedUrls.DAV_PKV_PR_ERP_ABGABEDATEN),
  DAV_PKV_PR_ERP_ABGABEDATEN_COMPOSITION(
      StructureDefinitionFixedUrls.DAV_PKV_PR_ERP_ABGABEDATEN_COMPOSITION),
  DAV_PKV_PR_ERP_ABRECHNUNGSZEILEN(StructureDefinitionFixedUrls.DAV_PKV_PR_ERP_ABRECHNUNGSZEILEN),
  DAV_EX_ERP_ABRECHNUNGSZEILEN(
      "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-Abrechnungszeilen"),
  DAV_ABGABEINFORMATIONEN(StructureDefinitionFixedUrls.DAV_PKV_PR_ERP_ABGABEINFORMATIONEN),
  DAV_PKV_PR_ERP_ABGABEINFORMATIONEN(
      StructureDefinitionFixedUrls.DAV_PKV_PR_ERP_ABGABEINFORMATIONEN),
  DAV_INVOICE(StructureDefinitionFixedUrls.DAV_PKV_PR_ERP_ABRECHNUNGSZEILEN),
  DAV_EX_ERP_VAT("http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-MwStSatz"),
  DAV_EX_ERP_CO_PAYMENT(
      "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-Gesamtzuzahlung"),
  DAV_EX_ERP_MWSTSATZ(
      "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-MwStSatz"),
  DAV_EX_ERP_KOSTEN_VERSICHERTER(
      "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-KostenVersicherter"),
  DAV_EX_ERP_GESAMTZUZAHLUNG(
      "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-Gesamtzuzahlung"),
  DAV_EX_ERP_CHARGENBEZEICHNUNG(
      "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-EX-ERP-Chargenbezeichnung"),
  DAV_PKV_PR_ERP_APOTHEKE(StructureDefinitionFixedUrls.DAV_PKV_PR_ERP_APOTHEKE),
  DAV_PKV_EX_ERP_ABRECHNUNGSTYP(
      "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-EX-ERP-AbrechnungsTyp"),

  GKV_PERSON_GROUP(StructureDefinitionFixedUrls.GKV_BESONDERE_PERSONENGRUPPE),
  GKV_DMP_KENNZEICHEN(StructureDefinitionFixedUrls.GKV_DMP_KENNZEICHEN),
  GKV_WOP(StructureDefinitionFixedUrls.GKV_WOP),
  GKV_VERSICHERTENART(StructureDefinitionFixedUrls.GKV_VERSICHERTENART),

  GEM_SUPPLY_OPTIONS_TYPE(StructureDefinitionFixedUrls.GEM_SUPPLY_OPTIONS_TYPE),
  GEM_AVAILABILITY_STATUS(StructureDefinitionFixedUrls.GEM_AVAILABILITY_STATUS),
  GEM_INSURANCE_PROVIDER(StructureDefinitionFixedUrls.GEM_INSURANCE_PROVIDER),
  GEM_SUBSTITION_ALLOWED(StructureDefinitionFixedUrls.GEM_ERX_SUBSTITUTION_ALLOWED),
  GEM_PRESCRIPTION_TYPE(StructureDefinitionFixedUrls.GEM_ERX_PRESCRIPTION_TYPE),
  GEM_BINARY(StructureDefinitionFixedUrls.GEM_ERX_BINARY),
  GEM_ERX_TASK(StructureDefinitionFixedUrls.GEM_ERX_TASK),
  GEM_MEDICATION_DISPENSE(StructureDefinitionFixedUrls.GEM_ERX_MEDICATION_DISPENSE),
  GEM_RECEIPT(StructureDefinitionFixedUrls.GEM_ERX_RECEIPT),
  GEM_CONSENT(StructureDefinitionFixedUrls.GEM_ERX_CONSENT),
  GEM_MARKING_FLAG(StructureDefinitionFixedUrls.GEM_ERX_MARKING_FLAG),
  GEM_CHARGE_ITEM(StructureDefinitionFixedUrls.GEM_ERX_CHARGE_ITEM),
  GEM_COM_INFO_REQ(StructureDefinitionFixedUrls.GEM_ERX_COM_INFO_REQ),
  GEM_COM_DISP_REQ(StructureDefinitionFixedUrls.GEM_ERX_COM_DISP_REQ),
  GEM_COM_REPLY(StructureDefinitionFixedUrls.GEM_ERX_COM_REPLY),
  GEM_COM_REPRESENTATIVE(StructureDefinitionFixedUrls.GEM_ERX_COM_REPRESENTATIVE),
  GEM_COM_CHARGE_CHANGE_REQ(StructureDefinitionFixedUrls.GEM_ERX_COM_CHARGE_CHANGE_REQ),
  GEM_COM_CHARGE_CHANGE_REPLY(StructureDefinitionFixedUrls.GEM_ERX_COM_CHARGE_CHANGE_REPLY),

  HOUSE_NUMBER("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber"),
  STREET_NAME("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName"),
  NORMGROESSE("http://fhir.de/StructureDefinition/normgroesse"),
  HUMAN_OWN_NAME("http://hl7.org/fhir/StructureDefinition/humanname-own-name"),
  HUMAN_OWN_PREFIX("http://hl7.org/fhir/StructureDefinition/humanname-own-prefix"),
  HUMAN_NAMENSZUSATZ("http://fhir.de/StructureDefinition/humanname-namenszusatz"),
  ;

  private final String canonicalUrl;

  ErpStructureDefinition(String url) {
    this.canonicalUrl = url;
  }

  /**
   * Returns the URL and cuts the version if one is available. Note: probably misleading naming as
   * the term "canonical" should be already unversioned
   *
   * @return the canonical URL without trailing a version
   */
  public String getUnversionedUrl() {
    return this.canonicalUrl.split("\\|")[0];
  }

  public CanonicalType asCanonicalType() {
    return new CanonicalType(this.getCanonicalUrl());
  }
}
