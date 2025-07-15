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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerAmount;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerBool;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerDrugName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerValueSet;

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.BaseMedicationType;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;

public class KbvErpMedicationPZNFaker {

  private final Map<String, Consumer<KbvErpMedicationPZNBuilder>> builderConsumers =
      new HashMap<>();

  private KbvErpMedicationPZNFaker() {
    val supplyForm =
        fakerValueSet(
            Darreichungsform.class,
            List.of(Darreichungsform.PUE, Darreichungsform.KPG, Darreichungsform.LYE));
    this.withPznMedication(PZN.random(), fakerDrugName())
        .withType(BaseMedicationType.MEDICAL_PRODUCT)
        .withCategory(MedicationCategory.C_00)
        .withVaccine(fakerBool())
        .withStandardSize(StandardSize.random())
        .withSupplyForm(supplyForm)
        .withAmount(fakerAmount(), "Stk");
  }

  public static KbvErpMedicationPZNFaker builder() {
    return new KbvErpMedicationPZNFaker();
  }

  public KbvErpMedicationPZNFaker withVersion(KbvItaErpVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public KbvErpMedicationPZNFaker withType(BaseMedicationType type) {
    builderConsumers.put("type", b -> b.type(type));
    return this;
  }

  public KbvErpMedicationPZNFaker withCategory(MedicationCategory category) {
    builderConsumers.put("category", b -> b.category(category));
    return this;
  }

  public KbvErpMedicationPZNFaker withVaccine(boolean vaccine) {
    builderConsumers.put("vaccine", b -> b.isVaccine(vaccine));
    return this;
  }

  public KbvErpMedicationPZNFaker withStandardSize(StandardSize size) {
    builderConsumers.put("standardSize", b -> b.normgroesse(size));
    return this;
  }

  public KbvErpMedicationPZNFaker withSupplyForm(Darreichungsform form) {
    builderConsumers.put("supplyForm", b -> b.darreichungsform(form));
    return this;
  }

  public KbvErpMedicationPZNFaker withPznMedication(String pzn, String medicationName) {
    return withPznMedication(PZN.from(pzn), medicationName);
  }

  public KbvErpMedicationPZNFaker withPznMedication(PZN pzn, String medicationName) {
    builderConsumers.put("pznMedication", b -> b.pzn(pzn, medicationName));
    return this;
  }

  public KbvErpMedicationPZNFaker withAmount(long numerator) {
    return this.withAmount(numerator, "Stk");
  }

  public KbvErpMedicationPZNFaker withAmount(long numerator, String unit) {
    builderConsumers.put("amount", b -> b.amount(numerator, unit));
    return this;
  }

  public KbvErpMedication fake() {
    return this.toBuilder().build();
  }

  public KbvErpMedicationPZNBuilder toBuilder() {
    val builder = KbvErpMedicationPZNBuilder.builder();
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
