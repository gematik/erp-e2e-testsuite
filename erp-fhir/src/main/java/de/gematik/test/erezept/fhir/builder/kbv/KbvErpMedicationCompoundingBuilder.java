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
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.builder.erp.IngredientCodeBuilder;
import de.gematik.test.erezept.fhir.extensions.kbv.ProductionInstruction;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvBasisStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.BaseMedicationType;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.MedicationType;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Ratio;

public class KbvErpMedicationCompoundingBuilder
    extends ResourceBuilder<KbvErpMedication, KbvErpMedicationCompoundingBuilder> {

  private static final BaseMedicationType BASE_MEDICATION_TYPE =
      BaseMedicationType.PHARM_BIO_PRODUCT;
  private static final int MINIMUM_OF_ONE = 1;
  private KbvItaErpVersion kbvItaErpVersion = KbvItaErpVersion.getDefaultVersion();
  private String darreichungsform;
  private long amountNumerator = 1;
  private String amountNumeratorUnit = "Volume";
  private long amountDenominator = 1;
  private MedicationCategory category = MedicationCategory.C_00;
  private boolean isVaccine = false;
  private ProductionInstruction productionInstruction;
  private PZN pzn;
  private String medicationName;
  private String ingredItemText;
  private String packaging;
  private Ratio ingredientStrength;
  private String ingredientStrengthFreeText;

  public static KbvErpMedicationCompoundingBuilder builder() {
    return new KbvErpMedicationCompoundingBuilder();
  }

  public KbvErpMedicationCompoundingBuilder category(MedicationCategory category) {
    this.category = category;
    return self();
  }

  public KbvErpMedicationCompoundingBuilder version(KbvItaErpVersion version) {
    this.kbvItaErpVersion = version;
    return this;
  }

  public KbvErpMedicationCompoundingBuilder isVaccine(boolean isVaccine) {
    this.isVaccine = isVaccine;
    return self();
  }

  public KbvErpMedicationCompoundingBuilder productionInstruction(
      ProductionInstruction productionInstruction) {
    this.productionInstruction = productionInstruction;
    return self();
  }

  public KbvErpMedicationCompoundingBuilder darreichungsform(Darreichungsform df) {
    return darreichungsform(df.getDisplay());
  }

  public KbvErpMedicationCompoundingBuilder darreichungsform(String df) {
    this.darreichungsform = df;
    return self();
  }

  public KbvErpMedicationCompoundingBuilder ingredItemText(String ingredItemText) {
    this.ingredItemText = ingredItemText;
    return self();
  }

  public KbvErpMedicationCompoundingBuilder medicationIngredient(
      String pzn, String medicationName) {
    return medicationIngredient(PZN.from(pzn), medicationName, "freitextInPzn");
  }

  public KbvErpMedicationCompoundingBuilder medicationIngredient(
      String pzn, String medicationName, String freitextInPzn) {
    return medicationIngredient(PZN.from(pzn), medicationName, freitextInPzn);
  }

  public KbvErpMedicationCompoundingBuilder medicationIngredient(
      PZN pzn, String medicationName, String freitextInPzn) {
    this.pzn = pzn;
    this.medicationName = medicationName;
    this.ingredItemText = freitextInPzn;
    return self();
  }

  public KbvErpMedicationCompoundingBuilder packaging(String packaging) {
    this.packaging = packaging;
    return self();
  }

  public KbvErpMedicationCompoundingBuilder ingredientStrengthText(
      String freetextInIngredientStrength) {
    this.ingredientStrengthFreeText = freetextInIngredientStrength;
    return self();
  }

  public KbvErpMedicationCompoundingBuilder ingredientStrength(Ratio ingredientStrength) {
    this.ingredientStrength = ingredientStrength;
    return self();
  }

  public KbvErpMedicationCompoundingBuilder amount(long numerator) {
    return this.amount(numerator, "stk");
  }

  public KbvErpMedicationCompoundingBuilder amount(long numerator, String unit) {
    return amount(numerator, MINIMUM_OF_ONE, unit);
  }

  public KbvErpMedicationCompoundingBuilder amount(long numerator, long denominator, String unit) {
    this.amountNumerator = numerator;
    this.amountNumeratorUnit = unit;
    this.amountDenominator = denominator;
    return self();
  }

  @Override
  public KbvErpMedication build() {
    checkRequired();
    val medication =
        this.createResource(
            KbvErpMedication::new, KbvItaErpStructDef.MEDICATION_COMPOUNDING, kbvItaErpVersion);

    val amount = new Ratio();

    if (this.kbvItaErpVersion.compareTo(KbvItaErpVersion.V1_1_0) <= 0) {
      medication.addExtension(BASE_MEDICATION_TYPE.asExtension());
    } else {
      medication.addExtension(getNewSnomedKategorie());
    }

    Optional.ofNullable(productionInstruction)
        .map(ProductionInstruction::asExtension)
        .ifPresent(medication::addExtension);

    // "Gesamtmenge" in Compounding hat als URL ebenfalls PackagingSize
    amount
        .getNumerator()
        .addExtension(
            KbvItaErpStructDef.PACKAGING_SIZE.asStringExtension(String.valueOf(amountNumerator)));

    // maximum length of string in Medication.extension:Verpackung.value[x]:valueString is 60 digits
    Optional.ofNullable(packaging)
        .map(p -> ProductionInstruction.asPackaging(p).asExtension())
        .ifPresent(medication::addExtension);

    amount.getNumerator().setUnit(amountNumeratorUnit);
    amount.getDenominator().setValue(amountDenominator);

    medication.addExtension(category.asExtension());
    medication.addExtension(KbvItaErpStructDef.MEDICATION_VACCINE.asBooleanExtension(isVaccine));

    medication
        .setCode(MedicationType.COMPOUNDING.asCodeableConcept())
        .setForm(new CodeableConcept().setText(darreichungsform))
        .setAmount(amount);

    medication.getCode().setText(medicationName);
    medication.addIngredient(simpleMedicationIngredientBuilder());

    return medication;
  }

  private Extension getNewSnomedKategorie() {
    val coding =
        new Coding()
            .setSystem(CommonCodeSystem.SNOMED_SCT.getCanonicalUrl())
            .setVersion("http://snomed.info/sct/11000274103/version/20240515")
            .setCode("1208954007")
            .setDisplay("Extemporaneous preparation (product)");
    return new Extension(
        KbvBasisStructDef.BASE_MEDICATION_TYPE.getCanonicalUrl(), new CodeableConcept(coding));
  }

  private Medication.MedicationIngredientComponent simpleMedicationIngredientBuilder() {
    var medicationIngredient = IngredientCodeBuilder.builder().dontFillMissingIngredientStrength();
    Optional.ofNullable(pzn).ifPresent(medicationIngredient::withPzn);
    Optional.ofNullable(medicationName).ifPresent(medicationIngredient::textInCoding);
    Optional.ofNullable(ingredItemText).ifPresent(medicationIngredient::ingredItemCodingText);
    Optional.ofNullable(ingredientStrengthFreeText)
        .ifPresent(medicationIngredient::withStrengthFreetext);
    Optional.ofNullable(ingredientStrength).ifPresent(medicationIngredient::ingredientStrength);
    return medicationIngredient.build();
  }

  private void checkRequired() {
    this.checkRequiredExactlyOneOf(
        "A MedicationCompounding requires a ProductionInstruction or a Packaging in Version:"
            + " KbvItaErpVersion.V1_1_0, but only one of them",
        productionInstruction,
        packaging);
    this.checkRequired(
        ingredItemText,
        "A MedicationCompounding requires a Medication.MedicationIngredientComponent mit"
            + " Freitextangabe");
    this.checkRequired(darreichungsform, "A MedicationCompounding requires a Darreichungsform ");
    if (ingredientStrength == null && ingredientStrengthFreeText == null) {
      throw new BuilderException(
          "A MedicationCompounding requires a IngredientStrength or a IngredientStrengthFreeText");
    }
  }
}
