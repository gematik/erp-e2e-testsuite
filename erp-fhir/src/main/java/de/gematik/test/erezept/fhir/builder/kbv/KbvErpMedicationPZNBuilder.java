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

package de.gematik.test.erezept.fhir.builder.kbv;

import com.google.common.base.Strings;
import de.gematik.bbriccs.fhir.de.HL7StructDef;
import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.BaseMedicationType;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Ratio;

public class KbvErpMedicationPZNBuilder
    extends KbvErpMedicationBaseBuilder<KbvErpMedicationPZNBuilder> {

  private BaseMedicationType baseMedicationType = BaseMedicationType.MEDICAL_PRODUCT;

  private StandardSize normgroesse = StandardSize.NB;
  private Darreichungsform darreichungsform = Darreichungsform.TAB;
  private PZN pzn;
  private String medicationName;
  private String ingredientText;
  private BigDecimal ingredientStrengthNumerator;
  private String ingredientStrengthNumUnit;
  private String packagingSize;
  private long amountNumerator;
  private String amountNumeratorUnit;
  private String ingredientStrengthDenomUnit;
  private BigDecimal ingredientStrengthDenominator;
  private CodeableConcept ingredientItemItemCC;
  private final Extension absentExt = HL7StructDef.DATA_ABSENT_REASON.asCodeExtension("unknown");

  public static KbvErpMedicationPZNBuilder builder() {
    return new KbvErpMedicationPZNBuilder();
  }

  public KbvErpMedicationPZNBuilder type(BaseMedicationType type) {
    this.baseMedicationType = type;
    return this;
  }

  public KbvErpMedicationPZNBuilder ingredientItemCC(ASK ask) {
    this.ingredientItemItemCC = ask.asCodeableConcept();
    return this;
  }

  public KbvErpMedicationPZNBuilder normgroesse(StandardSize size) {
    this.normgroesse = size;
    return this;
  }

  public KbvErpMedicationPZNBuilder ingredientStrengthDenom(
      double denomValue, String denominatorUnit) {
    this.ingredientStrengthDenominator = new BigDecimal(String.valueOf(denomValue));
    this.ingredientStrengthDenomUnit = denominatorUnit;
    return this;
  }

  public KbvErpMedicationPZNBuilder ingredientStrengthNum(double num, String unit) {
    // it is important to use String.valueOf() because you can produce floating point calculation
    // error without
    this.ingredientStrengthNumerator = new BigDecimal(String.valueOf(num));
    this.ingredientStrengthNumUnit = unit;
    return this;
  }

  public KbvErpMedicationPZNBuilder ingredientText(String text) {
    this.ingredientText = text;
    return this;
  }

  public KbvErpMedicationPZNBuilder darreichungsform(Darreichungsform form) {
    this.darreichungsform = form;
    return this;
  }

  public KbvErpMedicationPZNBuilder pzn(String pzn, String medicationName) {
    return pzn(PZN.from(pzn), medicationName);
  }

  public KbvErpMedicationPZNBuilder pzn(PZN pzn, String medicationName) {
    this.pzn = pzn;
    this.medicationName = medicationName;
    return this;
  }

  public KbvErpMedicationPZNBuilder amount(long numerator) {
    return this.amount(numerator, "Stk");
  }

  public KbvErpMedicationPZNBuilder amount(long numerator, String unit) {
    this.amountNumerator = numerator;
    this.amountNumeratorUnit = unit;
    return this;
  }

  public KbvErpMedicationPZNBuilder packagingSize(String packagingSize) {
    return packagingSize(packagingSize, "ml");
  }

  public KbvErpMedicationPZNBuilder packagingSize(String packagingSize, String unit) {
    this.packagingSize = packagingSize;
    this.amountNumeratorUnit = unit;
    return this;
  }

  @Override
  public KbvErpMedication build() {
    checkRequired();
    val medication =
        this.createResource(
            KbvErpMedication::new, KbvItaErpStructDef.MEDICATION_PZN, kbvItaErpVersion);

    this.defaultAmount();

    val amount = new Ratio();

    val medExtKategorie = baseMedicationType.asExtension();
    if (kbvItaErpVersion.compareTo(KbvItaErpVersion.V1_1_0) > 0) {

      ((CodeableConcept) medExtKategorie.getValue())
          .getCoding().stream()
              .filter(CommonCodeSystem.SNOMED_SCT::matches)
              .findFirst()
              .ifPresent(c -> c.setVersion("http://snomed.info/sct/11000274103/version/20240515"));

      val ingredient = KbvIngredientComponentBuilder.builder().dontFillMissingIngredientStrength();

      if (this.kbvItaErpVersion.isBiggerThan(KbvItaErpVersion.V1_3_0)) {

        // until kbv.ita.erp 1.4.1 there is no need for Ingredient strength Num or Denom
        val numUnit =
            Optional.ofNullable(ingredientStrengthNumUnit)
                .filter(it -> !Strings.isNullOrEmpty(it))
                .orElse(null);
        val denomUnit =
            Optional.ofNullable(ingredientStrengthDenomUnit)
                .filter(it -> !Strings.isNullOrEmpty(it))
                .orElse(null);

        val strengthNumerator =
            Optional.ofNullable(ingredientStrengthNumerator)
                .filter(it -> !it.equals(BigDecimal.ZERO))
                .orElse(null);

        val strengthDenominator =
            Optional.ofNullable(ingredientStrengthDenominator)
                .filter(it -> !it.equals(BigDecimal.ZERO))
                .orElse(null);
        ingredient.ingredientStrength(
            new Quantity().setValue(strengthNumerator).setUnit(numUnit),
            new Quantity().setValue(strengthDenominator).setUnit(denomUnit));

      } else {

        val numUnit =
            Optional.ofNullable(ingredientStrengthNumUnit)
                .filter(it -> !Strings.isNullOrEmpty(it))
                .orElse("mg");
        val denomUnit =
            Optional.ofNullable(ingredientStrengthDenomUnit)
                .filter(it -> !Strings.isNullOrEmpty(it))
                .orElse("ml");

        val strengthNumerator =
            Optional.ofNullable(ingredientStrengthNumerator)
                .filter(it -> !it.equals(BigDecimal.ZERO))
                .orElse(new BigDecimal(GemFaker.fakerAmount(1, 5)));

        val strengthDenominator =
            Optional.ofNullable(ingredientStrengthDenominator)
                .filter(it -> !it.equals(BigDecimal.ZERO))
                .orElse(BigDecimal.valueOf(1));
        ingredient.ingredientStrength(
            new Quantity().setValue(strengthNumerator).setUnit(numUnit),
            new Quantity().setValue(strengthDenominator).setUnit(denomUnit));
      }

      Optional.ofNullable(ingredientText).ifPresent(ingredient::textInCoding);

      medication.addIngredient(ingredient.build());

      // changes from KBV ITA ERP Version 1.4.0
      // https://simplifier.net/packages/kbv.ita.erp/1.4.0/files/3113160
      if (this.kbvItaErpVersion.isBiggerThan(KbvItaErpVersion.V1_3_0)) {
        newBehaviorsInItaErp14(medication);
      }
    }

    extensions.add(medExtKategorie);
    val numerator = amount.getNumerator();
    val psizeValue =
        Optional.ofNullable(this.packagingSize).orElse(String.valueOf(amountNumerator));
    numerator.addExtension(KbvItaErpStructDef.PACKAGING_SIZE.asStringExtension(psizeValue));

    amount.getNumerator().setUnit(amountNumeratorUnit);
    amount.getDenominator().setValue(1); // always 1 defined by the Profile (??)

    // handle default values
    extensions.add(category.asExtension());
    extensions.add(KbvItaErpStructDef.MEDICATION_VACCINE.asBooleanExtension(isVaccine));
    extensions.add(normgroesse.asExtension());
    medication.setExtension(extensions);

    medication
        .setCode(pzn.asNamedCodeable(medicationName))
        .setForm(darreichungsform.asCodeableConcept())
        .setAmount(amount);

    return medication;
  }

  private void newBehaviorsInItaErp14(KbvErpMedication medication) {
    medication.getMeta().setVersionId("1");
    if (Strings.isNullOrEmpty(ingredientText)) {
      medication.getIngredientFirstRep().getItemCodeableConcept().addExtension(absentExt);
    }
    if (!medication.getIngredientFirstRep().hasStrength()) {
      medication.getIngredientFirstRep().getStrength().addExtension(absentExt);
    }
  }

  /**
   * The amount is quite tricky: - If not given by the user, make it a default with 10,1 - Use the
   * unit from kbvDarreichungsform - set a default code to Stk for now
   *
   * <p>See also the comment in this.amount(..)
   */
  private void defaultAmount() {
    if (amountNumerator <= 0) {
      this.amountNumerator = 1;
      this.amountNumeratorUnit = "Stk";
    }
  }

  private void checkRequired() {
    if (this.kbvItaErpVersion.isSmallerThan(KbvItaErpVersion.V1_4_0)
        && this.ingredientItemItemCC != null
        && !this.ingredientItemItemCC.isEmpty())
      this.checkRequired(
          ingredientText,
          "a text in Medication.ingredient.item[x]:itemCodeableConcept.text is mandatory until"
              + " Kbv.Ita.erp 1.4");

    if (this.kbvItaErpVersion.isBiggerThanOrEqualTo(KbvItaErpVersion.V1_4_0)
        && (ingredientStrengthNumerator != null || ingredientStrengthDenominator != null)) {
      this.checkRequired(
          ingredientText,
          " -erp-angabeWirkstaerkeWirkstoffUnbekannt : Wenn die Wirkstärke angegeben ist, darf der"
              + " Wirkstoff nicht unbekannt sein.");
    }
  }
}
