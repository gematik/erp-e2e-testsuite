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

package de.gematik.test.erezept.fhir.builder.erp;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;

import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Quantity;

public class GemErpMedicationCompoundingFaker implements GemErpMedicationFaker {

  private final Map<String, Consumer<GemErpMedCompoundingBuilder>> builderConsumers =
      new HashMap<>();
  private String askKey = "ask";
  private String snomedKey = "snomed";
  private String atcKey = "atc";

  public GemErpMedicationCompoundingFaker() {

    withVersion(ErpWorkflowVersion.V1_4);

    if (fakerBool()) {
      withAsk(ASK.from(GemFaker.fakerBsnr()));
    } else {
      withAtc(ATC.from(GemFaker.fakerPhone()));
    }
    if (fakerBool()) withSnomed(GemFaker.fakerPhone());

    if (fakerBool()) withSnomed(GemFaker.fakerPhone());
    if (fakerBool())
      withIngredientWithContainedAtc(
          getFaker().random().nextInt(1, 1000),
          getFaker().random().nextInt(1, 10),
          ATC.from(getRandomFourDigitsCode()));
    if (fakerBool()) withDrugCategory(fakerValueSet(EpaDrugCategory.class));
    if (fakerBool()) withVaccineTrue(fakerBool());
    if (fakerBool()) withAmount(getFaker().random().nextLong(20));
    if (fakerBool()) withAmount(getFaker().random().nextLong(20), randomElement("St", "ml", "mg"));
    if (fakerBool()) withLotNumber(fakerLotNumber());
    if (fakerBool()) withAtc(ATC.from(getRandomFourDigitsCode(), fakerDrugName(), fakerDrugName()));
  }

  /**
   * Set the ASK value for the medication to set more than one ASK value, use this method multiple
   * times
   *
   * @param ask
   * @return GemErpMedicationCompoundingFaker
   */
  public GemErpMedicationCompoundingFaker withAsk(ASK ask) {
    // to set more than one ASK Codings (Set up a List of Codings) the key will be appended
    if (builderConsumers.containsKey(askKey)) askKey = askKey + "+1";
    builderConsumers.put(askKey, b -> b.ask(ask));
    return this;
  }

  /**
   * Set the SNOMED value for the medication to set more than one SNOMED value, use this method
   * multiple times
   *
   * @param snomed
   * @return GemErpMedicationCompoundingFaker
   */
  public GemErpMedicationCompoundingFaker withSnomed(String snomed) {
    // to set more than one snomed Codings (Set up a List of Codings) the key will be appended
    if (builderConsumers.containsKey(snomedKey)) snomedKey = snomedKey + "+1";
    builderConsumers.put(snomedKey, b -> b.snomed(snomed));
    return this;
  }

  /**
   * Set the ATC value for the medication to set more than one ATC value, use this method multiple
   * times
   *
   * @param atc
   * @return GemErpMedicationCompoundingFaker
   */
  public GemErpMedicationCompoundingFaker withAtc(ATC atc) {
    // to set more than one ATC Codings (Set up a List of Codings) the key will be appended
    if (builderConsumers.containsKey(atcKey)) atcKey = atcKey + "+1";
    builderConsumers.put(atcKey, b -> b.atc(atc));
    return this;
  }

  public GemErpMedicationCompoundingFaker withVersion(ErpWorkflowVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public GemErpMedicationCompoundingFaker withDrugCategory(EpaDrugCategory category) {
    builderConsumers.put("category", b -> b.category(category));
    return this;
  }

  /**
   * Set an ingredient component for the medication with an included ATC Coding
   *
   * @param numerator of the ingredient strength
   * @param denominator of the ingredient strength
   * @param atc as contained coding of the ingredient
   * @return GemErpMedicationCompoundingFaker
   */
  public GemErpMedicationCompoundingFaker withIngredientWithContainedAtc(
      int numerator, int denominator, ATC atc) {
    builderConsumers.put(
        "ingredientComponent",
        b -> b.ingredientComponent(buildIngredient(numerator, denominator, atc)));
    return this;
  }

  public GemErpMedicationCompoundingFaker withVaccineTrue(boolean isVaccine) {
    builderConsumers.put("vaccine", b -> b.isVaccine(isVaccine));
    return this;
  }

  public GemErpMedicationCompoundingFaker withAmount(long numerator) {
    builderConsumers.put("amount", b -> b.amount(numerator));
    return this;
  }

  public GemErpMedicationCompoundingFaker withAmount(long numerator, String unit) {
    builderConsumers.put("amount", b -> b.amount(numerator, unit));
    return this;
  }

  public GemErpMedicationCompoundingFaker withLotNumber(String lotNumber) {
    builderConsumers.put("lotnumber", b -> b.lotNumber(lotNumber));
    return this;
  }

  public GemErpMedication fake() {
    return this.toBuilder().build();
  }

  private Medication.MedicationIngredientComponent buildIngredient(int num, int denom, ATC atc) {
    return IngredientCodeBuilder.builder()
        .ingredientStrength(
            Quantity.fromUcum(String.valueOf(num), "mg"),
            Quantity.fromUcum(String.valueOf(denom), "mg"))
        .withAtc(atc)
        .build();
  }

  public GemErpMedCompoundingBuilder toBuilder() {
    val builder = GemErpMedicationBuilder.forCompounding();
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }

  private String getRandomFourDigitsCode() {
    return getFaker().regexify("[0-9]{1,4}");
  }
}
