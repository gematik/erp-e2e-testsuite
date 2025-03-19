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
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.extensions.kbv.ProductionInstruction;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.BaseMedicationType;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.MedicationType;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Ratio;
import org.hl7.fhir.r4.model.StringType;

public class KbvErpMedicationCompoundingBuilder
    extends ResourceBuilder<KbvErpMedication, KbvErpMedicationCompoundingBuilder> {

  private KbvItaErpVersion kbvItaErpVersion = KbvItaErpVersion.getDefaultVersion();
  private static final BaseMedicationType BASE_MEDICATION_TYPE =
      BaseMedicationType.PHARM_BIO_PRODUCT;
  private static final int MINIMUM_OF_ONE = 1;
  private String darreichungsform;
  private long amountNumerator = 1;
  private String amountNumeratorUnit = "Volume";
  private long amountDenominator;
  private MedicationCategory category = MedicationCategory.C_00;
  private boolean isVaccine = false;
  private ProductionInstruction productionInstruction;
  private PZN pzn;
  private String medicationName;
  private String freiTextInPzn;
  private String packaging;

  public static KbvErpMedicationCompoundingBuilder builder() {
    return new KbvErpMedicationCompoundingBuilder();
  }

  // todo eigenen medication ingredientBuilder extrahieren
  // https://simplifier.net/packages/kbv.ita.erp/1.1.2/files/2212814
  private Medication.MedicationIngredientComponent simpleMedicationIngredientBuilder() {
    val medicationIngredient =
        new Medication.MedicationIngredientComponent()
            .setStrength(new Ratio())
            .setItem(pzn.asNamedCodeable(medicationName));
    medicationIngredient
        .getStrength()
        .getExtensionFirstRep()
        .setUrl(KbvItaErpStructDef.MEDICATION_INGREDIENT_AMOUNT.getCanonicalUrl())
        .setValue(new StringType(freiTextInPzn));
    return medicationIngredient;
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
    this.freiTextInPzn = freitextInPzn;
    return self();
  }

  public KbvErpMedicationCompoundingBuilder packaging(String packaging) {
    this.packaging = packaging;
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
    if (kbvItaErpVersion.compareTo(KbvItaErpVersion.V1_0_2) == 0) {
      amount.getNumerator().setValue(amountNumerator);

      // notice ProductionInstruction and packaging is a Pair and one of them should be set
      Optional.ofNullable(productionInstruction)
          .map(pi -> pi.asExtension(60))
          .ifPresent(medication::addExtension);

    } else {
      medication.addExtension(BASE_MEDICATION_TYPE.asExtension());
      Optional.ofNullable(productionInstruction)
          .map(ProductionInstruction::asExtension)
          .ifPresent(medication::addExtension);

      // "Gesamtmenge" in Compounding hat als URL ebenfalls PackagingSize
      amount
          .getNumerator()
          .addExtension(
              KbvItaErpStructDef.PACKAGING_SIZE.asStringExtension(String.valueOf(amountNumerator)));
    }

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

  private void checkRequired() {
    this.checkRequiredExactlyOneOf(
        "A MedicationCompounding requires a ProductionInstruction or a Packaging in Version:"
            + " KbvItaErpVersion.V1_1_0, but only one of them",
        productionInstruction,
        packaging);
    this.checkRequired(
        freiTextInPzn,
        "A MedicationCompounding requires a Medication.MedicationIngredientComponent mit"
            + " Freitextangabe");
    this.checkRequired(pzn, "A MedicationCompounding requires in this Implementation a Pzn");
    this.checkRequired(darreichungsform, "A MedicationCompounding requires a Darreichungsform ");
  }
}
