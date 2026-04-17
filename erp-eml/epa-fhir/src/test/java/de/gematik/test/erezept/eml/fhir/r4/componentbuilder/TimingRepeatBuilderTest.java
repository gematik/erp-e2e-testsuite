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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.eml.fhir.r4.componentbuilder;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.TimingRepeatBuilder;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.UnitsOfTimeDE;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.TimeType;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.Test;

class TimingRepeatBuilderTest {

  @Test
  void shouldBuildTimingRepeatComponentCorrect() {
    val tRC =
        TimingRepeatBuilder.forRepeatComp()
            .frequency(3)
            .period(2)
            .periodUnit(Timing.UnitsOfTime.D)
            .timeOfDay("08:00:00")
            .dayOfWeek(Timing.DayOfWeek.MON)
            // Setter tests special usecase e.g. in real Testcase like using a space-separated
            // CorrectCall (e.g., "MORN NOON")
            .when(Timing.EventTiming.C.getDisplay() + " " + Timing.EventTiming.ACM.getDisplay())
            .boundsDuration(5, UnitsOfTimeDE.MONAT)
            .build();

    assertNotNull(tRC);
    assertEquals(3, tRC.getRepeat().getFrequency());
    assertEquals(2, tRC.getRepeat().getPeriod().intValue());
    assertEquals(Timing.UnitsOfTime.D, tRC.getRepeat().getPeriodUnit());
    assertEquals(1, tRC.getRepeat().getTimeOfDay().size());
    assertEquals("08:00:00", tRC.getRepeat().getTimeOfDay().get(0).getValue());
    assertEquals(1, tRC.getRepeat().getDayOfWeek().size());
    assertEquals(Timing.DayOfWeek.MON, tRC.getRepeat().getDayOfWeek().get(0).getValue());
    assertEquals(2, tRC.getRepeat().getWhen().size());
    assertEquals(Timing.EventTiming.C, tRC.getRepeat().getWhen().get(0).getValue());
    assertNotNull(tRC.getRepeat().getBoundsDuration());
    assertEquals(5, tRC.getRepeat().getBoundsDuration().getValue().intValue());
    assertEquals("Monat", tRC.getRepeat().getBoundsDuration().getUnit());
    assertEquals("mo", tRC.getRepeat().getBoundsDuration().getCode());
  }

  @Test
  void shouldBuildTimingRepeatComponentWithMultipleTimeOfDaysCorrect() {
    val tRC =
        TimingRepeatBuilder.forRepeatComp()
            .frequency(5)
            .period(1)
            .periodUnit(Timing.UnitsOfTime.fromCode("d"))
            .timeOfDay("08:00:00")
            .timeOfDay("10:00:00")
            .timeOfDay("12:00:00")
            .dayOfWeek("mon thu")
            .boundsDuration(5, UnitsOfTimeDE.fromCode("d").getSingular(), "d")
            .build();

    assertNotNull(tRC);
    assertEquals(2, tRC.getRepeat().getDayOfWeek().size());
    assertEquals(1, tRC.getRepeat().getPeriod().intValue());
    assertEquals(3, tRC.getRepeat().getTimeOfDay().size());
    assertNotEquals(5, tRC.getRepeat().getTimeOfDay().size());
    assertEquals(
        new TimeType("08:00:00").getValue(), tRC.getRepeat().getTimeOfDay().get(0).getValue());
  }

  @Test
  void shouldBuildWithWhenListCorrect() {
    List<Timing.EventTiming> eventTimings =
        List.of(Timing.EventTiming.AC, Timing.EventTiming.C, Timing.EventTiming.EVE);
    List<Timing.DayOfWeek> dayList =
        List.of(Timing.DayOfWeek.MON, Timing.DayOfWeek.TUE, Timing.DayOfWeek.THU);
    String timeOfDays = "08:00:00 10:00:00 12:00:00";

    val tRC =
        TimingRepeatBuilder.forRepeatComp()
            .frequency(5)
            .period(1)
            .when(Timing.EventTiming.MORN)
            .when(eventTimings)
            .periodUnit("d")
            .timeOfDay(timeOfDays)
            .dayOfWeek(dayList)
            .build();

    assertNotNull(tRC);
    assertEquals(5, tRC.getRepeat().getFrequency());
    assertEquals(1, tRC.getRepeat().getPeriod().intValue());
    assertEquals(Timing.UnitsOfTime.D, tRC.getRepeat().getPeriodUnit());
    assertEquals(3, tRC.getRepeat().getTimeOfDay().size());
    assertEquals("08:00:00", tRC.getRepeat().getTimeOfDay().get(0).getValue());
    assertEquals("10:00:00", tRC.getRepeat().getTimeOfDay().get(1).getValue());
    assertEquals("12:00:00", tRC.getRepeat().getTimeOfDay().get(2).getValue());
    assertEquals(4, tRC.getRepeat().getWhen().size());
    assertEquals(Timing.EventTiming.MORN, tRC.getRepeat().getWhen().get(0).getValue());
    assertEquals(Timing.EventTiming.AC, tRC.getRepeat().getWhen().get(1).getValue());
    assertEquals(Timing.EventTiming.C, tRC.getRepeat().getWhen().get(2).getValue());
    assertEquals(Timing.EventTiming.EVE, tRC.getRepeat().getWhen().get(3).getValue());
    assertEquals(3, tRC.getRepeat().getDayOfWeek().size());
  }

  @Test
  void setterShouldBeNullSave() {
    List<Timing.EventTiming> eventTimings = List.of();
    List<Timing.EventTiming> eventTimingsNull = null;
    List<String> listOfDays = List.of();
    List<String> listOfDaysNull = null;
    List<Timing.DayOfWeek> days = List.of();
    List<Timing.DayOfWeek> daysNull = null;

    val tRC =
        TimingRepeatBuilder.forRepeatComp()
            .frequency(5)
            .period(1)
            .when("")
            .when(eventTimingsNull)
            .when(eventTimings)
            .timeOfDay("")
            .timeOfDay(listOfDays)
            .timeOfDay(listOfDaysNull)
            .dayOfWeek("")
            .dayOfWeek(days)
            .dayOfWeek(daysNull);
    assertDoesNotThrow(tRC::build);
  }
}
