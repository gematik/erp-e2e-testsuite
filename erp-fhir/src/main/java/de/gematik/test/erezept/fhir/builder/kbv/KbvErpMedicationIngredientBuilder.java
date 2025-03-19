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

package de.gematik.test.erezept.fhir.builder.kbv;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.MedicationType;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Ratio;

public class KbvErpMedicationIngredientBuilder
    extends ResourceBuilder<KbvErpMedication, KbvErpMedicationIngredientBuilder> {

  private static final long MINIMUM_OF_ONE = 1;
  private final List<Extension> extensions = new LinkedList<>();
  private String amountNumerator;
  private String amountNumeratorUnit;
  private long amountDenominator = MINIMUM_OF_ONE;
  private KbvItaErpVersion kbvItaErpVersion = KbvItaErpVersion.getDefaultVersion();
  private MedicationCategory category = MedicationCategory.C_00;
  private boolean isVaccine = false;
  private String darreichungsform;
  private Medication.MedicationIngredientComponent ingredient;
  private long ingredientNumerator = 1;
  private long ingredientDenominator = 1;
  private String ingredientUnit = "eier";
  private StandardSize standardSize = StandardSize.N1;
  private String wirkstoffName;

  public static KbvErpMedicationIngredientBuilder builder() {
    return new KbvErpMedicationIngredientBuilder();
  }

  public KbvErpMedicationIngredientBuilder version(KbvItaErpVersion kbvItaErpVersion) {
    this.kbvItaErpVersion = kbvItaErpVersion;
    return self();
  }

  public KbvErpMedicationIngredientBuilder category(MedicationCategory category) {
    this.category = category;
    return self();
  }

  public KbvErpMedicationIngredientBuilder isVaccine(boolean isVaccine) {
    this.isVaccine = isVaccine;
    return self();
  }

  public KbvErpMedicationIngredientBuilder amount(String numerator) {
    return this.amount(numerator, "stk");
  }

  public KbvErpMedicationIngredientBuilder amount(String numerator, String unit) {
    return amount(numerator, MINIMUM_OF_ONE, unit);
  }

  public KbvErpMedicationIngredientBuilder darreichungsform(String df) {
    this.darreichungsform = df;
    return self();
  }

  public KbvErpMedicationIngredientBuilder ingredientComponent(String ingredientUnit) {
    return ingredientComponent(1, 1, ingredientUnit);
  }

  public KbvErpMedicationIngredientBuilder ingredientComponent(
      long numerator, long deNom, String unit) {
    this.ingredientUnit = unit;
    this.ingredientNumerator = numerator;
    this.ingredientDenominator = deNom;
    return self();
  }

  /**
   * setup the optional amount-Component. !!! denominator has a fixed value of 1 !!!
   *
   * @param numerator as String
   * @param denominator as long
   * @param unit
   * @return KbvErpMedicationIngredientBuilder
   */
  public KbvErpMedicationIngredientBuilder amount(String numerator, long denominator, String unit) {
    this.amountNumerator = numerator;
    this.amountNumeratorUnit = unit;
    this.amountDenominator = denominator;
    return self();
  }

  public KbvErpMedicationIngredientBuilder normGroesse(StandardSize standardSize) {
    this.standardSize = standardSize;
    return self();
  }

  public KbvErpMedicationIngredientBuilder drugName(String name) {
    this.wirkstoffName = name;
    return self();
  }

  @Override
  public KbvErpMedication build() {
    simpleMedicationIngredientBuilder();
    checkRequired();
    val medication =
        this.createResource(
            KbvErpMedication::new, KbvItaErpStructDef.MEDICATION_INGREDIENT, kbvItaErpVersion);

    if (amountNumerator != null) {
      val amount = new Ratio();
      amount
          .getNumerator()
          .addExtension(KbvItaErpStructDef.PACKAGING_SIZE.asStringExtension(amountNumerator));
      amount.getNumerator().setUnit(amountNumeratorUnit);
      amount.getDenominator().setValue(amountDenominator);
      medication.setAmount(amount);
    }
    extensions.add(category.asExtension());
    extensions.add(KbvItaErpStructDef.MEDICATION_VACCINE.asBooleanExtension(isVaccine));
    extensions.add(standardSize.asExtension());
    medication.setExtension(extensions);
    medication
        .setCode(MedicationType.INGREDIENT.asCodeableConcept())
        .setForm(new CodeableConcept().setText(darreichungsform));
    medication.addIngredient(ingredient);
    return medication;
  }

  private void simpleMedicationIngredientBuilder() {
    val ratio = new Ratio();
    ratio.getNumerator().setValue(ingredientNumerator).setUnit(ingredientUnit);
    ratio.getDenominator().setValue(ingredientDenominator);
    ingredient =
        new Medication.MedicationIngredientComponent()
            .setStrength(ratio)
            .setItem(asNamedCodeable(wirkstoffName));
  }

  private CodeableConcept asNamedCodeable(String drugName) {
    // https://www.bfarm.de/DE/Arzneimittel/Arzneimittelinformationen/Arzneimittel-recherchieren/AMIce/Datenbankinformation-AMIce-Arzneimittel/_node.html
    val codeable = DeBasisProfilCodeSystem.ASK.asCodeableConcept("13374").setText(drugName);
    codeable.setText(drugName);
    return codeable;
  }

  private void checkRequired() {
    val address = " https://simplifier.net/erezept/kbvprerpmedicationingredient";
    this.checkRequired(
        wirkstoffName,
        "Medication_Ingredient need \"Wirkstoffname\" in"
            + " Medication.ingredient.item[x]:itemCodeableConcept.text href:"
            + address);
    this.checkRequired(
        darreichungsform,
        "Medication_Ingredient Ressource need a text as \"Darreichungsform\" for Form-Object href:"
            + address);
    this.checkRequired(
        ingredientDenominator,
        "Medication_Ingredient Ressource need min one ingredientDenominator Object href:"
            + address);
    this.checkRequired(
        ingredientNumerator,
        "Medication_Ingredient Ressource need min one ingredientNumerator Object href:" + address);
  }
}
