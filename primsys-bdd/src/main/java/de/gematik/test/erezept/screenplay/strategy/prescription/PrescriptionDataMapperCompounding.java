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
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationCompoundingFaker;
import de.gematik.test.erezept.fhir.extensions.kbv.ProductionInstruction;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.valuesets.*;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.List;
import java.util.Map;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

public class PrescriptionDataMapperCompounding extends PrescriptionDataMapper {

  public PrescriptionDataMapperCompounding(
      Actor patient,
      PrescriptionAssignmentKind prescriptionAssignmentKind,
      List<Map<String, String>> medications) {
    super(medications, patient, prescriptionAssignmentKind);
  }

  @Override
  protected KbvErpMedication getKbvErpMedication(Map<String, String> medMap) {
    val pzn = getOrDefault("PZN", PZN.random().getValue(), medMap);
    val name = getOrDefault("Name", GemFaker.fakerDrugName(), medMap);
    val category = getOrDefault("Verordnungskategorie", "00", medMap);
    val isVaccine = Boolean.parseBoolean(getOrDefault("Impfung", "false", medMap));
    val darreichungsMenge = getOrDefault("Darreichungsmenge", "1", medMap);
    val medCodeText = getOrDefault("Rezepturname", "Zinkpaste DAB2020", medMap);
    val freiTextInPzn = getOrDefault("Freitext", "123", medMap);
    val compoundingNumerator = Long.parseLong(getOrDefault("Numerator", "312", medMap));
    val compoundingNumeratorUnit = getOrDefault("NumeratorUnit", "mg", medMap);

    return KbvErpMedicationCompoundingFaker.builder()
        .withCategory(MedicationCategory.fromCode(category))
        .withProductionInstruction(ProductionInstruction.asCompounding(medCodeText))
        .withMedicationIngredient(pzn, name, freiTextInPzn)
        .withVaccine(isVaccine)
        .withAmount(compoundingNumerator, compoundingNumeratorUnit)
        .withAmount(Integer.decode(darreichungsMenge))
        .toBuilder()
        .build();
  }
}
