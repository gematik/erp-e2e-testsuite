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

package de.gematik.test.erezept.screenplay.strategy.prescription;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

class PrescriptionDataMapperIngredientTest extends ErpFhirParsingTest {

  @Mock private Actor patient;

  @Test
  void shouldGetKbvErpMedicationCorrect() {
    val medMap =
        Map.of(
            "Name", "TestDrug",
            "Verordnungskategorie", "00",
            "Impfung", "false",
            "Amount", "1",
            "Numerator", "312",
            "Denominator", "1",
            "NumeratorUnit", "mg",
            "Normgröße", "NB");
    val medications = List.of(Map.of("key", "value"));
    val prescriptionDataMapper =
        new PrescriptionDataMapperIngredient(
            patient, PrescriptionAssignmentKind.PHARMACY_ONLY, medications);
    val medication = prescriptionDataMapper.getKbvErpMedication(medMap);

    assertNotNull(medication);
    assertEquals("TestDrug", medication.getIngredientFirstRep().getItemCodeableConcept().getText());

    val vr = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(vr.isSuccessful());
  }

  @Test
  void shouldFailWhileGettingKbvErpMedication() {
    Map<String, String> medMap = null;
    val medications = List.of(Map.of("key", "value"));
    val prescriptionDataMapper =
        new PrescriptionDataMapperIngredient(
            patient, PrescriptionAssignmentKind.PHARMACY_ONLY, medications);
    assertThrows(
        NullPointerException.class, () -> prescriptionDataMapper.getKbvErpMedication(medMap));
  }
}
