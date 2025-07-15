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

package de.gematik.test.erezept.eml.fhir.r4;

import static org.hl7.fhir.r4.model.MedicationDispense.MedicationDispenseStatus.COMPLETED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.eml.fhir.testutil.EpaFhirParsingTest;
import de.gematik.test.erezept.eml.fhir.values.RxPrescriptionId;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EpaOpProvideDispensationTest extends EpaFhirParsingTest {

  private static final String content =
      ResourceLoader.readFileFromResource(
          "fhir/valid/parameters/Parameters-example-epa-op-provide-dispensation-erp-input-parameters-1.json");

  private static EpaOpProvideDispensation epaOpProvideDispensation;

  @BeforeAll
  static void setup() {
    epaOpProvideDispensation = epaFhir.decode(EpaOpProvideDispensation.class, content);
  }

  @Test
  void getEpaAuthoredOn() {
    val calendar = Calendar.getInstance();
    calendar.set(2025, Calendar.JANUARY, 22, 0, 0, 0);
    calendar.clear(Calendar.MILLISECOND);

    val expected = calendar.getTime();
    assertEquals(expected, epaOpProvideDispensation.getEpaAuthoredOn());
  }

  @Test
  void getWhenHandedOver() {
    val calendar = Calendar.getInstance();
    calendar.set(2025, Calendar.JANUARY, 22, 0, 0, 0);
    calendar.clear(Calendar.MILLISECOND);

    val expected = calendar.getTime();
    assertEquals(expected, epaOpProvideDispensation.getEpaWhenHandedOver());
  }

  @Test
  void shouldFailWhileGetWhenHandedOver() {
    val calendar = Calendar.getInstance();
    calendar.set(2024, Calendar.JANUARY, 22, 0, 0, 0);
    calendar.clear(Calendar.MILLISECOND);

    val expected = calendar.getTime();
    assertNotEquals(expected, epaOpProvideDispensation.getEpaWhenHandedOver());
  }

  @Test
  void shouldThrowWhileMissingContentWhenHandedOver() {
    val provDispForManipulation = epaFhir.decode(EpaOpProvideDispensation.class, content);
    provDispForManipulation.getParameter().stream()
        .filter(p -> p.getName().equals("rxDispensation"))
        .findFirst()
        .orElseThrow()
        .setPart(List.of());

    assertThrows(MissingFieldException.class, provDispForManipulation::getEpaWhenHandedOver);
  }

  @Test
  void getEpaPrescriptionId() {
    assertEquals(
        RxPrescriptionId.from("160.153.303.257.459"),
        epaOpProvideDispensation.getEpaPrescriptionId());
  }

  @Test
  void getEpaMedicationDispense() {
    assertNotNull(epaOpProvideDispensation.getEpaMedicationDispense());
    assertEquals(COMPLETED, epaOpProvideDispensation.getEpaMedicationDispense().getStatus());
  }

  @Test
  void getEpaMedication() {
    val medication = epaOpProvideDispensation.getEpaMedication();
    assertNotNull(medication);
    assertEquals(
        Optional.of(PZN.from("10019621")), epaOpProvideDispensation.getEpaMedication().getPzn());
  }

  @Test
  void getEpaOrganisation() {
    val organization = epaOpProvideDispensation.getEpaOrganisation();
    assertEquals(TelematikID.from("9-2.58.00000040"), organization.getTelematikId());
  }
}
