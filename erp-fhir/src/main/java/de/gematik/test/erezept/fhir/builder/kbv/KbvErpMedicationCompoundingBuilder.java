/*
 * Copyright 2023 gematik GmbH
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

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.extensions.kbv.ProductionInstruction;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.LinkedList;
import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.*;

public class KbvErpMedicationCompoundingBuilder
    extends AbstractResourceBuilder<KbvErpMedicationCompoundingBuilder> {

  private static final BaseMedicationType BASE_MEDICATION_TYPE =
      BaseMedicationType.PHARM_BIO_PRODUCT;
  private static final int MINIMUM_OF_ONE = 1;
  private String medicineName;
  private final List<Extension> extensions = new LinkedList<>();
  private String darreichungsform;
  private long amountNumerator;
  private String amountNumeratorUnit;
  private long amountDenominator;
  private KbvItaErpVersion kbvItaErpVersion = KbvItaErpVersion.getDefaultVersion();
  private MedicationCategory category = MedicationCategory.C_00;
  private boolean isVaccine = false;
  private ProductionInstruction productionInstruction = ProductionInstruction.random();
  private PZN pzn;
  private String medicationName;
  private String freiTextInPzn;

  public static KbvErpMedicationCompoundingBuilder builder() {
    return new KbvErpMedicationCompoundingBuilder();
  }

  public static KbvErpMedicationCompoundingBuilder faker() {
    return faker(PZN.random(), "Fancy Salbe die auch als Schuhcreme funktioniert", "freeTheText");
  }

  public static KbvErpMedicationCompoundingBuilder faker(PZN pzn, String name, String freiText) {
    return new KbvErpMedicationCompoundingBuilder()
        .medicationIngredient(pzn, name, freiText)
        .category(MedicationCategory.C_00)
        .isVaccine(false) // default false
        .productionInstruction(ProductionInstruction.asCompounding("freitext"))
        .darreichungsform("Zäpfchen, viel Spaß")
        .amount(5, 1, "Stk");
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
      @NonNull String pzn, @NonNull String medicationName) {
    return medicationIngredient(PZN.from(pzn), medicationName, "freitextInPzn");
  }

  public KbvErpMedicationCompoundingBuilder medicationIngredient(
      @NonNull String pzn, @NonNull String medicationName, String freitextInPzn) {
    return medicationIngredient(PZN.from(pzn), medicationName, freitextInPzn);
  }

  public KbvErpMedicationCompoundingBuilder medicationIngredient(
      PZN pzn, String medicationName, String freitextInPzn) {
    this.pzn = pzn;
    this.medicationName = medicationName;
    this.freiTextInPzn = freitextInPzn;
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

  public KbvErpMedication build() {
    checkRequired();
    val medicationCompounding = new KbvErpMedication();
    val profile = KbvItaErpStructDef.MEDICATION_COMPOUNDING.asCanonicalType(kbvItaErpVersion);
    val meta = new Meta().setProfile(List.of(profile));
    medicationCompounding.setId(this.getResourceId()).setMeta(meta);
    this.defaultAmount();
    val amount = new Ratio();
    if (kbvItaErpVersion.compareTo(KbvItaErpVersion.V1_1_0) < 0) {
      amount.getNumerator().setValue(amountNumerator);
    } else {
      extensions.add(BASE_MEDICATION_TYPE.asExtension());

      val numerator = amount.getNumerator();
      numerator
          .addExtension()
          .setValue(new StringType(String.valueOf(amountNumerator)))
          // "Gesamtmenge" in Compounding hat als URL ebenfalls PackagingSize
          .setUrl(KbvItaErpStructDef.PACKAGING_SIZE.getCanonicalUrl());
    }

    amount.getNumerator().setUnit(amountNumeratorUnit);
    amount.getDenominator().setValue(amountDenominator);

    extensions.add(category.asExtension());
    extensions.add(KbvItaErpStructDef.MEDICATION_VACCINE.asBooleanExtension(isVaccine));
    extensions.add(productionInstruction.asExtension());
    medicationCompounding.setExtension(extensions);
    medicationCompounding
        .setCode(MedicationType.COMPOUNDING.asCodeableConcept())
        .setForm(new CodeableConcept().setText(darreichungsform))
        .setAmount(amount);

    medicationCompounding.getCode().setText(medicineName);
    medicationCompounding.addIngredient(simpleMedicationIngredientBuilder());

    return medicationCompounding;
  }

  private void defaultAmount() {
    if (amountNumerator <= 0) {
      this.amountNumerator = 1;
      this.amountNumeratorUnit = "Volume";
    }
  }

  private void checkRequired() {
    this.checkRequired(
        productionInstruction,
        "A MedicationCompounding requires a ProductionInstruction in Version:"
            + " KbvItaErpVersion.V1_1_0");
    this.checkRequired(
        freiTextInPzn,
        "A MedicationCompounding requires a Medication.MedicationIngredientComponent mit"
            + " Freitextangabe");
    this.checkRequired(pzn, "A MedicationCompounding requires in this Implementation a Pzn");
    this.checkRequired(darreichungsform, "A MedicationCompounding requires a Darreichungsform ");
  }
}
