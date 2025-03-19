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

package de.gematik.test.erezept.eml.fhir.r4;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.Test;

class EpaMedicationDispenseTest {

  @Test
  void getPerformer() {
    val performer = new MedicationDispense.MedicationDispensePerformerComponent();
    performer.setActor(new Reference("Organization/3856402c-7636-4fbd-98bd-d278852b8e88"));

    val medicationDispense = new EpaMedicationDispense();
    medicationDispense.setPerformer(List.of(performer));
    assertEquals(
        Optional.of("Organization/3856402c-7636-4fbd-98bd-d278852b8e88"),
        medicationDispense.getEpaPerformer());
  }

  @Test
  void getEpaMedicationReference() {
    val testString = "Medication/3b990824-3814-4d75-80b1-e2935827f8f0";

    val medicationDispense = new EpaMedicationDispense();
    medicationDispense.setMedication(new Reference().setReference(testString));
    assertEquals(testString, medicationDispense.getEpaMedicationReference());
  }
}
