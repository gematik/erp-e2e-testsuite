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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.fhir.profiles.definitions;

import de.gematik.bbriccs.fhir.coding.WithStructureDefinition;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpWorkflowStructDef implements WithStructureDefinition<ErpWorkflowVersion> {

  // communications,
  COM_INFO_REQ("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_InfoReq"),
  COM_DISP_REQ("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_DispReq"),
  COM_REPLY("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_Reply"),
  GEM_DIGA("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_DiGA"),
  COM_REPRESENTATIVE(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Communication_Representative"),
  SUPPLY_OPTIONS_TYPE(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_SupplyOptionsType"),
  AVAILABILITY_STATUS(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_AvailabilityState"),
  INSURANCE_PROVIDER(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_InsuranceProvider"),
  SUBSTITUTION_ALLOWED(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_SubstitutionAllowedType"),

  PRESCRIPTION_TYPE("https://gematik.de/fhir/StructureDefinition/PrescriptionType"),
  PRESCRIPTION_TYPE_12(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_PrescriptionType"),
  BINARY("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Binary"),
  TASK("https://gematik.de/fhir/StructureDefinition/ErxTask"),
  TASK_12("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Task"),
  MEDICATION("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Medication"),
  MEDICATION_DISPENSE("https://gematik.de/fhir/StructureDefinition/ErxMedicationDispense"),
  MEDICATION_DISPENSE_12(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_MedicationDispense"),
  MEDICATION_DISPENSE_DIGA(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_MedicationDispense_DiGA"),
  CLOSE_OPERATION_BUNDLE(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_CloseOperationInputBundle"),
  GEM_ERP_PR_BUNDLE("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_Bundle"),
  AUDIT_EVENT("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_AuditEvent"),
  EXPIRY_DATE("https://gematik.de/fhir/StructureDefinition/ExpiryDate"),
  EXPIRY_DATE_12("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_ExpiryDate"),
  ACCEPT_DATE("https://gematik.de/fhir/StructureDefinition/AcceptDate"),
  ACCEPT_DATE_12("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_AcceptDate"),
  LAST_MEDICATION_DISPENSE(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_LastMedicationDispense"),
  REDEEM_CODE("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_RedeemCode"),
  DEEP_LINK("https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_DeepLink"),
  CLOSE_OPERATION_INPUT_PARAM(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_PAR_CloseOperation_Input"),
  DISPENSE_OPERATION_INPUT_PARAM(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_PR_PAR_DispenseOperation_Input"),
  ;

  private final String canonicalUrl;
}
