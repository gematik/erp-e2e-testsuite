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

package de.gematik.test.erezept.fhir.builder.eu;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerLotNumber;

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;

public class EuMedicationPZNFaker {

  private final Map<String, Consumer<EuMedicationBuilder>> builderConsumers = new HashMap<>();

  private EuMedicationPZNFaker() {
    withVersion(EuVersion.getDefaultVersion());

    withPzn(PZN.random(), fakerDrugName());

    if (fakerBool()) withDrugCategory(fakerValueSet(EpaDrugCategory.class));

    if (fakerBool()) withStandardSize(StandardSize.random());

    if (fakerBool())
      withDarreichungsform(
          fakerValueSet(
              Darreichungsform.class,
              List.of(Darreichungsform.KPG, Darreichungsform.PUE, Darreichungsform.LYE)));

    if (fakerBool()) withVaccineFlag(fakerBool());

    if (fakerBool()) withAmount(getFaker().random().nextLong(20));

    if (fakerBool()) withAmount(getFaker().random().nextLong(20), randomElement("St", "ml", "mg"));

    if (fakerBool()) withLotNumber(fakerLotNumber());
  }

  public static EuMedicationPZNFaker faker() {
    return new EuMedicationPZNFaker();
  }

  public EuMedicationPZNFaker withVersion(EuVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public EuMedicationPZNFaker withDrugCategory(EpaDrugCategory category) {
    builderConsumers.put("category", b -> b.category(category));
    return this;
  }

  public EuMedicationPZNFaker withPzn(PZN pzn) {
    builderConsumers.put("pzn", b -> b.pzn(pzn));
    return this;
  }

  public EuMedicationPZNFaker withPzn(PZN pzn, String medicationName) {
    builderConsumers.put("pzn", b -> b.pzn(pzn, medicationName));
    return this;
  }

  public EuMedicationPZNFaker withStandardSize(StandardSize normgroesse) {
    builderConsumers.put("normgroesse", b -> b.normgroesse(normgroesse));
    return this;
  }

  public EuMedicationPZNFaker withDarreichungsform(Darreichungsform darreichungsform) {
    builderConsumers.put("darreichungsform", b -> b.darreichungsform(darreichungsform));
    return this;
  }

  public EuMedicationPZNFaker withVaccineFlag(boolean isVaccine) {
    builderConsumers.put("vaccine", b -> b.isVaccine(isVaccine));
    return this;
  }

  public EuMedicationPZNFaker withAmount(long numerator) {
    builderConsumers.put("amount", b -> b.amount(numerator));
    return this;
  }

  public EuMedicationPZNFaker withAmount(long numerator, String unit) {
    builderConsumers.put("amount", b -> b.amount(numerator, unit));
    return this;
  }

  public EuMedicationPZNFaker withLotNumber(String lotNumber) {
    builderConsumers.put("lotnumber", b -> b.lotNumber(lotNumber));
    return this;
  }

  public GemErpMedication fake() {
    return this.toBuilder().build();
  }

  public EuMedicationBuilder toBuilder() {
    val builder = EuMedicationBuilder.builder();
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
