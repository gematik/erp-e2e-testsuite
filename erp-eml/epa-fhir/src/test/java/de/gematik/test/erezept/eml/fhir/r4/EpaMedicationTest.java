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

package de.gematik.test.erezept.eml.fhir.r4;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EpaMedicationTest {

  private static final String MEDICATION_AS_STRING =
      ResourceLoader.readFileFromResource(
          "fhir/valid/medication/Medication-SumatripanMedication.json");
  private static final String ASK_MEDIC_AS_STRING =
      ResourceLoader.readFileFromResource(
          "fhir/valid/medication/ASK-Medication-4f9ab221-0eef-4e46-a8a9-38302e0488b1.json");

  private static EpaMedication medication;
  private static EpaMedication askMedication;

  @BeforeAll
  static void setup() {
    val fh = EpaFhirFactory.create();
    medication = fh.decode(EpaMedication.class, MEDICATION_AS_STRING);
    askMedication = fh.decode(EpaMedication.class, ASK_MEDIC_AS_STRING);
  }

  @Test
  void shouldGetPzn() {
    assertTrue(medication.getPzn().isPresent());
    assertTrue(medication.getPzn().get().getValue().length() > 0);
  }

  @Test
  void shouldDeliverAsk() {
    val askNo = askMedication.getAsk();
    assertFalse(askNo.isEmpty());
  }

  @Test
  void shouldDeliverAtc() {
    val askNo = askMedication.getAtc();
    assertTrue(askNo.isPresent());
  }

  @Test
  void getPresIdentShouldWork() {
    val id = askMedication.getRxPrescriptionId();
    assertEquals("160.153.303.257.459_20250122", id.orElseThrow().getValue());
  }
}
