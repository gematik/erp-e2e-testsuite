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

import static org.hl7.fhir.r4.model.MedicationDispense.MedicationDispenseStatus.COMPLETED;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import de.gematik.test.erezept.eml.fhir.values.RxPrescriptionId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EpaOpProvideDispensationTest {

  private static final String content =
      ResourceLoader.readFileFromResource(
          "fhir/valid/medication/Parameters-example-epa-op-provide-dispensation-erp-input-parameters-1.json");

  private static EpaOpProvideDispensation epaOpProvideDispensation;
  private static FhirCodec fhir;

  @BeforeAll
  static void setup() {
    fhir = EpaFhirFactory.create();
    epaOpProvideDispensation = fhir.decode(EpaOpProvideDispensation.class, content);
  }

  @Test
  void getEpaAuthoredOn() {
    assertEquals(
        new Date(2025 - 1900, Calendar.JANUARY, 22), epaOpProvideDispensation.getEpaAuthoredOn());
  }

  @Test
  void getWhenHandedOver() {
    assertEquals(
        new Date(2025 - 1900, Calendar.JANUARY, 22),
        epaOpProvideDispensation.getEpaWhenHandedOver());
  }

  @Test
  void shouldFailWhileGetWhenHandedOver() {
    assertNotEquals(
        new Date(2024 - 1900, Calendar.JANUARY, 22),
        epaOpProvideDispensation.getEpaWhenHandedOver());
  }

  @Test
  void ShouldThrowWhileMissingContentWhenHandedOver() {
    val provDispForManipulation = fhir.decode(EpaOpProvideDispensation.class, content);
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
