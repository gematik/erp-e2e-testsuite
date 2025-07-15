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

import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.test.erezept.eml.fhir.r4.EpaMedPznIngredientBuilder;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Reference;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class GemErpMedCompoundingBuilder
    extends GemErpMedicationBuilder<GemErpMedCompoundingBuilder> {

  @Override
  public GemErpMedication build() {

    var medication =
        this.createResource(GemErpMedication::new, ErpWorkflowStructDef.MEDICATION, version);
    mapPznToContainedMedication();
    applyCommonFields(medication);

    return medication;
  }

  private void mapPznToContainedMedication() {
    // in Case of MedCompounding contains PZN code in IngredientComponent
    // https://wiki.gematik.de/spaces/B714ERPFD/pages/621624735/Transformationsregel+F_017
    if (ingredientComponent != null
        && ingredientComponent.hasItemCodeableConcept()
        && getPznCode(ingredientComponent) != null) {
      var pznCoding = getPznCode(ingredientComponent);
      val epaPznIngreMed =
          EpaMedPznIngredientBuilder.builder().withPzn(new CodeableConcept(pznCoding)).build();
      containedResources = List.of(epaPznIngreMed);

      // in case of MedicationCompounding no other code in an Ingredient component is allowed
      ingredientComponent.setItem(new Reference("#" + epaPznIngreMed.getId()));
    }
  }

  @Override
  public GemErpMedCompoundingBuilder manufacturingInstruction(String instruction) {
    this.manufacInstruction = instruction;
    return self();
  }

  public GemErpMedCompoundingBuilder packagingSize(String totalQuantity) {
    this.totalQuantity = totalQuantity;
    return self();
  }

  public GemErpMedCompoundingBuilder packaging(String packagingInExtensions) {
    this.packaging = packagingInExtensions;
    return self();
  }

  public GemErpMedCompoundingBuilder amount(long numerator) {
    return this.amount(numerator, "Stk");
  }

  public GemErpMedCompoundingBuilder amount(long numerator, String unit) {
    this.amountNumerator = numerator;
    this.amountNumeratorUnit = unit;
    return self();
  }

  public GemErpMedCompoundingBuilder amountDenominator(long denominator) {
    this.amountDenominator = denominator;
    return self();
  }

  public GemErpMedCompoundingBuilder codeText(String codeText) {
    this.codeText = codeText;
    return self();
  }

  /**
   * to build the ingredient component please Use IngredientCodeBuilder instead of this putting
   * single values into the GemMedicationBuilder
   *
   * @param ingredientComponent
   * @return GemErpMedCompoundingBuilder
   */
  public GemErpMedCompoundingBuilder ingredientComponent(
      Medication.MedicationIngredientComponent ingredientComponent) {
    this.ingredientComponent = ingredientComponent;
    return self();
  }

  public GemErpMedCompoundingBuilder formText(String formText) {
    this.formText = formText;
    return self();
  }

  private Coding getPznCode(Medication.MedicationIngredientComponent ingredientComp) {
    return ingredientComp.getItemCodeableConcept().getCoding().stream()
        .filter(DeBasisProfilCodeSystem.PZN::matches)
        .findFirst()
        .orElse(null);
  }
}
