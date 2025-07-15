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

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.IngredientCodeBuilder;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.BaseMedicationType;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.assertj.core.util.Strings;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Ratio;

public class KbvErpMedicationPZNBuilder
    extends ResourceBuilder<KbvErpMedication, KbvErpMedicationPZNBuilder> {

  private final List<Extension> extensions = new LinkedList<>();
  private KbvItaErpVersion kbvItaErpVersion = KbvItaErpVersion.getDefaultVersion();
  private BaseMedicationType baseMedicationType = BaseMedicationType.MEDICAL_PRODUCT;
  private MedicationCategory category = MedicationCategory.C_00;
  private boolean isVaccine = false;
  private StandardSize normgroesse = StandardSize.NB;
  private Darreichungsform darreichungsform = Darreichungsform.TAB;
  private PZN pzn;
  private String medicationName;
  private String ingredientText;
  private int ingredientStrengthNumerator;
  private String ingredientStrengthNumUnit;
  private String packagingSize;
  private long amountNumerator;
  private String amountNumeratorUnit;
  private String ingredientStrengthDenomUnit;

  public static KbvErpMedicationPZNBuilder builder() {
    return new KbvErpMedicationPZNBuilder();
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public KbvErpMedicationPZNBuilder version(KbvItaErpVersion version) {
    this.kbvItaErpVersion = version;
    return this;
  }

  public KbvErpMedicationPZNBuilder type(BaseMedicationType type) {
    this.baseMedicationType = type;
    return this;
  }

  public KbvErpMedicationPZNBuilder category(MedicationCategory category) {
    this.category = category;
    return this;
  }

  public KbvErpMedicationPZNBuilder isVaccine(boolean isVaccine) {
    this.isVaccine = isVaccine;
    return this;
  }

  public KbvErpMedicationPZNBuilder normgroesse(StandardSize size) {
    this.normgroesse = size;
    return this;
  }

  public KbvErpMedicationPZNBuilder ingredientStrengthDenomUnit(String denominatorUnit) {
    this.ingredientStrengthDenomUnit = denominatorUnit;
    return this;
  }

  public KbvErpMedicationPZNBuilder ingredientStrengthNum(int num, String unit) {
    this.ingredientStrengthNumerator = num;
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

      val ingredient = IngredientCodeBuilder.builder().dontFillMissingIngredientStrength();

      ingredient.textInCoding(
          ingredientText != null && !ingredientText.isBlank()
              ? ingredientText
              : GemFaker.fakerName());

      val numUnit =
          Optional.ofNullable(ingredientStrengthNumUnit)
              .filter(it -> !Strings.isNullOrEmpty(it))
              .orElse("mg");
      val denomUnit =
          Optional.ofNullable(ingredientStrengthDenomUnit)
              .filter(it -> !Strings.isNullOrEmpty(it))
              .orElse("mg");

      val strengthNumerator =
          Optional.of(ingredientStrengthNumerator)
              .filter(it -> it != 0)
              .orElse(GemFaker.fakerAmount(1, 6));
      ingredient.ingredientStrength(
          new Quantity(strengthNumerator).setUnit(numUnit), new Quantity(1).setUnit(denomUnit));
      ingredient.textInCoding(
          ingredientText != null && !ingredientText.isBlank()
              ? ingredientText
              : GemFaker.fakerName());

      medication.addIngredient(ingredient.build());
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
}
