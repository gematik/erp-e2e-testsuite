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
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.DosageDgMPBuilder;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.TimingBuilder;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.UnitsOfTimeDE;
import de.gematik.test.erezept.eml.fhir.r4.dgmp.DosageDgMP;
import de.gematik.test.erezept.eml.fhir.valuesets.BmpDosiereinheit;
import java.util.List;
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
                    .frequency(1)
                    .period(3, Timing.UnitsOfTime.D)
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
  void shouldBuildOnSameWay() {
    val dosage1 =
        DosageDgMPBuilder.dosageBuilder(2, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .frequency(1)
                    .period(3, Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .build())
            .build();

    val dosage2 =
        DosageDgMPBuilder.dosageTimeOfDayEntry(
            2, BmpDosiereinheit.MG, List.of("08:00:00"), 3, Timing.UnitsOfTime.D);
    assertEquals(
        dosage1.getTiming().getRepeat().getPeriodUnit(),
        dosage2.getTiming().getRepeat().getPeriodUnit());
    assertEquals(
        dosage1.getTiming().getRepeat().getPeriod(), dosage2.getTiming().getRepeat().getPeriod());
    assertEquals(
        dosage1.getTiming().getRepeat().getFrequency(),
        dosage2.getTiming().getRepeat().getFrequency());
    assertEquals(
        dosage1.getTiming().getRepeat().getTimeOfDay().getFirst().getHour(),
        dosage2.getTiming().getRepeat().getTimeOfDay().getFirst().getHour());
    assertEquals(
        dosage1.getDoseAndRateFirstRep().getDoseQuantity().getValue(),
        dosage2.getDoseAndRateFirstRep().getDoseQuantity().getValue());
    assertEquals(
        dosage1.getDoseAndRateFirstRep().getDoseQuantity().getSystem(),
        dosage2.getDoseAndRateFirstRep().getDoseQuantity().getSystem());
  }

  @Test
  void shouldBuildTimeOfDayWithBoundsOnSameWay() {
    val dosage1 =
        DosageDgMPBuilder.dosageBuilder(2, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(7, UnitsOfTimeDE.WOCHE)
                    .frequency(2)
                    .period(3, Timing.UnitsOfTime.D)
                    .timeOfDay(List.of("08:00:00", "18:00:00"))
                    .build())
            .build();

    val dosage2 =
        DosageDgMPBuilder.dosageTimeOfDayEntry(
            2,
            BmpDosiereinheit.MG,
            List.of("08:00:00", "18:00:00"),
            7,
            UnitsOfTimeDE.WOCHE,
            3,
            Timing.UnitsOfTime.D);
    assertEquals(
        dosage1.getTiming().getRepeat().getBoundsDuration().getValue(),
        dosage2.getTiming().getRepeat().getBoundsDuration().getValue());
    assertEquals(
        dosage1.getTiming().getRepeat().getBoundsDuration().getUnit(),
        dosage2.getTiming().getRepeat().getBoundsDuration().getUnit());
    assertEquals(
        dosage1.getTiming().getRepeat().getPeriodUnit(),
        dosage2.getTiming().getRepeat().getPeriodUnit());
    assertEquals(
        dosage1.getTiming().getRepeat().getPeriod(), dosage2.getTiming().getRepeat().getPeriod());
    assertEquals(
        dosage1.getTiming().getRepeat().getFrequency(),
        dosage2.getTiming().getRepeat().getFrequency());
    assertEquals(
        dosage1.getTiming().getRepeat().getTimeOfDay().getFirst().getHour(),
        dosage2.getTiming().getRepeat().getTimeOfDay().getFirst().getHour());
    assertEquals(
        dosage1.getTiming().getRepeat().getTimeOfDay().get(1).getHour(),
        dosage2.getTiming().getRepeat().getTimeOfDay().get(1).getHour());
    assertEquals(
        dosage1.getDoseAndRateFirstRep().getDoseQuantity().getValue(),
        dosage2.getDoseAndRateFirstRep().getDoseQuantity().getValue());
    assertEquals(
        dosage1.getDoseAndRateFirstRep().getDoseQuantity().getSystem(),
        dosage2.getDoseAndRateFirstRep().getDoseQuantity().getSystem());
  }

  @Test
  void shouldBuildWhenOnSameWay() {
    val dosage1 =
        DosageDgMPBuilder.dosageBuilder(2, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(7, UnitsOfTimeDE.WOCHE)
                    .period(3, Timing.UnitsOfTime.D)
                    .frequency(2)
                    .when(List.of(Timing.EventTiming.MORN, Timing.EventTiming.EVE))
                    .build())
            .build();

    val dosage2 =
        DosageDgMPBuilder.dosageWhenEntry(
            2,
            BmpDosiereinheit.MG,
            List.of(Timing.EventTiming.MORN, Timing.EventTiming.EVE),
            7,
            UnitsOfTimeDE.WOCHE,
            3,
            Timing.UnitsOfTime.D);
    assertEquals(
        dosage1.getTiming().getRepeat().getBoundsDuration().getValue(),
        dosage2.getTiming().getRepeat().getBoundsDuration().getValue());
    assertEquals(
        dosage1.getTiming().getRepeat().getBoundsDuration().getUnit(),
        dosage2.getTiming().getRepeat().getBoundsDuration().getUnit());
    assertEquals(
        dosage1.getTiming().getRepeat().getPeriodUnit(),
        dosage2.getTiming().getRepeat().getPeriodUnit());
    assertEquals(
        dosage1.getTiming().getRepeat().getPeriod(), dosage2.getTiming().getRepeat().getPeriod());
    assertEquals(
        dosage1.getTiming().getRepeat().getFrequency(),
        dosage2.getTiming().getRepeat().getFrequency());
    assertEquals(
        dosage1.getTiming().getRepeat().getWhen().getFirst().getValue(),
        dosage2.getTiming().getRepeat().getWhen().getFirst().getValue());
    assertEquals(
        dosage1.getTiming().getRepeat().getWhen().get(1).getValue(),
        dosage2.getTiming().getRepeat().getWhen().get(1).getValue());
    assertEquals(
        dosage1.getDoseAndRateFirstRep().getDoseQuantity().getValue(),
        dosage2.getDoseAndRateFirstRep().getDoseQuantity().getValue());
    assertEquals(
        dosage1.getDoseAndRateFirstRep().getDoseQuantity().getSystem(),
        dosage2.getDoseAndRateFirstRep().getDoseQuantity().getSystem());
  }

  @Test
  void shouldBuildDayOfWeekOnSameWay() {
    val dosage1 =
        DosageDgMPBuilder.dosageBuilder(2, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(7, UnitsOfTimeDE.WOCHE)
                    .period(3, Timing.UnitsOfTime.D)
                    .frequency(2)
                    .dayOfWeek(List.of(Timing.DayOfWeek.MON, Timing.DayOfWeek.FRI))
                    .build())
            .build();

    val dosage2 =
        DosageDgMPBuilder.dosageDayOfWeekEntry(
            2,
            BmpDosiereinheit.MG,
            List.of(Timing.DayOfWeek.MON, Timing.DayOfWeek.FRI),
            7,
            UnitsOfTimeDE.WOCHE,
            3,
            Timing.UnitsOfTime.D);

    assertEquals(dosage1, dosage2);
    assertEquals(
        dosage1.getTiming().getRepeat().getBoundsDuration().getValue(),
        dosage2.getTiming().getRepeat().getBoundsDuration().getValue());
    assertEquals(
        dosage1.getTiming().getRepeat().getBoundsDuration().getUnit(),
        dosage2.getTiming().getRepeat().getBoundsDuration().getUnit());
    assertEquals(
        dosage1.getTiming().getRepeat().getPeriodUnit(),
        dosage2.getTiming().getRepeat().getPeriodUnit());
    assertEquals(
        dosage1.getTiming().getRepeat().getPeriod(), dosage2.getTiming().getRepeat().getPeriod());
    assertEquals(
        dosage1.getTiming().getRepeat().getFrequency(),
        dosage2.getTiming().getRepeat().getFrequency());
    assertEquals(
        dosage1.getTiming().getRepeat().getDayOfWeek().getFirst().getValue(),
        dosage2.getTiming().getRepeat().getDayOfWeek().getFirst().getValue());
    assertEquals(
        dosage1.getTiming().getRepeat().getDayOfWeek().get(1).getValue(),
        dosage2.getTiming().getRepeat().getDayOfWeek().get(1).getValue());
    assertEquals(
        dosage1.getDoseAndRateFirstRep().getDoseQuantity().getValue(),
        dosage2.getDoseAndRateFirstRep().getDoseQuantity().getValue());
    assertEquals(
        dosage1.getDoseAndRateFirstRep().getDoseQuantity().getSystem(),
        dosage2.getDoseAndRateFirstRep().getDoseQuantity().getSystem());
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
                TimingBuilder.forRepeatComp().period(3, Timing.UnitsOfTime.D).frequency(2).build())
            .build();
    assertEquals(system, dosage.getDoseAndRate().get(0).getDoseQuantity().getSystem());
    assertEquals(unit, dosage.getDoseAndRate().get(0).getDoseQuantity().getUnit());
    assertEquals(code, dosage.getDoseAndRate().get(0).getDoseQuantity().getCode());
    assertEquals(value, dosage.getDoseAndRate().get(0).getDoseQuantity().getValue().intValue());
  }

  @Test
  void shouldBuildWithDayOfWeek() {
    val whenDosage =
        DosageDgMPBuilder.dosageWhenEntry(
            12,
            BmpDosiereinheit.STUECK,
            List.of(Timing.EventTiming.NIGHT, Timing.EventTiming.EVE),
            12.5,
            UnitsOfTimeDE.TAG,
            5,
            Timing.UnitsOfTime.WK,
            List.of(Timing.DayOfWeek.SUN, Timing.DayOfWeek.MON, Timing.DayOfWeek.FRI));

    assertEquals(6, whenDosage.getTiming().getRepeat().getFrequency());
  }

  @Test
  void shouldGetBuilder() {
    val builder = DosageDgMPBuilder.dosageBuilder();
    val dose = builder.build();
    assertNotNull(builder);
    assertFalse(dose.hasDoseAndRate());
  }
}
