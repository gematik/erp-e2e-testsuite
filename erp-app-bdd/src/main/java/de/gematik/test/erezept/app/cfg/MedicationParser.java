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

package de.gematik.test.erezept.app.cfg;

import static de.gematik.test.erezept.fhir.valuesets.MedicationType.COMPOUNDING;
import static de.gematik.test.erezept.fhir.valuesets.MedicationType.INGREDIENT;

import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import java.util.Arrays;
import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public class MedicationParser {

  public static boolean compareMedicationNames(String medicationNameOne, String medicationNameTwo) {
    val medicationNameOneAsArray = medicationNameOne.split(", ");
    val medicationNameTwoAsArray = medicationNameTwo.split(", ");

    Arrays.sort(medicationNameOneAsArray);
    Arrays.sort(medicationNameTwoAsArray);

    return Arrays.equals(medicationNameOneAsArray, medicationNameTwoAsArray);
  }

  public static String getMedicationName(KbvErpMedication medication) {
    if (isWirkstoffOrRezeptur(medication)) {
      return getNameFromIngredients(medication);
    } else {
      return getNameFromCode(medication);
    }
  }

  private static boolean isWirkstoffOrRezeptur(KbvErpMedication medication) {
    val medicationType = medication.getMedicationType();

    return medicationType.isPresent()
        && (medicationType.get().equals(INGREDIENT) || medicationType.get().equals(COMPOUNDING));
  }

  private static String getNameFromCode(KbvErpMedication medication) {
    return medication.getCode().getText();
  }

  private static String getNameFromIngredients(KbvErpMedication medication) {
    val ingredientsList =
        medication.getIngredient().stream().map(s -> s.getItemCodeableConcept().getText()).toList();

    return String.join(", ", ingredientsList);
  }
}
