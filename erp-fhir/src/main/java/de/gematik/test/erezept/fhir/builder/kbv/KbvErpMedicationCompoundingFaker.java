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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.extensions.kbv.ProductionInstruction;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;

public class KbvErpMedicationCompoundingFaker {

  private final Map<String, Consumer<KbvErpMedicationCompoundingBuilder>> builderConsumers =
      new HashMap<>();

  private KbvErpMedicationCompoundingFaker() {

    this.withDosageForm(GemFaker.fakerValueSet(Darreichungsform.class))
        .withAmount(5, 1, "Stk")
        .withIngredientStrengthText(
            format("Ad {0} g", GemFaker.randomElement("25", "50", "75", "100", "150", "200")))
        .withIngredItemText(GemFaker.fakerDrugName())
        .withProductionInstruction(ProductionInstruction.asCompounding("freitext"));
  }

  public static KbvErpMedicationCompoundingFaker builder() {
    return new KbvErpMedicationCompoundingFaker();
  }

  public KbvErpMedicationCompoundingFaker withDosageForm(String df) {
    builderConsumers.put("kbvDarreichungsform", b -> b.darreichungsform(df));
    return this;
  }

  public KbvErpMedicationCompoundingFaker withDosageForm(Darreichungsform df) {
    val display = df.getDisplay();
    val maxLen = Math.min(display.length(), 30);
    this.withDosageForm(display.substring(0, maxLen));
    return this;
  }

  /**
   * packaging will be removed!!!
   *
   * @param productionInstruction
   * @return
   */
  public KbvErpMedicationCompoundingFaker withProductionInstruction(String productionInstruction) {
    return withProductionInstruction(ProductionInstruction.asCompounding(productionInstruction));
  }

  public KbvErpMedicationCompoundingFaker withProductionInstruction(ProductionInstruction pd) {
    builderConsumers.put("productionInstruction", b -> b.productionInstruction(pd));
    builderConsumers.remove("packaging");
    return this;
  }

  public KbvErpMedicationCompoundingFaker withAmount(long numerator) {
    return this.withAmount(numerator, "Stk");
  }

  public KbvErpMedicationCompoundingFaker withAmount(long numerator, String unit) {
    return this.withAmount(numerator, 1, unit);
  }

  public KbvErpMedicationCompoundingFaker withAmount(
      long numerator, long denominator, String unit) {
    builderConsumers.put("amount", b -> b.amount(numerator, denominator, unit));
    return this;
  }

  public KbvErpMedicationCompoundingFaker withVersion(KbvItaErpVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public KbvErpMedicationCompoundingFaker withCategory(MedicationCategory category) {
    builderConsumers.put("category", b -> b.category(category));
    return this;
  }

  public KbvErpMedicationCompoundingFaker withVaccine(boolean isVaccine) {
    builderConsumers.put("vaccine", b -> b.isVaccine(isVaccine));
    return this;
  }

  public KbvErpMedicationCompoundingFaker withMedicationIngredient(
      String pzn, String medicationName) {
    return this.withMedicationIngredient(pzn, medicationName, "freitextInPzn");
  }

  public KbvErpMedicationCompoundingFaker withMedicationIngredient(
      PZN pzn, String medicationName, String freitextInPzn) {
    return this.withMedicationIngredient(pzn.getValue(), medicationName, freitextInPzn);
  }

  public KbvErpMedicationCompoundingFaker withMedicationIngredient(
      String pzn, String medicationName, String freitextInPzn) {
    builderConsumers.put(
        "medicationIngredient", b -> b.medicationIngredient(pzn, medicationName, freitextInPzn));
    return this;
  }

  public KbvErpMedicationCompoundingFaker withResourceId(String resourceId) {
    builderConsumers.put("resourceId", b -> b.setId(resourceId));
    return this;
  }

  public KbvErpMedicationCompoundingFaker withPackaging(String packaging) {
    builderConsumers.put("packaging", p -> p.packaging(packaging));
    builderConsumers.remove("productionInstruction");
    return this;
  }

  public KbvErpMedication fake() {
    return this.toBuilder().build();
  }

  public KbvErpMedicationCompoundingBuilder toBuilder() {
    val builder = KbvErpMedicationCompoundingBuilder.builder();
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }

  public KbvErpMedicationCompoundingFaker withIngredItemText(String ingredItemText) {
    builderConsumers.put("ingredItemText", b -> b.ingredientItemText(ingredItemText));
    return this;
  }

  public KbvErpMedicationCompoundingFaker withIngredientStrengthText(
      String ingredientStrengthText) {
    builderConsumers.put(
        "ingredStrengthText", b -> b.ingredientStrengthText(ingredientStrengthText));
    return this;
  }
}
