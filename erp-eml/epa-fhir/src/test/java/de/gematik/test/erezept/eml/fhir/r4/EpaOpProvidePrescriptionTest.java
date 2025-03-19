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

package de.gematik.test.erezept.eml.fhir.r4;

import static org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestStatus.ACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.eml.fhir.testutil.EpaFhirParsingTest;
import de.gematik.test.erezept.eml.fhir.values.RxPrescriptionId;
import java.util.Calendar;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EpaOpProvidePrescriptionTest extends EpaFhirParsingTest {

  private static EpaOpProvidePrescription epaOpProvidePrescription;

  @BeforeAll
  static void setup() {
    val resourcePath =
        "fhir/forunittests/Parameters-example-epa-op-provide-prescription-erp-input-parameters-2.json";
    val content = ResourceLoader.readFileFromResource(resourcePath);
    epaOpProvidePrescription = epaFhir.decode(EpaOpProvidePrescription.class, content);
  }

  @Test
  void shouldGetEpaPrescriptionId() {
    assertEquals(
        RxPrescriptionId.from("160.153.303.257.459"),
        epaOpProvidePrescription.getEpaPrescriptionId());
  }

  @Test
  void shouldGetEpaAuthoredOn() {
    val calendar = Calendar.getInstance();
    calendar.set(2025, Calendar.JANUARY, 22, 0, 0, 0);
    calendar.clear(Calendar.MILLISECOND);

    val expected = calendar.getTime();
    assertEquals(expected, epaOpProvidePrescription.getEpaAuthoredOn());
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
