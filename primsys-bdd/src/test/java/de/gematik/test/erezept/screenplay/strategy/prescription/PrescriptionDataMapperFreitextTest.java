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

package de.gematik.test.erezept.screenplay.strategy.prescription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.List;
import java.util.Map;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class PrescriptionDataMapperFreitextTest extends ErpFhirParsingTest {

  @Mock private Actor patient;

  @Test
  void shouldGetKbvErpMedicationCorrect() {
    val medMap =
        Map.of(
            "Verordnungskategorie", "00",
            "Impfung", "false",
            "Freitext", "TestFreitext",
            "Darreichungsform", "TAB",
            "Darreichungsmenge", "1");
    val prescriptionDataMapper =
        new PrescriptionDataMapperFreitext(
            patient, PrescriptionAssignmentKind.PHARMACY_ONLY, List.of(Map.of("key", "value")));
    val medication = prescriptionDataMapper.getKbvErpMedication(medMap);

    assertNotNull(medication);
    assertEquals("TestFreitext", medication.getFreeText());
    val vr = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(vr.isSuccessful());
  }

  @Test
  void shouldGetKbvErpMedicationCorrectWithMissingArguments() {
    val medMap = Map.of("Verordnungskategorie", "00");
    val prescriptionDataMapper =
        new PrescriptionDataMapperFreitext(
            patient, PrescriptionAssignmentKind.PHARMACY_ONLY, List.of(Map.of("key", "value")));
    val medication = prescriptionDataMapper.getKbvErpMedication(medMap);

    assertNotNull(medication);
    assertNotNull(medication.getFreeText());
    assertFalse(medication.isVaccine());
    assertTrue(medication.getForm().getText().contains("Tablette"));
    assertTrue(medication.getForm().getText().contains("1"));
  }

  @Test
  void shouldFailWhileGettingKbvErpMedication() {
    Map<String, String> medMap = null;
    val prescriptionDataMapper =
        new PrescriptionDataMapperFreitext(
            patient, PrescriptionAssignmentKind.PHARMACY_ONLY, List.of(Map.of("key", "value")));
    assertThrows(
        NullPointerException.class, () -> prescriptionDataMapper.getKbvErpMedication(medMap));
  }
}
