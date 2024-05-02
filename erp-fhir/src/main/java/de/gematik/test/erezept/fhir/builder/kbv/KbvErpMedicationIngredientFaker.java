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

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;

import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class KbvErpMedicationIngredientFaker {
  private final KbvErpMedicationIngredientBuilder builder;
  private final Map<String, Consumer<KbvErpMedicationIngredientBuilder>> builderConsumers =
      new HashMap<>();

  private KbvErpMedicationIngredientFaker(KbvErpMedicationIngredientBuilder builder) {
    builderConsumers.put("dosageForm", b -> b.darreichungsform(fakerDosage()));
    builderConsumers.put("ingredientComponent", b -> b.ingredientComponent(2, 1, "wölckhen"));
    builderConsumers.put("drugName", b -> b.drugName(fakerDrugName()));
    this.builder = builder;
  }

  public static KbvErpMedicationIngredientFaker builder() {
    return new KbvErpMedicationIngredientFaker(KbvErpMedicationIngredientBuilder.builder());
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
    builderConsumers.computeIfPresent(
        "dosageForm", (key, defaultValue) -> b -> b.darreichungsform(df));
    return this;
  }

  public KbvErpMedicationIngredientFaker withIngredientComponent(String ingredientUnit) {
    return this.withIngredientComponent(1, 1, ingredientUnit);
  }

  public KbvErpMedicationIngredientFaker withIngredientComponent(
      long numerator, long deNom, String unit) {
    builderConsumers.computeIfPresent(
        "ingredientComponent",
        (key, defaultValue) -> b -> b.ingredientComponent(numerator, deNom, unit));
    return this;
  }

  public KbvErpMedicationIngredientFaker withStandardSize(StandardSize size) {
    builderConsumers.put("standardSize", b -> b.normGroesse(size));
    return this;
  }

  public KbvErpMedicationIngredientFaker withDrugName(String drugName) {
    builderConsumers.computeIfPresent("drugName", (key, defaultValue) -> b -> b.drugName(drugName));
    return this;
  }

  public KbvErpMedication fake() {
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder.build();
  }
}
