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

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;

import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;

public class KbvErpMedicationIngredientFaker {

  private final Map<String, Consumer<KbvErpMedicationIngredientBuilder>> builderConsumers =
      new HashMap<>();

  private KbvErpMedicationIngredientFaker() {
    this.withDosageForm(fakerDosage())
        .withIngredientComponent(2, 1, "Wölckhen")
        .withDrugName(fakerDrugName())
        .withVaccine(fakerBool())
        .withStandardSize(StandardSize.random());
  }

  public static KbvErpMedicationIngredientFaker builder() {
    return new KbvErpMedicationIngredientFaker();
  }

  public KbvErpMedicationIngredientFaker withVersion(KbvItaErpVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public KbvErpMedicationIngredientFaker withCategory(MedicationCategory category) {
    builderConsumers.put("category", b -> b.category(category));
    return this;
  }

  public KbvErpMedicationIngredientFaker withVaccine(boolean isVaccine) {
    builderConsumers.put("vaccine", b -> b.isVaccine(isVaccine));
    return this;
  }

  public KbvErpMedicationIngredientFaker withAmount(String numerator) {
    return this.withAmount(numerator, "Stk");
  }

  public KbvErpMedicationIngredientFaker withAmount(String numerator, String unit) {
    return this.withAmount(numerator, 1, unit);
  }

  public KbvErpMedicationIngredientFaker withAmount(
      String numerator, long denominator, String unit) {
    builderConsumers.put("amount", b -> b.amount(numerator, denominator, unit));
    return this;
  }

  public KbvErpMedicationIngredientFaker withDosageForm(String df) {
    builderConsumers.put("dosageForm", b -> b.darreichungsform(df));
    return this;
  }

  public KbvErpMedicationIngredientFaker withIngredientComponent(String ingredientUnit) {
    return this.withIngredientComponent(1, 1, ingredientUnit);
  }

  public KbvErpMedicationIngredientFaker withIngredientComponent(
      long numerator, long deNom, String unit) {
    builderConsumers.put("ingredientComponent", b -> b.ingredientComponent(numerator, deNom, unit));
    return this;
  }

  public KbvErpMedicationIngredientFaker withStandardSize(StandardSize size) {
    builderConsumers.put("standardSize", b -> b.normGroesse(size));
    return this;
  }

  public KbvErpMedicationIngredientFaker withDrugName(String drugName) {
    builderConsumers.put("drugName", b -> b.drugName(drugName));
    return this;
  }

  public KbvErpMedication fake() {
    return this.toBuilder().build();
  }

  public KbvErpMedicationIngredientBuilder toBuilder() {
    val builder = KbvErpMedicationIngredientBuilder.builder();
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
