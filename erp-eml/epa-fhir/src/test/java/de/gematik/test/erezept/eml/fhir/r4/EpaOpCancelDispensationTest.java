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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import java.util.Calendar;
import java.util.Date;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EpaOpCancelDispensationTest {
  private static final String EPA_OP_CANCEL_DISP_AS_STRING =
      ResourceLoader.readFileFromResource(
          "fhir/valid/medication/Parameters-example-epa-op-cancel-dispensation-erp-input-parameters-1.json");

  private static EpaOpCancelDispensation epaOpCancelDispensation;

  @BeforeAll
  static void setup() {
    val fh = EpaFhirFactory.create();
    epaOpCancelDispensation =
        fh.decode(EpaOpCancelDispensation.class, EPA_OP_CANCEL_DISP_AS_STRING);
  }

  @Test
  void shouldGetEpaAuthoredOnDateCorrect() {
    assertEquals(
        new Date(2025 - 1900, Calendar.JANUARY, 22), epaOpCancelDispensation.getEpaAuthoredOn());
  }

  @Test
  void shouldGetEpaPrescriptionIdCorrect() {
    val id = epaOpCancelDispensation.getEpaPrescriptionId();
    assertEquals("160.153.303.257.459", id.getValue());
  }
}
