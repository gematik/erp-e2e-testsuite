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

package de.gematik.test.erezept.fhir.parser.profiles.definitions;

import de.gematik.test.erezept.fhir.parser.profiles.IStructureDefinition;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpWorkflowStructDef implements IStructureDefinition<ErpWorkflowVersion> {
  SUPPLY_OPTIONS_TYPE("https://gematik.de/fhir/StructureDefinition/SupplyOptionsType"),
  SUPPLY_OPTIONS_TYPE_12(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_SupplyOptionsType"),
  AVAILABILITY_STATUS("https://gematik.de/fhir/StructureDefinition/AvailabilityStateExtension"),
  AVAILABILITY_STATUS_12(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_AvailabilityState"),
  INSURANCE_PROVIDER("https://gematik.de/fhir/StructureDefinition/InsuranceProvider"),
  INSURANCE_PROVIDER_12(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_InsuranceProvider"),
  SUBSTITUTION_ALLOWED("https://gematik.de/fhir/StructureDefinition/SubstitutionAllowedType"),
  SUBSTITUTION_ALLOWED_12(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_SubstitutionAllowedType"),
  PRESCRIPTION_TYPE("https://gematik.de/fhir/StructureDefinition/PrescriptionType"),
  PRESCRIPTION_TYPE_12(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_PrescriptionType"),
  BINARY("https://gematik.de/fhir/StructureDefinition/ErxBinary"),
  BINARY_12("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Binary"),
  TASK("https://gematik.de/fhir/StructureDefinition/ErxTask"),
  TASK_12("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Task"),
  MEDICATION_DISPENSE("https://gematik.de/fhir/StructureDefinition/ErxMedicationDispense"),
  MEDICATION_DISPENSE_12(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_MedicationDispense"),
  RECEIPT("https://gematik.de/fhir/StructureDefinition/ErxReceipt"),
  GEM_ERP_PR_BUNDLE("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Bundle"),

  CONSENT("https://gematik.de/fhir/StructureDefinition/ErxConsent"),
  MARKING_FLAG("https://gematik.de/fhir/StructureDefinition/MarkingFlag"),
  CHARGE_ITEM("https://gematik.de/fhir/StructureDefinition/ErxChargeItem"),
  COM_INFO_REQ("https://gematik.de/fhir/StructureDefinition/ErxCommunicationInfoReq"),
  COM_INFO_REQ_12(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_InfoReq"),
  COM_DISP_REQ("https://gematik.de/fhir/StructureDefinition/ErxCommunicationDispReq"),
  COM_DISP_REQ_12(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_DispReq"),
  COM_REPLY("https://gematik.de/fhir/StructureDefinition/ErxCommunicationReply"),
  COM_REPLY_12("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_Reply"),
  COM_REPRESENTATIVE("https://gematik.de/fhir/StructureDefinition/ErxCommunicationRepresentative"),
  COM_REPRESENTATIVE_12(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_Representative"),
  AUDIT_EVENT("https://gematik.de/fhir/StructureDefinition/ErxAuditEvent"),
  EXPIRY_DATE("https://gematik.de/fhir/StructureDefinition/ExpiryDate"),
  EXPIRY_DATE_12("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_ExpiryDate"),
  ACCEPT_DATE("https://gematik.de/fhir/StructureDefinition/AcceptDate"),
  ACCEPT_DATE_12("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_AcceptDate");

  private final String canonicalUrl;
}
