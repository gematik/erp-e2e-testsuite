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

package de.gematik.test.erezept.fhir.date;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.GregorianCalendar;
import lombok.val;
import org.junit.jupiter.api.Test;

class WeekendRuleTest {

  @Test
  void shouldDetectWeekends() {
    val rule = WeekendRule.forWeekends();
    for (var i = 0; i < 2; i++) {
      val date = new GregorianCalendar(2023, Calendar.MARCH, 18 + i).getTime();
      assertTrue(rule.isOn(date));
    }
  }

  @Test
  void shouldFailOnWorkingDays() {
    val rule = WeekendRule.forWeekends();
    for (var i = 0; i < 5; i++) {
      val date = new GregorianCalendar(2023, Calendar.MARCH, 13 + i).getTime();
      assertFalse(rule.isOn(date));
    }
  }

  @Test
  void shouldNotFindWeekendBetweenWorkingWeek() {
    val rule = WeekendRule.forWeekends();
    val dc = DateConverter.getInstance();
    val start = dc.dateFromIso8601("2023-03-13");
    val end = dc.dateFromIso8601("2023-03-17");
    assertFalse(rule.isBetween(start, end));
  }

  @Test
  void shouldFindWeekendBetween() {
    val rule = WeekendRule.forWeekends();
    val dc = DateConverter.getInstance();
    val start = dc.dateFromIso8601("2023-03-13");
    val end = dc.dateFromIso8601("2023-03-31");
    assertTrue(rule.isBetween(start, end));
  }

  @Test
  void shouldGiveNextSaturday() {
    val rule = WeekendRule.forWeekends();
    val dc = DateConverter.getInstance();
    val start = dc.dateFromIso8601("2023-03-13");

    val next = rule.firstAfter(start);
    val nextLD = dc.dateToLocalDate(next);

    assertEquals(dc.dateFromIso8601("2023-03-18"), next);
    assertEquals(DayOfWeek.SATURDAY, nextLD.getDayOfWeek());
  }

  @Test
  void shouldGiveCurrentSundayAsNextAfter() {
    val rule = WeekendRule.forWeekends();
    val dc = DateConverter.getInstance();
    val start = dc.dateFromIso8601("2023-03-19");

    val next = rule.firstAfter(start);
    val nextLD = dc.dateToLocalDate(next);

    assertEquals(dc.dateFromIso8601("2023-03-19"), next);
    assertEquals(DayOfWeek.SUNDAY, nextLD.getDayOfWeek());
  }

  @Test
  void shouldGiveNextSaturdayBetween() {
    val rule = WeekendRule.forWeekends();
    val dc = DateConverter.getInstance();
    val start = dc.dateFromIso8601("2023-03-13");
    val end = dc.dateFromIso8601("2023-03-19");

    val next = rule.firstBetween(start, end);
    val nextLD = dc.dateToLocalDate(next);

    assertTrue(rule.isBetween(start, end));
    assertEquals(dc.dateFromIso8601("2023-03-18"), next);
    assertEquals(DayOfWeek.SATURDAY, nextLD.getDayOfWeek());
  }

  @Test
  void shouldGiveCurrentSundayBetween() {
    val rule = WeekendRule.forWeekends();
    val dc = DateConverter.getInstance();
    val start = dc.dateFromIso8601("2023-03-19");
    val end = dc.dateFromIso8601("2023-03-25");

    val next = rule.firstBetween(start, end);
    val nextLD = dc.dateToLocalDate(next);

    assertTrue(rule.isBetween(start, end));
    assertEquals(dc.dateFromIso8601("2023-03-19"), next);
    assertEquals(DayOfWeek.SUNDAY, nextLD.getDayOfWeek());
  }
}
