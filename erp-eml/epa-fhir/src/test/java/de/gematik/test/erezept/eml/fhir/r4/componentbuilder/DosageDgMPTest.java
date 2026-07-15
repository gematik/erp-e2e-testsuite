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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.DosageDgMPBuilder;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.TimingBuilder;
import de.gematik.test.erezept.eml.fhir.r4.dgmp.DosageDgMP;
import de.gematik.test.erezept.eml.fhir.valuesets.BmpDosiereinheit;
import java.util.Collections;
import lombok.val;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.Test;

class DosageDgMPTest {

  @Test
  void hashCodeShouldBeEqualForEqualObjects() {
    DosageDgMP d1 = new DosageDgMP();
    DosageDgMP d2 = new DosageDgMP();
    d1.setText("Test");
    d2.setText("Test");
    Timing.TimingRepeatComponent repeat = new Timing.TimingRepeatComponent();
    repeat.setCount(1);
    repeat.setFrequency(2);
    repeat.setPeriod(3.0);
    repeat.setPeriodUnit(Timing.UnitsOfTime.H);
    d1.setTiming(new Timing().setRepeat(repeat));
    d2.setTiming(new Timing().setRepeat(repeat.copy()));
    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void hashCodeShouldDifferForDifferentObjects() {
    DosageDgMP d1 = new DosageDgMP();
    DosageDgMP d2 = new DosageDgMP();
    d1.setText("Test1");
    d2.setText("Test2");
    Timing.TimingRepeatComponent repeat1 = new Timing.TimingRepeatComponent();
    repeat1.setCount(1);
    Timing.TimingRepeatComponent repeat2 = new Timing.TimingRepeatComponent();
    repeat2.setCount(2);
    d1.setTiming(new Timing().setRepeat(repeat1));
    d2.setTiming(new Timing().setRepeat(repeat2));
    assertNotEquals(d1, d2);
    assertNotEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void hashCodeShouldHandleNulls() {
    DosageDgMP d1 = new DosageDgMP();
    DosageDgMP d2 = new DosageDgMP();
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void hashCodeShouldConsiderDoseAndRate() {
    DosageDgMP d1 = new DosageDgMP();
    DosageDgMP d2 = new DosageDgMP();
    Dosage.DosageDoseAndRateComponent dose1 = new Dosage.DosageDoseAndRateComponent();
    dose1.setType(new org.hl7.fhir.r4.model.CodeableConcept().setText("A"));
    Dosage.DosageDoseAndRateComponent dose2 = new Dosage.DosageDoseAndRateComponent();
    dose2.setType(new org.hl7.fhir.r4.model.CodeableConcept().setText("B"));
    d1.setDoseAndRate(Collections.singletonList(dose1));
    d2.setDoseAndRate(Collections.singletonList(dose2));
    assertNotEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void shouldTestEqualsAsText() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG).text("1 Tablette morgens").build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG).text("1 Tablette morgens").build();
    assertTrue(d1.equals(d2)); // NOSONAR
  }

  @Test
  void shouldFailWhileUseEquals1() {
    val d1 = DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG).text("1 Tablette").build();

    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG).text("1 Tablette morgens").build();
    assertFalse(d1.equals(d2)); // NOSONAR
  }

  @Test
  void shouldFailWhileUseEquals2() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(2.5, BmpDosiereinheit.MG)
            .text("1 Tablette morgens")
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG).text("1 Tablette morgens").build();

    assertFalse(d1.equals(d2)); // NOSONAR
  }

  @Test
  void shouldFailWhileUseEquals3() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.DOSIERBRIEFCHEN)
            .text("1 Tablette morgens")
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG).text("1 Tablette morgens").build();
    assertFalse(d1.equals(d2)); // NOSONAR
  }

  @Test
  void shouldBuildWithTimingMedium() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    assertTrue(d1.equals(d2)); // NOSONAR
    assertTrue(d2.equals(d1)); // NOSONAR
  }

  @Test
  void shouldBuildWithBoundsDuration() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(TimingBuilder.forRepeatComp().boundsDuration(7, "Woche(n)", "wk").build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(TimingBuilder.forRepeatComp().boundsDuration(7, "Woche(n)", "wk").build())
            .build();
    assertTrue(d1.equals(d2)); // NOSONAR
    assertTrue(d2.equals(d1)); // NOSONAR
  }

  @Test
  void shouldDetectDfferentBoundsDurationsValue() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(7, "Woche(n)", "wk")
                    .period(3)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .when("EVE")
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(8, "Woche(n)", "wk")
                    .period(3)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .when("EVE")
                    .build())
            .build();
    compareBothSidesAsFalse(d1, d2);
  }

  @Test
  void shouldDetectDfferentBoundsDurationsUnit() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(7, "Woche", "wk")
                    .period(3)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .when("EVE")
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(7, "Woche(n)", "wk")
                    .period(3)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .when("EVE")
                    .build())
            .build();
    compareBothSidesAsFalse(d1, d2);
  }

  @Test
  void shouldDetectDfferentBoundsDurationsCode() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(7, "Woche", "wk")
                    .period(3)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .when("EVE")
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(7, "Woche", "d")
                    .period(3)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .when("EVE")
                    .build())
            .build();
    compareBothSidesAsFalse(d1, d2);
  }

  @Test
  void shouldBuildWithTimingSmall() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    assertTrue(d1.equals(d2)); // NOSONAR
    assertTrue(d2.equals(d1)); // NOSONAR
  }

  @Test
  void shouldDetectUnEqualMediumChangeDaRValue() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(2, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    compareBothSidesAsFalse(d1, d2);
  }

  @Test
  void shouldDetectUnEqualMediumChangeDaRUnitValue() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.APPLIKATORFUELLUNG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    compareBothSidesAsFalse(d1, d2);
  }

  @Test
  void shouldDetectUnEqualMediumChangeTimingPeriodUnitValue() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.WK)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    compareBothSidesAsFalse(d1, d2);
  }

  @Test
  void shouldDetectUnEqualMediumReduceWhen() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("10:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    compareBothSidesAsFalse(d1, d2);
  }

  @Test
  void shouldDetectUnEqualMediumChangeDayOfWeek() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.WED)
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    compareBothSidesAsFalse(d1, d2);
  }

  @Test
  void shouldDetectUnEqualMediumAddTimeOfDay() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .timeOfDay("12:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    compareBothSidesAsFalse(d1, d2);
  }

  @Test
  void shouldDetectUnEqualMediumAddDayOfWek() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.FRI)
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    compareBothSidesAsFalse(d1, d2);
  }

  @Test
  void shouldDetectUnEqualMediumAddDayOfWek2() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.FRI)
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .dayOfWeek(Timing.DayOfWeek.WED)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    compareBothSidesAsFalse(d1, d2);
  }

  @Test
  void shouldDetectUnEqualMediumAddDayOfWek3() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.FRI)
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .dayOfWeek(Timing.DayOfWeek.TUE)
                    .dayOfWeek(Timing.DayOfWeek.SAT)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    compareBothSidesAsFalse(d1, d2);
  }

  @Test
  void shouldDetectUnEqualMediumTimeOfDay2() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("17:00:00")
                    .timeOfDay("17:30:00")
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .when("MORN")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("18:00:00")
                    .when("EVE")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .build())
            .build();
    compareBothSidesAsFalse(d1, d2);
  }

  @Test
  void shouldPassDifferentOrders() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .timeOfDay("08:00:00")
                    .timeOfDay("18:00:00")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .when("MORN")
                    .when("EVE")
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .timeOfDay("08:00:00")
                    .timeOfDay("18:00:00")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .when("MORN")
                    .when("EVE")
                    .build())
            .build();
    assertTrue(d1.equals(d2)); // NOSONAR
    assertTrue(d2.equals(d1)); // NOSONAR
  }

  @Test
  void shouldPassDifferentOrdersInTimeOfDay() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .timeOfDay("18:00:00")
                    .timeOfDay("08:00:00")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .when("MORN")
                    .when("EVE")
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .timeOfDay("08:00:00")
                    .timeOfDay("18:00:00")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .when("MORN")
                    .when("EVE")
                    .build())
            .build();
    assertTrue(d1.equals(d2)); // NOSONAR
    assertTrue(d2.equals(d1)); // NOSONAR
  }

  @Test
  void shouldPassDifferentOrdersInDayOfWeek() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .timeOfDay("18:00:00")
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .when("MORN")
                    .when("EVE")
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .timeOfDay("08:00:00")
                    .timeOfDay("18:00:00")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .when("MORN")
                    .when("EVE")
                    .build())
            .build();
    assertTrue(d1.equals(d2)); // NOSONAR
    assertTrue(d2.equals(d1)); // NOSONAR
  }

  @Test
  void shouldPassDifferentOrdersInWhen() {
    val d1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .timeOfDay("08:00:00")
                    .timeOfDay("18:00:00")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .when("EVE")
                    .when("MORN")
                    .build())
            .build();
    val d2 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3)
                    .frequency(1)
                    .timeOfDay("08:00:00")
                    .timeOfDay("18:00:00")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .periodUnit(Timing.UnitsOfTime.D)
                    .when("MORN")
                    .when("EVE")
                    .build())
            .build();
    assertTrue(d1.equals(d2)); // NOSONAR
    assertTrue(d2.equals(d1)); // NOSONAR
  }

  @Test
  void shouldComputeFrom() {
    val dosage = new Dosage();
    dosage.setText("1 Tablette morgens");
    val newDosage = DosageDgMP.fromDosage(dosage);
    assertEquals("1 Tablette morgens", newDosage.getText());
  }

  private static void compareBothSidesAsFalse(DosageDgMP d1, DosageDgMP d2) {
    assertFalse(d1.equals(d2)); // NOSONAR
    assertFalse(d2.equals(d1)); // NOSONAR
  }
}
