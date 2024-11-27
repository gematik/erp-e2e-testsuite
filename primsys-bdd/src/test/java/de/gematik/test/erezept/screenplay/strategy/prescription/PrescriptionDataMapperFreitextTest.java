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

class PrescriptionDataMapperFreitextTest {

  @Mock private Actor patient;
  @Mock private PrescriptionAssignmentKind type;
  private PrescriptionDataMapperFreitext prescriptionDataMapper;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    List<Map<String, String>> medications = List.of(Map.of("key", "value"));
    prescriptionDataMapper = new PrescriptionDataMapperFreitext(patient, type, medications);
  }

  @Test
  void shouldGetKbvErpMedicationCorrect() {
    Map<String, String> medMap =
        Map.of(
            "Verordnungskategorie", "00",
            "Impfung", "false",
            "Freitext", "TestFreitext",
            "Darreichungsform", "TAB",
            "Darreichungsmenge", "1");
    KbvErpMedication medication = prescriptionDataMapper.getKbvErpMedication(medMap);
    assertNotNull(medication);
    assertEquals("TestFreitext", medication.getFreeText());
  }

  @Test
  void shouldGetKbvErpMedicationCorrectWithMissingArguments() {
    Map<String, String> medMap = Map.of("Verordnungskategorie", "00");
    KbvErpMedication medication = prescriptionDataMapper.getKbvErpMedication(medMap);
    assertNotNull(medication);
    assertNotNull(medication.getFreeText());
    assertFalse(medication.isVaccine());
    assertTrue(medication.getForm().getText().contains("Tablette"));
    assertTrue(medication.getForm().getText().contains("1"));
  }

  @Test
  void shouldFailWhileGettingKbvErpMedication() {
    Map<String, String> medMap = null;
    assertThrows(
        NullPointerException.class, () -> prescriptionDataMapper.getKbvErpMedication(medMap));
  }
}
