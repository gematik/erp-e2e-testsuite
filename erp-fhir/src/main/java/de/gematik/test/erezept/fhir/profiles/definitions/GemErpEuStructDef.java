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
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GemErpEuStructDef implements WithStructureDefinition<EuVersion> {
  CONSENT("https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_Consent"),
  ACCESS_AUTHORIZATION_REQUEST(
      "https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_PAR_Access_Authorization_Request"),
  ACCESS_AUTHORIZATION_RESPONSE(
      "https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_PAR_Access_Authorization_Response"),
  PATCH_TASK_INPUT(
      "https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_PAR_PATCH_Task_Input"),
  PRESCRIPTION_INPUT(
      "https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_PAR_GET_Prescription_Input"),
  NCPEH_COUNTRY_EXT("https://gematik.de/fhir/ti/StructureDefinition/ncpeh-country-extension"),
  EU_ORGANIZATION("https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_Organization"),
  EU_PRACTITIONER_ROLE(
      "https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_PractitionerRole"),
  EU_PRACTITIONER("https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_Practitioner"),
  EU_MEDICATION("https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_Medication"),
  EU_DISPENSATION(
      "https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_MedicationDispense"),
  EU_MED_DSP_CLOSE_INPUT(
      "https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_PAR_CloseOperation_Input"),
  EXT_REDEEMABLE_BY_PROPERTIES(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_EU_IS_REDEEMABLE_BY_PROPERTIES"),
  EXT_REDEEMABLE_BY_PATIENT_AUTHORIZATION(
      "https://gematik.de/fhir/erp/StructureDefinition/GEM_ERP_EX_EU_IS_REDEEMABLE_BY_PATIENT_AUTHORIZATION"),
  ;

  private final String canonicalUrl;
}
