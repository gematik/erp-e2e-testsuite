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
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Reference;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class GemErpMedicationCompoundingBuilder
    extends GemErpMedicationBuilder<GemErpMedicationCompoundingBuilder> {

  @Override
  public GemErpMedication build() {

    var medication =
        this.createResource(GemErpMedication::new, ErpWorkflowStructDef.MEDICATION, version);
    applyCommonFields(medication);

    return medication;
  }

  @Override
  public GemErpMedicationCompoundingBuilder manufacturingInstruction(String instruction) {
    this.manufacInstruction = instruction;
    return self();
  }

  public GemErpMedicationCompoundingBuilder packagingSize(String totalQuantity) {
    this.totalQuantity = totalQuantity;
    return self();
  }

  public GemErpMedicationCompoundingBuilder packaging(String packagingInExtensions) {
    this.packaging = packagingInExtensions;
    return self();
  }

  public GemErpMedicationCompoundingBuilder amount(long numerator) {
    return this.amount(numerator, "Stk");
  }

  public GemErpMedicationCompoundingBuilder amount(long numerator, String unit) {
    this.amountNumerator = numerator;
    this.amountNumeratorUnit = unit;
    return self();
  }

  public GemErpMedicationCompoundingBuilder amountDenominator(long denominator) {
    this.amountDenominator = denominator;
    return self();
  }

  public GemErpMedicationCompoundingBuilder codeText(String codeText) {
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
  public GemErpMedicationCompoundingBuilder ingredientComponent(
      Medication.MedicationIngredientComponent ingredientComponent) {
    val pzn = getFirstPzn(List.of(ingredientComponent));
    if (pzn.isPresent()) {
      val pznCoding = pzn.get();
      val epaPznIngreMed =
          EpaMedPznIngredientBuilder.builder()
              .pzn(new CodeableConcept(pznCoding))
              .withoutVersion()
              .build();
      containedResources.add(epaPznIngreMed);

      // in case of MedicationCompounding no other code in an Ingredient component is
      // allowed
      ingredientComponentList.add(
          new Medication.MedicationIngredientComponent(
              new Reference("#" + epaPznIngreMed.getId())));
    } else {
      this.ingredientComponentList.add(ingredientComponent);
    }
    return self();
  }

  public GemErpMedicationCompoundingBuilder formText(String formText) {
    this.formText = formText;
    return self();
  }

  private Optional<Coding> getFirstPzn(
      List<Medication.MedicationIngredientComponent> ingredientComp) {
    return ingredientComp.stream()
        .flatMap(it -> it.getItemCodeableConcept().getCoding().stream())
        .filter(DeBasisProfilCodeSystem.PZN::matches)
        .findFirst();
  }
}
