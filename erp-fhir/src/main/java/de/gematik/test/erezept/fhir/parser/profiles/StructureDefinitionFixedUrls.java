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

/**
 * This class serves a single point of truth for constant URLs within the FHIR realm.
 *
 * <p><em>Note:</em><br>
 * These constants are intended to be used by Enumerations and within Annotations only. Dont' use
 * these constants directly within the "user-space" but rather prefer using according Enumerations
 * which make use of these constants.
 *
 * <p><em>Note2:</em><br>
 * These constants are required here because these are also required within @ResourceDef annotations
 * which won't accept for example ErpStructureDefinition.PRESCRIPTION_TYPE.getCanonicalUrl()
 */
public class StructureDefinitionFixedUrls {

  private StructureDefinitionFixedUrls() {
    // hide constructor for static class
    throw new AssertionError();
  }

  /* KBV Namespace */
  public static final String KBV_PR_ERP_BUNDLE =
      "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.2";
  public static final String KBV_PR_ERP_PRESCRIPTION =
      "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.0.2";
  public static final String KBV_PR_ERP_COMPOSITION =
      "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.0.2";
  public static final String KBV_PR_ERP_MEDICATION_PZN =
      "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.2";
  public static final String KBV_PR_ERP_MEDICATION_FREETEXT =
      "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText|1.0.2";

  public static final String KBV_PR_FOR_PRACTITIONER =
      "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3";
  public static final String KBV_PR_FOR_PATIENT =
      "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3";
  public static final String KBV_PR_FOR_ORGANIZATION =
      "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.0.3";
  public static final String KBV_PR_FOR_COVERAGE =
      "https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3";

  public static final String KBV_PR_BASE_PATIENT =
      "https://fhir.kbv.de/StructureDefinition/KBV_PR_Base_Patient|1.1.3";

  public static final String KBV_EX_ERP_DOSAGE_FLAG =
      "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag";
  public static final String KBV_EX_ERP_EMERGENCY_SERVICES_FEE =
      "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee";
  public static final String KBV_EX_ERP_STATUS_CO_PAYMENT =
      "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment";
  public static final String KBV_EX_ERP_BVG =
      "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG";
  public static final String KBV_EX_ERP_MULTIPLE_PRESCRIPTION =
      "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription";
  public static final String KBV_EX_ERP_MEDICATION_CATEGORY =
      "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category";
  public static final String KBV_EX_ERP_MEDICATION_VACCINE =
      "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine";
  public static final String KBV_EX_FOR_LEGAL_BASIS =
      "https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis";

  public static final String GKV_BESONDERE_PERSONENGRUPPE =
      "http://fhir.de/StructureDefinition/gkv/besondere-personengruppe";
  public static final String GKV_DMP_KENNZEICHEN =
      "http://fhir.de/StructureDefinition/gkv/dmp-kennzeichen";
  public static final String GKV_WOP = "http://fhir.de/StructureDefinition/gkv/wop";
  public static final String GKV_VERSICHERTENART =
      "http://fhir.de/StructureDefinition/gkv/versichertenart";

  /* gematik Namespace */
  public static final String GEM_SUPPLY_OPTIONS_TYPE =
      "https://gematik.de/fhir/StructureDefinition/SupplyOptionsType";
  public static final String GEM_AVAILABILITY_STATUS =
      "https://gematik.de/fhir/StructureDefinition/AvailabilityStatus";
  public static final String GEM_INSURANCE_PROVIDER =
      "https://gematik.de/fhir/StructureDefinition/InsuranceProvider";
  public static final String GEM_ERX_AUDITEVENT =
      "https://gematik.de/fhir/StructureDefinition/ErxAuditEvent";
  public static final String GEM_ERX_TASK = "https://gematik.de/fhir/StructureDefinition/ErxTask";
  public static final String GEM_ERX_PRESCRIPTION_TYPE =
      "https://gematik.de/fhir/StructureDefinition/PrescriptionType";
  public static final String GEM_ERX_BINARY =
      "https://gematik.de/fhir/StructureDefinition/ErxBinary";
  public static final String GEM_ERX_MEDICATION_DISPENSE =
      "https://gematik.de/fhir/StructureDefinition/ErxMedicationDispense|1.1.1";
  public static final String GEM_ERX_RECEIPT =
      "https://gematik.de/fhir/StructureDefinition/ErxReceipt";
  public static final String GEM_ERX_CONSENT =
      "https://gematik.de/fhir/StructureDefinition/ErxConsent";
  public static final String GEM_ERX_MARKING_FLAG =
      "https://gematik.de/fhir/StructureDefinition/MarkingFlag";
  public static final String GEM_ERX_SUBSTITUTION_ALLOWED =
      "https://gematik.de/fhir/StructureDefinition/SubstitutionAllowedType";
  public static final String GEM_ERX_CHARGE_ITEM =
      "https://gematik.de/fhir/StructureDefinition/ErxChargeItem";
  public static final String GEM_ERX_COM_DISP_REQ =
      "https://gematik.de/fhir/StructureDefinition/ErxCommunicationDispReq|1.1.1";
  public static final String GEM_ERX_COM_INFO_REQ =
      "https://gematik.de/fhir/StructureDefinition/ErxCommunicationInfoReq|1.1.1";
  public static final String GEM_ERX_COM_REPLY =
      "https://gematik.de/fhir/StructureDefinition/ErxCommunicationReply|1.1.1";
  public static final String GEM_ERX_COM_REPRESENTATIVE =
      "https://gematik.de/fhir/StructureDefinition/ErxCommunicationRepresentative|1.1.1";
  public static final String GEM_ERX_COM_CHARGE_CHANGE_REQ =
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_ChargChangeReq|1.2";
  public static final String GEM_ERX_COM_CHARGE_CHANGE_REPLY =
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_ChargChangeReply|1.2";

  public static final String DAV_PKV_PR_ERP_ABGABEDATEN =
      "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-AbgabedatenBundle|1.1";
  public static final String DAV_PKV_PR_ERP_ABGABEINFORMATIONEN =
      "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-Abgabeinformationen|1.1";
  public static final String DAV_PKV_PR_ERP_ABRECHNUNGSZEILEN =
      "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-Abrechnungszeilen|1.1";
  public static final String DAV_PKV_PR_ERP_APOTHEKE =
      "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-Apotheke|1.1";
  public static final String DAV_PKV_PR_ERP_ABGABEDATEN_COMPOSITION =
      "http://fhir.abda.de/eRezeptAbgabedaten/StructureDefinition/DAV-PKV-PR-ERP-AbgabedatenComposition|1.1";
}
