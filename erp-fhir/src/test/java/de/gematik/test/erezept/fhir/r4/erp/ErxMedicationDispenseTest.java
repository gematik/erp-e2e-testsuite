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

package de.gematik.test.erezept.fhir.r4.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.val;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;

class ErxMedicationDispenseTest extends ErpFhirParsingTest {
  private static final String BASE_PATH = "fhir/valid/erp/1.4.0/medicationdispense/";

  @Test
  void shouldEncodeSingleMedicationDispense() {
    val fileName = "MedicationDispense.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val medicationDispense = parser.decode(ErxMedicationDispense.class, content);
    assertNotNull(medicationDispense, "Valid MedicationDispense must be parseable");
    assertEquals("160.000.033.491.280.78", medicationDispense.getPrescriptionId().getValue());
    assertFalse(medicationDispense.isDiGA());

    assertEquals("X123456789", medicationDispense.getSubjectId().getValue());
    assertEquals("3-SMC-B-Testkarte-883110000095957", medicationDispense.getPerformerIdFirstRep());

    // Note: expectedHandedOver might change at winter time (written at 05.10.2021)
    val expectedHandedOver =
        ZonedDateTime.of(LocalDateTime.of(2024, 4, 3, 0, 0), ZoneId.systemDefault());
    assertEquals(expectedHandedOver, medicationDispense.getZonedWhenHandedOver());
  }

  @Test
  void shouldCastFromMedicationDispense() {
    val fileName = "MedicationDispense.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    Resource medicationDispense = parser.decode(MedicationDispense.class, content);
    val erxMedicationDispense = ErxMedicationDispense.fromMedicationDispense(medicationDispense);
    assertNotNull(erxMedicationDispense);
  }

  @Test
  void shouldGetFromMedicationDispenseTheReedemCode() {
    val fileName = "MedicationDispense-DiGA-Name-And-PZN.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val erxMedicationDispense = parser.decode(ErxMedicationDispense.class, content);
    assertNotNull(erxMedicationDispense);
    assertEquals("DE12345678901234", erxMedicationDispense.getRedeemCode().get().getValue());
  }
}
