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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.r4.EpaPharmaceuticalProdBuilder;
import de.gematik.test.erezept.eml.fhir.r4.EpaPharmaceuticalProduct;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Reference;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class GemErpMedicationKombiPkgBuilder
    extends GemErpMedicationBuilder<GemErpMedicationKombiPkgBuilder> {

  private List<EpaPharmaceuticalProduct> containedMedicationList = new ArrayList<>();

  @Override
  public GemErpMedication build() {

    var medication =
        this.createResource(GemErpMedication::new, ErpWorkflowStructDef.MEDICATION, version);

    containedMedicationList.forEach(medication::addContained);
    medication.setForm(Darreichungsform.KPG.asCodeableConcept());
    applyCommonFields(medication);

    return medication;
  }

  @Override
  public GemErpMedicationKombiPkgBuilder manufacturingInstruction(String instruction) {
    this.manufacInstruction = instruction;
    return self();
  }

  public GemErpMedicationKombiPkgBuilder containedMedications(
      EpaPharmaceuticalProduct... epaPharmaceuticalProduct) {
    return containedMedications(List.of(epaPharmaceuticalProduct));
  }

  public GemErpMedicationKombiPkgBuilder containedMedications(
      List<EpaPharmaceuticalProduct> epaPharmaceuticalProductList) {
    this.containedMedicationList.addAll(epaPharmaceuticalProductList);
    epaPharmaceuticalProductList.forEach(
        phPr ->
            this.ingredientComponentList.add(
                new Medication.MedicationIngredientComponent(
                    new Reference(format("#{0}", phPr.getId())))));
    return self();
  }

  public GemErpMedicationKombiPkgBuilder containedAtcMedications(List<ATC> atcList) {
    List<EpaPharmaceuticalProduct> medications = new LinkedList<>();
    atcList.forEach(
        atc ->
            medications.add(
                EpaPharmaceuticalProdBuilder.builder().withoutVersion().atcCode(atc).build()));
    containedMedications(medications);
    return self();
  }

  public GemErpMedicationKombiPkgBuilder packagingSize(String packagingSize) {
    this.packagingSize = packagingSize;
    return self();
  }

  public GemErpMedicationKombiPkgBuilder pzn(PZN pzn) {
    this.codes.add(pzn.asCoding());
    return self();
  }

  public GemErpMedicationKombiPkgBuilder packaging(String packagingInExtensions) {
    this.packaging = packagingInExtensions;
    return self();
  }

  public GemErpMedicationKombiPkgBuilder amount(long numerator) {
    return this.amount(numerator, "Stk");
  }

  public GemErpMedicationKombiPkgBuilder amount(long numerator, String unit) {
    this.amountNumerator = numerator;
    this.amountNumeratorUnit = unit;
    return self();
  }

  public GemErpMedicationKombiPkgBuilder amountDenominator(long denominator) {
    this.amountDenominator = denominator;
    return self();
  }

  public GemErpMedicationKombiPkgBuilder codeText(String codeText) {
    this.codeText = codeText;
    return self();
  }

  /**
   * to build the ingredient component please Use IngredientCodeBuilder instead of putting single
   * values into the GemMedicationBuilder
   *
   * @param ingredientComponent
   * @return GemErpMedCompoundingBuilder
   */
  public GemErpMedicationKombiPkgBuilder ingredientComponent(
      Medication.MedicationIngredientComponent ingredientComponent) {
    this.ingredientComponentList.add(ingredientComponent);
    return self();
  }

  public GemErpMedicationKombiPkgBuilder formText(String formText) {
    this.formText = formText;
    return self();
  }
}
