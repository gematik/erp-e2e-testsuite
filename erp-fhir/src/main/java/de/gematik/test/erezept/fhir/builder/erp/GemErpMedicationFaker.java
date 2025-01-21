/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.fhir.builder.erp;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerBool;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerDrugName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerValueSet;
import static de.gematik.test.erezept.fhir.builder.GemFaker.getFaker;
import static de.gematik.test.erezept.fhir.builder.GemFaker.randomElement;

import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.resources.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.fhir.valuesets.epa.EpaDrugCategory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;

public class GemErpMedicationFaker {

  private final Map<String, Consumer<GemErpMedicationBuilder>> builderConsumers = new HashMap<>();

  private GemErpMedicationFaker() {
    withVersion(ErpWorkflowVersion.V1_4_0);

    // for now set always a PZN, in the future purely by name or ingredient might also be possible
    withPzn(PZN.random());

    if (fakerBool()) withPzn(PZN.random(), fakerDrugName());

    if (fakerBool()) withDrugCategory(fakerValueSet(EpaDrugCategory.class));

    if (fakerBool()) withStandardSize(fakerValueSet(StandardSize.class));

    // building Kombipackung is not supported yet
    if (fakerBool())
      withDarreichungsform(
          fakerValueSet(
              Darreichungsform.class,
              List.of(Darreichungsform.KPG, Darreichungsform.PUE, Darreichungsform.LYE)));

    if (fakerBool()) withVaccineFlag(fakerBool());

    if (fakerBool()) withAmount(getFaker().random().nextLong(20));

    if (fakerBool()) withAmount(getFaker().random().nextLong(20), randomElement("St", "ml", "mg"));

    if (fakerBool()) withLotNumber(getFaker().regexify("[0-9]{8,10}"));
  }

  public static GemErpMedicationFaker builder() {
    return new GemErpMedicationFaker();
  }

  public GemErpMedicationFaker withVersion(ErpWorkflowVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public GemErpMedicationFaker withDrugCategory(EpaDrugCategory category) {
    builderConsumers.put("category", b -> b.category(category));
    return this;
  }

  public GemErpMedicationFaker withPzn(PZN pzn) {
    builderConsumers.put("pzn", b -> b.pzn(pzn));
    return this;
  }

  public GemErpMedicationFaker withPzn(PZN pzn, String medicationName) {
    builderConsumers.put("pzn", b -> b.pzn(pzn, medicationName));
    return this;
  }

  public GemErpMedicationFaker withStandardSize(StandardSize normgroesse) {
    builderConsumers.put("normgroesse", b -> b.normgroesse(normgroesse));
    return this;
  }

  public GemErpMedicationFaker withDarreichungsform(Darreichungsform darreichungsform) {
    builderConsumers.put("darreichungsform", b -> b.darreichungsform(darreichungsform));
    return this;
  }

  public GemErpMedicationFaker withVaccineFlag(boolean isVaccine) {
    builderConsumers.put("vaccine", b -> b.isVaccine(isVaccine));
    return this;
  }

  public GemErpMedicationFaker withAmount(long numerator) {
    builderConsumers.put("amount", b -> b.amount(numerator));
    return this;
  }

  public GemErpMedicationFaker withAmount(long numerator, String unit) {
    builderConsumers.put("amount", b -> b.amount(numerator, unit));
    return this;
  }

  public GemErpMedicationFaker withLotNumber(String lotNumber) {
    builderConsumers.put("lotnumber", b -> b.lotNumber(lotNumber));
    return this;
  }

  public GemErpMedication fake() {
    return this.toBuilder().build();
  }

  public GemErpMedicationBuilder toBuilder() {
    val builder = GemErpMedicationBuilder.builder();
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
