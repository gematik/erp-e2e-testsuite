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

package de.gematik.test.erezept.arguments;

import static de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe.GKV;
import static de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe.PKV;
import static de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType.FLOW_TYPE_166;

import de.gematik.test.core.ArgumentComposer;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MedicationTypeAndInsuranceComposer {

  public static final String MEDICATION_PZN = "Medication PZN";
  public static final String MEDICATION_INGREDIENT = "Medication Ingredient";
  public static final String MEDICATION_COMPOUNDING = "Medication Compounding";
  public static final String MEDICATION_FREITEXT = "Freitextverordnung";

  public static ArgumentComposer getComposer() {
    return ArgumentComposer.composeWith()
        .arguments(GKV, FLOW_TYPE_166)
        .arguments(PKV, FLOW_TYPE_166)
        .multiply(
            2,
            List.of(
                MEDICATION_PZN,
                MEDICATION_COMPOUNDING,
                MEDICATION_FREITEXT,
                MEDICATION_INGREDIENT));
  }
}
