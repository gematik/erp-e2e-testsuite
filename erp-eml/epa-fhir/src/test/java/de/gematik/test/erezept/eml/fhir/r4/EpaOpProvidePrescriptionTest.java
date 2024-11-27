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

import static org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestStatus.ACTIVE;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EpaOpProvidePrescriptionTest {

  private static EpaOpProvidePrescription epaOpProvidePrescription;

  @BeforeAll
  static void setup() {
    val fhir = EpaFhirFactory.create();
    val BASE_PATH =
        "fhir/valid/medication/Parameters-example-epa-op-provide-prescription-erp-input-parameters-2.json";
    val content = ResourceLoader.readFileFromResource(BASE_PATH);
    epaOpProvidePrescription = fhir.decode(EpaOpProvidePrescription.class, content);
  }

  @Test
  void shouldGetEpaPrescriptionId() {
    assertEquals(
        PrescriptionId.from("160.153.303.257.459"),
        epaOpProvidePrescription.getEpaPrescriptionId());
  }

  @Test
  void shouldGetEpaAuthoredOn() {
    assertEquals(
        new Date(2025 - 1900, Calendar.JANUARY, 22), epaOpProvidePrescription.getEpaAuthoredOn());
  }

  @Test
  void shouldGetEpaMedicationRequest() {
    assertNotNull(epaOpProvidePrescription.getEpaMedicationRequest());
    assertEquals(ACTIVE, epaOpProvidePrescription.getEpaMedicationRequest().getStatus());
  }

  @Test
  void shouldGetEpaMedication() {
    val medication = epaOpProvidePrescription.getEpaMedication();
    assertNotNull(medication);
    assertEquals(
        Optional.of(PZN.from("10019621")), epaOpProvidePrescription.getEpaMedication().getPzn());
  }

  @Test
  void shouldGetEpaOrganisation() {
    val organization = epaOpProvidePrescription.getEpaOrganisation();
    assertEquals(TelematikID.from("9-2.58.00000040"), organization.getTelematikId());
  }

  @Test
  void shouldGetEpaPractitioner() {
    val practitioner = epaOpProvidePrescription.getEpaPractitioner();
    assertEquals(TelematikID.from("1-1.58.00000040"), practitioner.getTelematikId());
  }
}
