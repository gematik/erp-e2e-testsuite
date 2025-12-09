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
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.r4.EpaPharmaceuticalProduct;
import de.gematik.test.erezept.eml.fhir.r4.componentbuilder.GemEpaIngredientComponentBuilder;
import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Quantity;

public class GemErpMedicationKombiPkgFaker implements GemErpMedicationFaker {

  private final Map<String, Consumer<GemErpMedicationKombiPkgBuilder>> builderConsumers =
      new HashMap<>();
  private String askKey = "ask";
  private String snomedKey = "snomed";
  private String atcKey = "atc";
  private static final String CONTAINED_MEDICATIONS_KEY = "containedMedications";
  private static final String CONTAINED_ATC_MEDICATIONS_KEY = "containedAtcMedications";
  private static final String CONTAINED_MEDICATION_LIST_KEY = "containedMedicationList";

  public GemErpMedicationKombiPkgFaker() {

    withVersion(ErpWorkflowVersion.getDefaultVersion());

    if (fakerBool()) {
      withAsk(ASK.from(GemFaker.fakerBsnr()));
    }
    if (fakerBool()) withSnomed(GemFaker.fakerPhone());

    if (fakerBool()) withSnomed(GemFaker.fakerPhone());
    if (fakerBool())
      withIngredientWithContainedAtc(
          getFaker().random().nextInt(1, 1000),
          getFaker().random().nextInt(1, 10),
          ATC.from(getRandomFourDigitsCode()));
    if (fakerBool()) withDrugCategory(fakerValueSet(EpaDrugCategory.class));
    if (fakerBool()) withVaccine(fakerBool());
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
  public GemErpMedicationKombiPkgFaker withAsk(ASK ask) {
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
  public GemErpMedicationKombiPkgFaker withSnomed(String snomed) {
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
  public GemErpMedicationKombiPkgFaker withAtc(ATC atc) {
    // to set more than one ATC Codings (Set up a List of Codings) the key will be appended
    if (builderConsumers.containsKey(atcKey)) atcKey = atcKey + "+1";
    builderConsumers.put(atcKey, b -> b.atc(atc));
    return this;
  }

  public GemErpMedicationKombiPkgFaker withVersion(ErpWorkflowVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public GemErpMedicationKombiPkgFaker withDrugCategory(EpaDrugCategory category) {
    builderConsumers.put("category", b -> b.category(category));
    return this;
  }

  /**
   * Set an ingredient component for the medication with an included ATC Coding
   *
   * @param numerator of the ingredient strength
   * @param denominator of the ingredient strength
   * @param atc as contained coding of the ingredient
   * @return GemErpMedicationKombiPkgFaker
   */
  public GemErpMedicationKombiPkgFaker withIngredientWithContainedAtc(
      int numerator, int denominator, ATC atc) {
    builderConsumers.put(
        "ingredientComponentList",
        b -> b.ingredientComponent(buildIngredient(numerator, denominator, atc)));
    return this;
  }

  public GemErpMedicationKombiPkgFaker withVaccine(boolean isVaccine) {
    builderConsumers.put("vaccine", b -> b.isVaccine(isVaccine));
    return this;
  }

  public GemErpMedicationKombiPkgFaker withAmount(long numerator) {
    builderConsumers.put("amount", b -> b.amount(numerator));
    return this;
  }

  public GemErpMedicationKombiPkgFaker withAmount(long numerator, String unit) {
    builderConsumers.put("amount", b -> b.amount(numerator, unit));
    return this;
  }

  public GemErpMedicationKombiPkgFaker withLotNumber(String lotNumber) {
    builderConsumers.put("lotnumber", b -> b.lotNumber(lotNumber));
    return this;
  }

  public GemErpMedicationKombiPkgFaker withPzn(PZN pzn) {
    builderConsumers.put("pzn", b -> b.pzn(pzn));
    return this;
  }

  public GemErpMedicationKombiPkgFaker withContained(
      EpaPharmaceuticalProduct... epaPharmaceuticalProductList) {
    builderConsumers.put(
        CONTAINED_MEDICATION_LIST_KEY, b -> b.containedMedications(epaPharmaceuticalProductList));
    return this;
  }

  public GemErpMedicationKombiPkgFaker withContainedAtcMedications(List<ATC> atcList) {
    builderConsumers.put(CONTAINED_ATC_MEDICATIONS_KEY, b -> b.containedAtcMedications(atcList));
    return this;
  }

  public GemErpMedicationKombiPkgFaker withContained(
      EpaPharmaceuticalProduct epaPharmaceuticalProduct1,
      EpaPharmaceuticalProduct epaPharmaceuticalProduct2) {
    builderConsumers.put(
        CONTAINED_MEDICATIONS_KEY,
        b -> b.containedMedications(List.of(epaPharmaceuticalProduct1, epaPharmaceuticalProduct2)));
    return this;
  }

  public GemErpMedication fake() {
    return this.toBuilder().build();
  }

  private Medication.MedicationIngredientComponent buildIngredient(int num, int denom, ATC atc) {
    return GemEpaIngredientComponentBuilder.builder()
        .ingredientStrength(
            Quantity.fromUcum(String.valueOf(num), "mg"),
            Quantity.fromUcum(String.valueOf(denom), "mg"))
        .atc(atc)
        .build();
  }

  public GemErpMedicationKombiPkgBuilder toBuilder() {
    val builder = GemErpMedicationBuilder.forKombiPckg();

    if (!builderConsumers.containsKey(CONTAINED_MEDICATIONS_KEY)
        && !builderConsumers.containsKey(CONTAINED_ATC_MEDICATIONS_KEY)
        && !builderConsumers.containsKey(CONTAINED_MEDICATION_LIST_KEY)) {
      this.withContainedAtcMedications(
          List.of(ATC.from(getRandomDigitsOf(8)), ATC.from(getRandomDigitsOf(8))));
    }

    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }

  private String getRandomFourDigitsCode() {
    return getFaker().regexify("[0-9]{1,4}");
  }

  private String getRandomDigitsOf(int length) {
    return getFaker().regexify("[0-9]{" + length + "}");
  }
}
