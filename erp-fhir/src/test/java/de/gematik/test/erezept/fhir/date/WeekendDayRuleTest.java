/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.date;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import lombok.val;
import org.junit.jupiter.api.Test;

class WeekendDayRuleTest {

  @Test
  void shouldGiveNextSaturday() {
    val rule = WeekendDayRule.forSaturday();
    val dc = DateConverter.getInstance();
    val start = dc.dateFromIso8601("2023-03-13");

    val next = rule.firstAfter(start);
    val nextLD = dc.dateToLocalDate(next);

    assertEquals(dc.dateFromIso8601("2023-03-18"), next);
    assertEquals(DayOfWeek.SATURDAY, nextLD.getDayOfWeek());
  }

  @Test
  void shouldGiveCurrentSaturday() {
    val rule = WeekendDayRule.forSaturday();
    val dc = DateConverter.getInstance();
    val start = dc.dateFromIso8601("2023-03-18");

    val next = rule.firstAfter(start);
    val nextLD = dc.dateToLocalDate(next);

    assertEquals(dc.dateFromIso8601("2023-03-18"), next);
    assertEquals(DayOfWeek.SATURDAY, nextLD.getDayOfWeek());
  }

  @Test
  void shouldGiveCurrentSunday() {
    val rule = WeekendDayRule.forSunday();
    val dc = DateConverter.getInstance();
    val start = dc.dateFromIso8601("2023-03-19");

    val next = rule.firstAfter(start);
    val nextLD = dc.dateToLocalDate(next);

    assertEquals(dc.dateFromIso8601("2023-03-19"), next);
    assertEquals(DayOfWeek.SUNDAY, nextLD.getDayOfWeek());
  }

  @Test
  void shouldGiveNextSunday() {
    val rule = WeekendDayRule.forSunday();
    val dc = DateConverter.getInstance();
    val start = dc.dateFromIso8601("2023-03-13");

    val next = rule.firstAfter(start);
    val nextLD = dc.dateToLocalDate(next);

    assertEquals(dc.dateFromIso8601("2023-03-19"), next);
    assertEquals(DayOfWeek.SUNDAY, nextLD.getDayOfWeek());
  }

  @Test
  void shouldGiveNextSaturdayBetween() {
    val rule = WeekendDayRule.forSaturday();
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
  void shouldNotGiveNextSaturdayBetweenMondayFriday() {
    val rule = WeekendDayRule.forSaturday();
    val dc = DateConverter.getInstance();
    val start = dc.dateFromIso8601("2023-03-13");
    val end = dc.dateFromIso8601("2023-03-16");

    val next = rule.firstBetween(start, end);
    assertNull(next);
    assertFalse(rule.isBetween(start, end));
  }

  @Test
  void shouldGiveNextSundayBetween() {
    val rule = WeekendDayRule.forSunday();
    val dc = DateConverter.getInstance();
    val start = dc.dateFromIso8601("2023-03-13");
    val end = dc.dateFromIso8601("2023-03-19");

    val next = rule.firstBetween(start, end);
    val nextLD = dc.dateToLocalDate(next);

    assertTrue(rule.isBetween(start, end));
    assertEquals(dc.dateFromIso8601("2023-03-19"), next);
    assertEquals(DayOfWeek.SUNDAY, nextLD.getDayOfWeek());
  }

  @Test
  void shouldNotGiveNextSundayBetweenMondayFriday() {
    val rule = WeekendDayRule.forSunday();
    val dc = DateConverter.getInstance();
    val start = dc.dateFromIso8601("2023-03-13");
    val end = dc.dateFromIso8601("2023-03-16");

    val next = rule.firstBetween(start, end);
    assertNull(next);
    assertFalse(rule.isBetween(start, end));
  }
}
