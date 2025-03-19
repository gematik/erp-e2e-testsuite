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
 */

package de.gematik.test.erezept.eml.fhir.profile;

import de.gematik.bbriccs.fhir.coding.WithStructureDefinition;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EpaMedicationStructDef implements WithStructureDefinition<EpaMedicationVersion> {
  DRUG_CATEGORY_EXT(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/drug-category-extension"),
  EPA_MEDICATION("https://gematik.de/fhir/epa-medication/StructureDefinition/epa-medication"),
  EPA_MEDICATION_REQUEST(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/epa-medication-request"),
  EPA_MEDICATION_DISPENSE(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/epa-medication-dispense"),
  EPA_OP_PROVIDE_DISPENSATION(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/epa-op-provide-dispensation-erp-input-parameters"),
  EPA_OP_CANCEL_DISPENSATION(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/epa-op-cancel-dispensation-erp-input-parameters"),
  EPA_OP_PROVIDE_PRESCRIPTION(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/epa-op-provide-prescription-erp-input-parameters"),
  EPA_OP_CANCEL_PRESCRIPTION(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/epa-op-cancel-prescription-erp-input-parameters"),
  EPA_MED_TYPE_EXT(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/epa-medication-type-extension"),
  EXT_MED_PACKAGING_SIZE(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/medication-packaging-size-extension"),
  MEDICATION_PZN_INGREDIENT(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/epa-medication-pzn-ingredient"),

  MANUFACTURING_INSTRUCTION(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/medication-manufacturing-instructions-extension"),
  MED_INGREDIENT_DOSAGE_FORM_EXT(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/medication-ingredient-darreichungsform-extension"),
  PACKAGING_EXTENSION(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/medication-formulation-packaging-extension"),
  RX_PRESCRIPTION_ID(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/rx-prescription-process-identifier-extension"),
  TOTAL_QUANTITY_FORMULATION_EXT(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/medication-total-quantity-formulation-extension"),
  VACCINE_EXT(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/medication-id-vaccine-extension"),
  ;

  private final String canonicalUrl;
}
