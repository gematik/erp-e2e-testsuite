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

package de.gematik.test.erezept.screenplay.strategy.prescription;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationIngredientFaker;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.List;
import java.util.Map;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

public class PrescriptionDataMapperIngredient extends PrescriptionDataMapper {

  public PrescriptionDataMapperIngredient(
      Actor patient,
      PrescriptionAssignmentKind prescriptionAssignmentKind,
      List<Map<String, String>> medications) {
    super(medications, patient, prescriptionAssignmentKind);
  }

  @Override
  protected KbvErpMedication getKbvErpMedication(Map<String, String> medMap) {
    val name = getOrDefault("Name", GemFaker.fakerDrugName(), medMap);
    val category = getOrDefault("Verordnungskategorie", "00", medMap);
    val isVaccine = Boolean.parseBoolean(getOrDefault("Impfung", "false", medMap));
    val darreichungsMenge = getOrDefault("Amount", "1", medMap);
    val ingredientNumerator = Long.parseLong(getOrDefault("Numerator", "312", medMap));
    val ingredientDenominator = Long.parseLong(getOrDefault("Denominator", "1", medMap));
    val ingredientNumeratorUnit = getOrDefault("NumeratorUnit", "mg", medMap);
    val size = getOrDefault("Normgröße", "NB", medMap);

    return KbvErpMedicationIngredientFaker.builder()
        .withCategory(MedicationCategory.fromCode(category))
        .withAmount(darreichungsMenge)
        .withDrugName(name)
        .withIngredientComponent(
            ingredientNumerator, ingredientDenominator, ingredientNumeratorUnit)
        .withCategory(MedicationCategory.fromCode(category))
        .withStandardSize(StandardSize.fromCode(size))
        .withVaccine(isVaccine)
        .fake();
  }
}
