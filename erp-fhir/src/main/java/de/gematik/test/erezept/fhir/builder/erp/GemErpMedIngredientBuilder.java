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

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.Medication;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class GemErpMedIngredientBuilder
    extends GemErpMedicationBuilder<GemErpMedIngredientBuilder> {

  @Override
  public GemErpMedication build() {
    val medication =
        this.createResource(GemErpMedication::new, ErpWorkflowStructDef.MEDICATION, version);

    applyCommonFields(medication);

    return medication;
  }

  public GemErpMedIngredientBuilder packaging(String totalQuantity) {
    this.totalQuantity = totalQuantity;
    return self();
  }

  public GemErpMedIngredientBuilder normgroesse(StandardSize normSizeCode) {
    this.normSizeCode = normSizeCode;
    return self();
  }

  public GemErpMedIngredientBuilder packagingSize(String packagingSize) {
    this.packagingSize = packagingSize;
    return self();
  }

  public GemErpMedIngredientBuilder amount(long numerator) {
    return this.amount(numerator, "Stk");
  }

  public GemErpMedIngredientBuilder amount(long numerator, String unit) {
    this.amountNumerator = numerator;
    this.amountNumeratorUnit = unit;
    return self();
  }

  public GemErpMedIngredientBuilder amountDenominator(long denominator) {
    this.amountDenominator = denominator;
    return self();
  }

  public GemErpMedIngredientBuilder ingredientComponent(
      Medication.MedicationIngredientComponent ingredientComponent) {
    this.ingredientComponent = ingredientComponent;
    return self();
  }

  public GemErpMedIngredientBuilder formText(String formText) {
    this.formText = formText;
    return self();
  }
}
