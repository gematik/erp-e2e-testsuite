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

package de.gematik.test.erezept.fhir.r4.dgmp;

import static de.gematik.test.erezept.fhir.builder.dgmp.RenderedDosageInstructionUtil.render;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.DosageDgMPBuilder;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.TimingBuilder;
import de.gematik.test.erezept.eml.fhir.r4.dgmp.DosageDgMP;
import de.gematik.test.erezept.eml.fhir.valuesets.BmpDosiereinheit;
import java.util.ArrayList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.Test;

class DosageDgMPRendererTest {

  @Test
  void shouldRenderWIthDosageList() {
    List<DosageDgMP> dosageDgMPList = new ArrayList<>();
    dosageDgMPList.add(
        DosageDgMPBuilder.dosageBuilder(2, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(1, Timing.UnitsOfTime.D)
                    .frequency(1)
                    .timeOfDay("22:00:00")
                    .build())
            .build());
    dosageDgMPList.add(
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(1, Timing.UnitsOfTime.D)
                    .frequency(1)
                    .timeOfDay("18:00:00")
                    .build())
            .build());
    dosageDgMPList.add(
        DosageDgMPBuilder.dosageBuilder(3, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(1, Timing.UnitsOfTime.D)
                    .frequency(1)
                    .timeOfDay("01:00:00")
                    .build())
            .build());

    val rendered = render(dosageDgMPList);
    assertEquals(
        "täglich: 22:00 Uhr — je 2 Stück; 18:00 Uhr — je 1 Stück; 01:00 Uhr — je 3 Stück",
        rendered);
  }

  @Test
  void shouldRenderWithDecimalDosage() {
    List<DosageDgMP> dosageDgMPList = new ArrayList<>();
    dosageDgMPList.add(
        DosageDgMPBuilder.dosageBuilder(2.5, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(1, Timing.UnitsOfTime.D)
                    .frequency(1)
                    .timeOfDay("22:00:00")
                    .build())
            .build());
    dosageDgMPList.add(
        DosageDgMPBuilder.dosageBuilder(1.25, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(1, Timing.UnitsOfTime.D)
                    .frequency(1)
                    .timeOfDay("18:00:00")
                    .build())
            .build());
    dosageDgMPList.add(
        DosageDgMPBuilder.dosageBuilder(3, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(1, Timing.UnitsOfTime.D)
                    .frequency(1)
                    .timeOfDay("01:00:00")
                    .build())
            .build());

    val rendered = render(dosageDgMPList);
    assertEquals(
        "täglich: 22:00 Uhr — je 2,5 Stück; 18:00 Uhr — je 1,25 Stück; 01:00 Uhr — je 3 Stück",
        rendered);
  }
}
