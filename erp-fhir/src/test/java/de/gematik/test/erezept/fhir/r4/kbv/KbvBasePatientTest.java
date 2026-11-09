/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.fhir.r4.kbv;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvPatientFaker;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import lombok.val;
import org.junit.jupiter.api.Test;

class KbvBasePatientTest extends ErpFhirParsingTest {

  @Test
  void encodeErwinFischer() {
    val expectedID = "93866fdc-3e50-4902-a7e9-891b54737b5e";
    val patient =
        KbvPatientFaker.builder()
            .withName("Ludger", "Königsstein")
            .withKvnrAndInsuranceType(KVNR.from("K220635158"), InsuranceTypeDe.GKV)
            .fake();
    patient.setId(expectedID);
    assertNotNull(patient);

    assertEquals(expectedID, patient.getLogicalId());
    assertTrue(patient.hasGkvKvnr());
    val expectedGkvKvnr = "K220635158";
    assertEquals(expectedGkvKvnr, patient.getGkvId().orElseThrow().getValue());

    assertEquals("Ludger Königsstein", patient.getFullname());
    assertNotNull(patient.getDescription());
  }

  @Test
  void shouldGetKbvPatientFromResource() {
    val bundle = KbvErpBundleFaker.builder().fake();

    val patient = bundle.getPatient();
    assertNotNull(patient);
    assertEquals(KbvPatient.class, patient.getClass());

    val kbvPatient = KbvPatient.fromPatient(patient);
    assertNotNull(kbvPatient);
    assertEquals(KbvPatient.class, kbvPatient.getClass());
  }
}
