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

package de.gematik.test.erezept.fhir.resources.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.val;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;

class ErxMedicationDispenseTest extends ParsingTest {
  private static final String BASE_PATH = "fhir/valid/erp/1.1.1/";

  @Test
  void shouldEncodeSingleMedicationDispense() {
    val fileName = "MedicationDispense_01.xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val medicationDispense = parser.decode(ErxMedicationDispense.class, content);
    assertNotNull(medicationDispense, "Valid MedicationDispense must be parseable");
    assertEquals(new PrescriptionId("12345678"), medicationDispense.getPrescriptionId());
    assertFalse(medicationDispense.isDiGA());

    val medication = medicationDispense.getErpMedicationFirstRep();
    assertNotNull(medication, "MedicationDispense must contain at leas one Medication");

    assertEquals(MedicationCategory.C_00, medication.getCategoryFirstRep());
    assertFalse(medication.isVaccine());
    assertEquals("06313728", medication.getPznFirstRep());
    assertEquals("Sumatriptan-1a Pharma 100 mg Tabletten", medication.getMedicationName());
    assertEquals(Darreichungsform.TAB, medication.getDarreichungsformFirstRep().orElseThrow());
    // TODO: implement tests for further convenience methods on demand

    assertEquals("X234567890", medicationDispense.getSubjectId().getValue());
    assertEquals("606358757", medicationDispense.getPerformerIdFirstRep());

    // Note: expectedHandedOver might change at winter time (written at 05.10.2021)
    val expectedHandedOver = ZonedDateTime.of(2020, 3, 20, 3, 13, 0, 0, ZoneId.systemDefault());
    assertEquals(expectedHandedOver, medicationDispense.getZonedWhenHandedOver());
    assertEquals("1-0-1-0", medicationDispense.getDosageInstructionTextFirstRep());
  }

  @Test
  void shouldCastFromMedicationDispense() {
    val fileName = "MedicationDispense_01.xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    Resource medicationDispense = parser.decode(MedicationDispense.class, content);
    val erxMedicationDispense = ErxMedicationDispense.fromMedicationDispense(medicationDispense);
    assertNotNull(erxMedicationDispense);
  }
}
