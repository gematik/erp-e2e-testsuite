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

package de.gematik.test.erezept.arguments;

import static de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe.*;
import static de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType.*;
import static de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind.DIRECT_ASSIGNMENT;
import static de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind.PHARMACY_ONLY;

import de.gematik.test.core.ArgumentComposer;
import java.util.List;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.jupiter.params.provider.Arguments;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkflowAndMedicationComposer {

  public static final String MEDICATION_PZN = "Medication PZN";
  public static final String MEDICATION_INGREDIENT = "Medication Ingredient";
  public static final String MEDICATION_COMPOUNDING = "Medication Compounding";
  public static final String MEDICATION_FREITEXT = "Freitextverordnung";

  public static ArgumentComposer workflowAndMedicationComposer() {
    return ArgumentComposer.composeWith()
        .arguments(GKV, PHARMACY_ONLY, FLOW_TYPE_160)
        .arguments(GKV, DIRECT_ASSIGNMENT, FLOW_TYPE_169)
        .arguments(PKV, PHARMACY_ONLY, FLOW_TYPE_200)
        .arguments(PKV, DIRECT_ASSIGNMENT, FLOW_TYPE_209)
        .multiply(
            3,
            List.of(
                MEDICATION_PZN,
                MEDICATION_COMPOUNDING,
                MEDICATION_FREITEXT,
                MEDICATION_INGREDIENT));
  }

  public static Stream<Arguments> workflowPharmacyOnlyAndMedicationComposer() {
    return ArgumentComposer.composeWith()
        .arguments(GKV, PHARMACY_ONLY, FLOW_TYPE_160)
        .arguments(PKV, PHARMACY_ONLY, FLOW_TYPE_200)
        .multiply(
            3,
            List.of(
                MEDICATION_PZN, MEDICATION_COMPOUNDING, MEDICATION_FREITEXT, MEDICATION_INGREDIENT))
        .create();
  }
}
