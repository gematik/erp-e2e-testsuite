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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerBool;

import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;

public class KbvErpMedicationFreeTextFaker {

  private final Map<String, Consumer<KbvErpMedicationFreeTextBuilder>> builderConsumers =
      new HashMap<>();

  private KbvErpMedicationFreeTextFaker() {
    this.withDosageForm("Lutscher mit Brausepulverfü...") // TODO: why only 30 characters?
        .withFreeText("3 mal täglich einen lutscher lutschen und anschließend Zähnchen putzen")
        .withVaccine(fakerBool());
  }

  public static KbvErpMedicationFreeTextFaker builder() {
    return new KbvErpMedicationFreeTextFaker();
  }

  public KbvErpMedicationFreeTextFaker withVersion(KbvItaErpVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public KbvErpMedicationFreeTextFaker withDosageForm(String df) {
    builderConsumers.put("darreichungsform", b -> b.darreichung(df));
    return this;
  }

  public KbvErpMedicationFreeTextFaker withFreeText(String freeText) {
    builderConsumers.put("freetext", b -> b.freeText(freeText));
    return this;
  }

  public KbvErpMedicationFreeTextFaker withVaccine(boolean isVaccine) {
    builderConsumers.put("vaccine", b -> b.isVaccine(isVaccine));
    return this;
  }

  public KbvErpMedicationFreeTextFaker withCategory(MedicationCategory category) {
    builderConsumers.put("category", b -> b.category(category));
    return this;
  }

  public KbvErpMedication fake() {
    return this.toBuilder().build();
  }

  public KbvErpMedicationFreeTextBuilder toBuilder() {
    val builder = KbvErpMedicationFreeTextBuilder.builder();
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
