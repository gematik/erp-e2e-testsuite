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

package de.gematik.test.erezept.fhir.parser.profiles.definitions;

import de.gematik.test.erezept.fhir.parser.profiles.IStructureDefinition;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KbvItaErpStructDef implements IStructureDefinition<KbvItaErpVersion> {
  BUNDLE("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle"),
  COMPOSITION("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition"),
  PRESCRIPTION("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription"),
  MEDICATION_PZN("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN"),
  MEDICATION_CATEGORY("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category"),
  MEDICATION_VACCINE("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine"),
  EMERGENCY_SERVICES_FEE("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee"),
  MEDICATION_FREETEXT("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText"),
  DOSAGE_FLAG("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag"),
  BVG("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG"),
  MULTIPLE_PRESCRIPTION("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription"),
  PACKAGING_SIZE("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_PackagingSize"),
  @Deprecated(
      since = "kbv.ita.erp-1.1.0") // from version 1.1.0 on ACCIDENT moved to KbvItaForStructDef
  ACCIDENT("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Accident"),
  @Deprecated(since = "kbv.ita.erp-1.1.0") // from version 1.1.0 on STATUS_CO_PAYMENT moved to
  // KbvItaForStructDef
  STATUS_CO_PAYMENT("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment"),
  ;

  private final String canonicalUrl;
}
