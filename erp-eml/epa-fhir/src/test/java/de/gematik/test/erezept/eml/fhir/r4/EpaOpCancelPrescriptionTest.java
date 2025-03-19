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

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.eml.fhir.testutil.EpaFhirParsingTest;
import java.util.Calendar;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EpaOpCancelPrescriptionTest extends EpaFhirParsingTest {
  private static final String EPA_OP_CANCEL_PRESC_AS_STRING =
      ResourceLoader.readFileFromResource(
          "fhir/valid/medication/Parameters-example-epa-op-cancel-prescription-erp-input-parameters-1.json");

  private static EpaOpCancelPrescription epaOpCancelPrescription;

  @BeforeAll
  static void setup() {
    epaOpCancelPrescription =
        epaFhir.decode(EpaOpCancelPrescription.class, EPA_OP_CANCEL_PRESC_AS_STRING);
  }

  @Test
  void shouldGetEpaAuthoredOnDateCorrect() {
    val calendar = Calendar.getInstance();
    calendar.set(2025, Calendar.JANUARY, 22, 0, 0, 0);
    calendar.clear(Calendar.MILLISECOND);

    val expected = calendar.getTime();
    assertEquals(expected, epaOpCancelPrescription.getEpaAuthoredOn());
  }

  @Test
  void shouldGetEpaPrescriptionIdCorrect() {
    val id = epaOpCancelPrescription.getEpaPrescriptionId();
    assertEquals("160.153.303.257.459", id.getValue());
  }
}
