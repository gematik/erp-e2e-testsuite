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

package de.gematik.test.erezept.eml.fhir.r4.componentbuilder;

import static de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef.INGREDIENT_DARREICHUNGSFORM;
import static de.gematik.test.erezept.eml.fhir.profile.UseFulCodeSystems.UCUM;

import de.gematik.bbriccs.fhir.builder.ElementBuilder;
import de.gematik.bbriccs.fhir.coding.WithCodeSystem;
import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.profile.UseFulCodeSystems;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Ratio;

@RequiredArgsConstructor
public abstract class GematikIngredientComponentBuilder<
        C extends Medication.MedicationIngredientComponent,
        B extends GematikIngredientComponentBuilder<C, B>>
    extends ElementBuilder<C, B> {

  private final C ingredient;

  private final List<Coding> ingredientList = new LinkedList<>();

  protected Ratio strength;
  private String strengthAmountText;
  private WithCodeSystem strengthNumSystem;
  private Extension darreichungsFormFreitext;
  private String ingredItemCodingText;

  public C build() {
    this.ingredientList.forEach(coding -> ingredient.getItemCodeableConcept().addCoding(coding));

    Optional.ofNullable(this.strength).ifPresent(it -> addIngredientValues(it, ingredient));
    Optional.ofNullable(this.ingredItemCodingText)
        .ifPresent(
            ingItemCodingText -> ingredient.getItemCodeableConcept().setText(ingItemCodingText));
    Optional.ofNullable(this.darreichungsFormFreitext).ifPresent(ingredient::addExtension);

    return ingredient;
  }

  private void addIngredientValues(
      Ratio strength, Medication.MedicationIngredientComponent ingredientComponent) {
    Optional.ofNullable(strengthNumSystem)
        .ifPresent(s -> strength.getNumerator().setSystem(s.getCanonicalUrl()));
    Optional.ofNullable(strengthNumSystem)
        .ifPresent(s -> strength.getDenominator().setSystem(s.getCanonicalUrl()));
    Optional.ofNullable(strengthAmountText)
        .ifPresent(s -> strength.addExtension(INGREDIENT_DARREICHUNGSFORM.asStringExtension(s)));
    ingredientComponent.setStrength(strength);
  }

  public B strengthAmountText(String form) {
    this.strengthAmountText = form;
    return self();
  }

  public B ask(ASK ask) {
    this.ingredientList.add(ask.asCoding());
    return self();
  }

  public B snomed(String snomed) {
    this.ingredientList.add(UseFulCodeSystems.SNOMED_SCT.asCoding(snomed));
    return self();
  }

  public B ingredientStrength(Quantity numerator, Quantity demoninator) {
    this.strength = new Ratio().setNumerator(numerator).setDenominator(demoninator);
    return self();
  }

  public B ingredientStrength(Ratio ingredientStrength) {
    this.strength = ingredientStrength;
    return self();
  }

  public B ingredientStrength(int numerator, String numUnit, int denominator, String denomUnit) {
    return ingredientStrength(
        new Quantity(numerator), new Quantity(denominator), numUnit, denomUnit);
  }

  public B ingredientStrength(Quantity numerator, Quantity denominator, String unit) {
    return ingredientStrength(numerator, denominator, unit, unit);
  }

  public B ingredientStrength(
      Quantity numerator, Quantity demoninator, String numUnit, String denomUnit) {
    val ingredStrength = new Ratio().setNumerator(numerator).setDenominator(demoninator);
    ingredStrength.getNumerator().setCode(numUnit).setSystem(UCUM.getCanonicalUrl());
    ingredStrength.getDenominator().setCode(denomUnit).setSystem(UCUM.getCanonicalUrl());
    return ingredientStrength(ingredStrength);
  }

  public B atc(ATC atc) {
    this.ingredientList.add(atc.asCoding());
    return self();
  }

  public B strengthNumSystem() {
    return strengthNumSystem(UCUM);
  }

  public B strengthNumSystem(WithCodeSystem strengthNumSystem) {
    this.strengthNumSystem = strengthNumSystem;
    return self();
  }

  public B darreichungsform(String darreichungsformFreitext) {
    this.darreichungsFormFreitext =
        INGREDIENT_DARREICHUNGSFORM.asStringExtension(darreichungsformFreitext);
    return self();
  }

  public B ingredientCodingText(String ingredItemCodingText) {
    this.ingredItemCodingText = ingredItemCodingText;
    return self();
  }

  public B pzn(PZN pzn, String name) {
    this.ingredientList.add(pzn.asCoding().setDisplay(name));
    return self();
  }

  public B pzn(PZN pzn) {
    return pzn(pzn, null);
  }
}
