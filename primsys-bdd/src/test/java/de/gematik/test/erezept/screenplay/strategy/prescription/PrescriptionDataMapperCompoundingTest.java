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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.List;
import java.util.Map;
import net.serenitybdd.screenplay.Actor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PrescriptionDataMapperCompoundingTest {

  @Mock private Actor patient;
  @Mock private PrescriptionAssignmentKind type;
  private PrescriptionDataMapperCompounding prescriptionDataMapper;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    List<Map<String, String>> medications = List.of(Map.of("key", "value"));
    prescriptionDataMapper = new PrescriptionDataMapperCompounding(patient, type, medications);
  }

  @Test
  void shouldGetKbvErpMedicationCorrect() {
    Map<String, String> medMap =
        Map.of(
            "Name", "TestDrug",
            "Verordnungskategorie", "00",
            "Impfung", "false",
            "Amount", "1",
            "Numerator", "312",
            "Denominator", "1",
            "NumeratorUnit", "mg",
            "Normgröße", "NB");
    KbvErpMedication medication = prescriptionDataMapper.getKbvErpMedication(medMap);
    assertNotNull(medication);
    assertEquals("TestDrug", medication.getIngredientFirstRep().getItemCodeableConcept().getText());
  }

  @Test
  void shouldFailWhileGettingKbvErpMedication() {
    Map<String, String> medMap = null;
    assertThrows(
        NullPointerException.class, () -> prescriptionDataMapper.getKbvErpMedication(medMap));
  }
}
