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
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KbvItaErpStructDef implements WithStructureDefinition<KbvItaErpVersion> {
  BUNDLE("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle"),
  COMPOSITION("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition"),
  PRESCRIPTION("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription"),
  MEDICATION_PZN("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN"),
  MEDICATION_COMPOUNDING(
      "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Compounding"),
  COMPOUNDING_INSTRUCTION(
      "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_CompoundingInstruction"),
  MEDICATION_CATEGORY("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category"),
  MEDICATION_VACCINE("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine"),
  EMERGENCY_SERVICES_FEE("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee"),
  MEDICATION_FREETEXT("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_FreeText"),
  DOSAGE_FLAG("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_DosageFlag"),
  MULTIPLE_PRESCRIPTION("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription"),
  PACKAGING_SIZE("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_PackagingSize"),
  PACKAGING("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Packaging"),
  ACCIDENT("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Accident"),
  SER("https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_SER"),
  MEDICATION_INGREDIENT_FORM(
      "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Ingredient_Form"),
  PRESCRIBER_ID("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Prescriber_ID"),

  @Deprecated(since = "kbv.ita.erp 1.2.0")
  BVG("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG"),
  @Deprecated
  STATUS_CO_PAYMENT("https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment"),
  SUPPLY_REQUEST("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_PracticeSupply"),
  MEDICATION_INGREDIENT_AMOUNT(
      "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Ingredient_Amount"),
  MEDICATION_INGREDIENT("https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_Ingredient"),
  ;

  private final String canonicalUrl;
}
