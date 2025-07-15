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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErxMedicationDispenseDiGATest extends ErpFhirParsingTest {

  private static final String BASE_PATH = "fhir/valid/erp/1.4.0/medicationdispense/";

  @Test
  void shouldDecodeSingleDiGAMedicationDispense() {
    val fileName = "MedicationDispense-DiGA-DeepLink.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val medicationDispense = parser.decode(ErxMedicationDispenseDiGA.class, content);
    assertTrue(medicationDispense.isDiGA());
    assertEquals(
        PrescriptionId.from("162.000.033.491.280.69"), medicationDispense.getPrescriptionId());
  }

  @Test
  void shouldDecodeDiGAMedicationDispenseWithoutType() {
    val fileName = "MedicationDispense-DiGA-Name-And-PZN.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val medicationDispense = parser.decode(content);
    assertInstanceOf(ErxMedicationDispenseDiGA.class, medicationDispense);
  }

  @Test
  void shouldThrowOnMissingPznAndName() {
    val fileName = "MedicationDispense-DiGA-DeepLink.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val medicationDispense = parser.decode(ErxMedicationDispenseDiGA.class, content);

    medicationDispense.setMedication(null);
    assertThrows(MissingFieldException.class, medicationDispense::getPzn);
    assertThrows(MissingFieldException.class, medicationDispense::getDigaName);
  }

  @Test
  void shouldThrowOnMissingPerformer() {
    val fileName = "MedicationDispense-DiGA-DeepLink.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val medicationDispense = parser.decode(ErxMedicationDispenseDiGA.class, content);

    medicationDispense.setPerformer(List.of());
    assertThrows(MissingFieldException.class, medicationDispense::getPerformerIdFirstRep);
  }
}
