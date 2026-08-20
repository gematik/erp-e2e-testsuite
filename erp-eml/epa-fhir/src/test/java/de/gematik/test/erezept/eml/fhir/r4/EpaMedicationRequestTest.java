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

package de.gematik.test.erezept.eml.fhir.r4;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.DosageDgMPBuilder;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.TimingBuilder;
import de.gematik.test.erezept.eml.fhir.valuesets.BmpDosiereinheit;
import java.util.Date;
import lombok.val;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EpaMedicationRequestTest {

  private static final EpaMedicationRequest medRequest = new EpaMedicationRequest();

  @BeforeAll
  static void setup() {
    val dosageDGMP =
        DosageDgMPBuilder.dosageBuilder(3, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .period(3, Timing.UnitsOfTime.D)
                    .build())
            .build();
    medRequest
        .setAuthoredOn(new Date(1737500400000L))
        .setDispenseRequest(
            new MedicationRequest.MedicationRequestDispenseRequestComponent()
                .setQuantity(Quantity.fromUcum("1", "{Package}")))
        .addDosageInstruction(dosageDGMP)
        .addDosageInstruction(dosageDGMP)
        .setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE);
  }

  @Test
  void shouldGetCorrectStaus() {
    assertEquals(MedicationRequest.MedicationRequestStatus.ACTIVE, medRequest.getStatus());
  }

  @Test
  void shouldGetAuthoredOnDate() {
    assertNotNull(medRequest.getAuthoredOn());
    assertEquals(new Date(1737500400000L), medRequest.getAuthoredOn());
  }

  @Test
  void shouldGetDispenseRequest() {
    assertNotNull(medRequest.getEmlDisReqQuantity());
    assertEquals(1, medRequest.getEmlDisReqQuantity().getValue().intValue());
  }

  @Test
  void shouldGetDosageDgmp() {
    assertNotNull(medRequest.getDosageInstructionDgMPs());
    assertEquals(2, medRequest.getDosageInstructionDgMPs().size());
  }
}
