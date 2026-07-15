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

package de.gematik.test.erezept.eml.fhir.r4.componentbuilder;

import static de.gematik.test.erezept.eml.fhir.profile.UseFulCodeSystems.UCUM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.DosageDgMPBuilder;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.TimingBuilder;
import de.gematik.test.erezept.eml.fhir.r4.dgmp.DosageDgMP;
import de.gematik.test.erezept.eml.fhir.valuesets.BmpDosiereinheit;
import lombok.val;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.Test;

class DosageDgMPBuilderTest {

  @Test
  void buildSimpleDosage() {
    DosageDgMP dosage =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG).text("1 Tablette morgens").build();
    assertNotNull(dosage);
    assertEquals("1 Tablette morgens", dosage.getText());
    assertEquals(1, dosage.getDoseAndRate().size());
    assertEquals("v", ((Quantity) dosage.getDoseAndRate().get(0).getDose()).getCode());
    assertEquals("mg", ((Quantity) dosage.getDoseAndRate().get(0).getDose()).getUnit());
    // FHIR-Validation and Assertion in ErxMedicationDispenseFakerTest
  }

  @Test
  void buildSimpleDosageWithTiming() {

    DosageDgMP dosage =
        DosageDgMPBuilder.dosageBuilder(2, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .build())
            .build();
    assertEquals(3, dosage.getTiming().getRepeat().getPeriod().intValue());
    assertEquals(1, dosage.getTiming().getRepeat().getFrequency());
    assertEquals(Timing.UnitsOfTime.D, dosage.getTiming().getRepeat().getPeriodUnit());
    assertEquals(1, dosage.getTiming().getRepeat().getTimeOfDay().size());
    assertEquals("08:00:00", dosage.getTiming().getRepeat().getTimeOfDay().get(0).getValue());
  }

  @Test
  void shouldBuildWithSpecialSystem() {
    val value = 2;
    val unit = "Tablette";
    val system = UCUM.getCanonicalUrl();
    val code = "1";
    DosageDgMP dosage =
        DosageDgMPBuilder.dosageBuilder(value, unit, code)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(2)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .build())
            .build();
    assertEquals(system, dosage.getDoseAndRate().get(0).getDoseQuantity().getSystem());
    assertEquals(unit, dosage.getDoseAndRate().get(0).getDoseQuantity().getUnit());
    assertEquals(code, dosage.getDoseAndRate().get(0).getDoseQuantity().getCode());
    assertEquals(value, dosage.getDoseAndRate().get(0).getDoseQuantity().getValue().intValue());
  }
}
