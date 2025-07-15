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

import static de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef.MED_INGREDIENT_DOSAGE_FORM_EXT;

import de.gematik.bbriccs.fhir.coding.WithCodeSystem;
import de.gematik.bbriccs.fhir.de.HL7StructDef;
import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem;
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
public class IngredientCodeBuilder {

  private final List<Coding> ingredientList = new LinkedList<>();

  private Ratio ingredientStrength;
  private String darreichungsForm;
  private Boolean refillStrengthAbsentReason = true;
  private WithCodeSystem strengthNumSystem;
  private String itemText;
  private Extension kbvDarreichungsFormFreitext;
  private Extension strengthFreetext;
  private String ingredItemCodingText;

  public static IngredientCodeBuilder builder() {
    return new IngredientCodeBuilder();
  }

  public Medication.MedicationIngredientComponent build() {
    Medication.MedicationIngredientComponent ingredient =
        new Medication.MedicationIngredientComponent();
    this.ingredientList.forEach(coding -> ingredient.getItemCodeableConcept().addCoding(coding));

    Optional.ofNullable(this.ingredientStrength)
        .ifPresent(strength -> fillMissingInIngredientStrength(ingredientStrength, ingredient));
    Optional.ofNullable(this.ingredItemCodingText)
        .ifPresent(
            ingItemCodingText -> ingredient.getItemCodeableConcept().setText(ingItemCodingText));
    Optional.ofNullable(this.kbvDarreichungsFormFreitext).ifPresent(ingredient::addExtension);
    Optional.ofNullable(this.strengthFreetext)
        .ifPresent(freetext -> ingredient.getStrength().addExtension(freetext));
    Optional.ofNullable(this.itemText)
        .ifPresent(txt -> ingredient.getItemCodeableConcept().setText(txt));
    return ingredient;
  }

  private void fillMissingInIngredientStrength(
      Ratio ingredientStrength, Medication.MedicationIngredientComponent ingredientComponent) {
    val absentExt = HL7StructDef.DATA_ABSENT_REASON.asCodeExtension("unknown");

    Optional.ofNullable(strengthNumSystem)
        .ifPresent(s -> ingredientStrength.getNumerator().setSystem(s.getCanonicalUrl()));
    Optional.ofNullable(strengthNumSystem)
        .ifPresent(s -> ingredientStrength.getDenominator().setSystem(s.getCanonicalUrl()));
    if (Boolean.TRUE.equals(refillStrengthAbsentReason)) {
      List.of(ingredientStrength.getNumerator(), ingredientStrength.getDenominator())
          .forEach(
              quantity -> {
                if (!quantity.hasSystem()) {
                  quantity.getSystemElement().addExtension(absentExt);
                }
                if (!quantity.hasCode()) {
                  quantity.getCodeElement().addExtension(absentExt);
                }
                if (!quantity.hasValue()) {
                  quantity.getValueElement().addExtension(absentExt);
                }
              });
    }
    ingredientComponent
        .setStrength(ingredientStrength)
        .addExtension(MED_INGREDIENT_DOSAGE_FORM_EXT.asStringExtension(darreichungsForm));
  }

  public IngredientCodeBuilder darreichungsform(String form) {
    this.darreichungsForm = form;
    return this;
  }

  public IngredientCodeBuilder textInCoding(String text) {
    this.itemText = text;
    return this;
  }

  public IngredientCodeBuilder withAsk(ASK ask) {
    this.ingredientList.add(ask.asCoding());
    return this;
  }

  public IngredientCodeBuilder withSnomed(String snomed) {
    this.ingredientList.add(CommonCodeSystem.SNOMED_SCT.asCoding(snomed));
    return this;
  }

  public IngredientCodeBuilder ingredientStrength(Quantity numerator, Quantity demoninator) {
    this.ingredientStrength = new Ratio().setNumerator(numerator).setDenominator(demoninator);
    return this;
  }

  public IngredientCodeBuilder ingredientStrength(Ratio ingredientStrength) {
    this.ingredientStrength = ingredientStrength;
    return this;
  }

  public IngredientCodeBuilder ingredientStrength(
      Quantity numerator, Quantity demoninator, String code) {
    val ingredStrength = new Ratio().setNumerator(numerator).setDenominator(demoninator);
    ingredStrength.getNumerator().setCode(code);
    ingredStrength.getDenominator().setCode(code);
    return ingredientStrength(ingredStrength);
  }

  public IngredientCodeBuilder withAtc(ATC atc) {
    this.ingredientList.add(atc.asCoding());
    return this;
  }

  public IngredientCodeBuilder withPzn(PZN pzn) {
    this.ingredientList.add(pzn.asCoding());
    return this;
  }

  public IngredientCodeBuilder withPzn(PZN pzn, String name) {
    this.ingredientList.add(pzn.asCoding().setDisplay(name));
    return this;
  }

  public IngredientCodeBuilder dontFillMissingIngredientStrength() {
    this.refillStrengthAbsentReason = false;
    return this;
  }

  public IngredientCodeBuilder withStrengthNumSystem() {
    return withStrengthNumSystem(CommonCodeSystem.UCUM);
  }

  public IngredientCodeBuilder withStrengthNumSystem(WithCodeSystem strengthNumSystem) {
    this.strengthNumSystem = strengthNumSystem;
    return this;
  }

  public IngredientCodeBuilder withStrengthFreetext(String quantityFreetext) {
    this.strengthFreetext =
        KbvItaErpStructDef.MEDICATION_INGREDIENT_AMOUNT.asStringExtension(quantityFreetext);
    return this;
  }

  public IngredientCodeBuilder withKbvDarreichungsform(
      KbvItaErpStructDef system, String darreichungsformFreitext) {
    this.kbvDarreichungsFormFreitext = system.asStringExtension(darreichungsformFreitext);
    return this;
  }

  public void ingredItemCodingText(String ingredItemCodingText) {
    this.ingredItemCodingText = ingredItemCodingText;
  }
}
