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

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.valuesets.BaseMedicationType;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class KbvErpMedicationPZNFaker {
  private final KbvErpMedicationPZNBuilder builder;
  private final Map<String, Consumer<KbvErpMedicationPZNBuilder>> builderConsumers =
      new HashMap<>();

  private KbvErpMedicationPZNFaker(KbvErpMedicationPZNBuilder builder) {
    this.builder = builder;
    builderConsumers.put("pznMedication", b -> b.pzn(PZN.random(), fakerDrugName()));
    builderConsumers.put("type", b -> b.type(BaseMedicationType.MEDICAL_PRODUCT));
    builderConsumers.put("category", b -> b.category(MedicationCategory.C_00));
    builderConsumers.put("vaccine", b -> b.isVaccine(false));
    builderConsumers.put("standardSize", b -> b.normgroesse(StandardSize.NB));
    builderConsumers.put(
        "supplyForm",
        b ->
            b.darreichungsform(
                GemFaker.fakerValueSet(Darreichungsform.class, Darreichungsform.LYO)));
    builderConsumers.put("amount", b -> b.amount(fakerAmount(), "Stk"));
  }

  public static KbvErpMedicationPZNFaker builder() {
    return new KbvErpMedicationPZNFaker(KbvErpMedicationPZNBuilder.builder());
  }

  public KbvErpMedicationPZNFaker withVersion(KbvItaErpVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public KbvErpMedicationPZNFaker withType(BaseMedicationType type) {
    builderConsumers.computeIfPresent("type", (key, defaultValue) -> b -> b.type(type));
    return this;
  }

  public KbvErpMedicationPZNFaker withCategory(MedicationCategory category) {
    builderConsumers.computeIfPresent("category", (key, defaultValue) -> b -> b.category(category));
    return this;
  }

  public KbvErpMedicationPZNFaker withVaccine(boolean vaccine) {
    builderConsumers.computeIfPresent("vaccine", (key, defaultValue) -> b -> b.isVaccine(vaccine));
    return this;
  }

  public KbvErpMedicationPZNFaker withStandardSize(StandardSize size) {
    builderConsumers.computeIfPresent(
        "standardSize", (key, defaultValue) -> b -> b.normgroesse(size));
    return this;
  }

  public KbvErpMedicationPZNFaker withSupplyForm(Darreichungsform form) {
    builderConsumers.computeIfPresent(
        "supplyForm", (key, defaultValue) -> b -> b.darreichungsform(form));
    return this;
  }

  public KbvErpMedicationPZNFaker withPznMedication(String pzn, String medicationName) {
    return withPznMedication(PZN.from(pzn), medicationName);
  }

  public KbvErpMedicationPZNFaker withPznMedication(PZN pzn, String medicationName) {
    builderConsumers.computeIfPresent(
        "pznMedication", (key, defaultValue) -> b -> b.pzn(pzn, medicationName));
    return this;
  }

  public KbvErpMedicationPZNFaker withAmount(long numerator) {
    return this.withAmount(numerator, "Stk");
  }

  public KbvErpMedicationPZNFaker withAmount(long numerator, String unit) {
    builderConsumers.computeIfPresent(
        "amount", (key, defaultValue) -> b -> b.amount(numerator, unit));
    return this;
  }

  public KbvErpMedication fake() {
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder.build();
  }
}
