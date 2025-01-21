/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.fhir.parser.profiles.definitions;

import de.gematik.test.erezept.fhir.parser.profiles.IStructureDefinition;
import de.gematik.test.erezept.fhir.parser.profiles.version.EpaMedicationVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EpaMedicationStructDef implements IStructureDefinition<EpaMedicationVersion> {
  MEDICATION_PZN_INGREDIENT(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/epa-medication-pzn-ingredient"),
  MED_INGREDIENT_DOSAGE_FORM_EXT(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/medication-ingredient-darreichungsform-extension"),
  DURG_CATEGORY_EXT(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/drug-category-extension"),
  VACCINE_EXT(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/medication-id-vaccine-extension"),
  PACKAGING_EXTENSION(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/medication-formulation-packaging-extension"),
  TOTAL_QUANTITY_FORMULATION_EXT(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/medication-total-quantity-formulation-extension"),
  MANUFACTURING_INSTRUCTION(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/medication-manufacturing-instructions-extension"),
  ;
  private final String canonicalUrl;
}
