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

package de.gematik.test.erezept.fhir.date;

import static org.junit.jupiter.api.Assertions.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import lombok.val;
import org.junit.jupiter.api.Test;

class DateCalculatorTest {

  @Test
  void nowShouldBeNotBeforeToday() {
    val edc = new DateCalculator();
    assertFalse(edc.isBeforeToday(new Date()));
  }

  @Test
  void nowShouldBeNotAfterToday() {
    val edc = new DateCalculator();
    assertFalse(edc.isAfterToday(new Date()));
  }

  @Test
  void shouldHandleNullableDates() {
    val edc = new DateCalculator();
    Date d = null;
    LocalDate ld = null;
    assertFalse(edc.isToday(d));
    assertFalse(edc.isToday(ld));
    assertFalse(edc.isBeforeToday(d));
    assertFalse(edc.isBeforeToday(ld));
    assertFalse(edc.isAfterToday(d));
    assertFalse(edc.isAfterToday(ld));
  }

  @Test
  void shouldEqualWithoutTime() {
    val dc = DateConverter.getInstance();
    val edc = new DateCalculator();
    val now = new Date();

    val expectation = dc.dateFromIso8601(new SimpleDateFormat("yyyy-MM-dd").format(now));
    assertTrue(edc.equalDates(expectation, now));
  }

  @Test
  void shouldCalculateCalendarDaysAfter() {
    val dc = DateConverter.getInstance();
    val edc = new DateCalculator();
    val startDate = dc.dateFromIso8601("2023-03-13");
    val expiryDate = edc.getDateAfterCalendarDays(startDate, 20);

    assertEquals(dc.dateFromIso8601("2023-04-02"), expiryDate);

    val expiryLD = dc.dateToLocalDate(expiryDate);
    assertEquals(2, expiryLD.getDayOfMonth());
    assertEquals(Month.APRIL, expiryLD.getMonth());
    assertEquals(2023, expiryLD.getYear());
  }

  @Test
  void shouldCalculateLocalDateCalendarDaysAfter() {
    val dc = DateConverter.getInstance();
    val edc = new DateCalculator();
    val startDate = dc.localDateFromIso("2023-03-13");
    val expiryDate = edc.getDateAfterCalendarDays(startDate, 20);

    assertEquals(dc.localDateFromIso("2023-04-02"), expiryDate);

    assertEquals(2, expiryDate.getDayOfMonth());
    assertEquals(Month.APRIL, expiryDate.getMonth());
    assertEquals(2023, expiryDate.getYear());
  }

  @Test
  void shouldCalculateWorkingDaysOverWeekend() {
    val dc = DateConverter.getInstance();
    val edc = new DateCalculator();
    val startDate = dc.dateFromIso8601("2023-03-13");
    val expiryDate = edc.getDateAfterWorkingDays(startDate, 7);
    val expiryLocalDate = dc.dateToLocalDate(expiryDate);

    // 13 + 7 working days + weekend => 22
    assertEquals(22, expiryLocalDate.getDayOfMonth());
    assertEquals(Month.MARCH, expiryLocalDate.getMonth());
  }

  @Test
  void shouldCalculateWorkingDaysOverEaster() {
    val dc = DateConverter.getInstance();
    val edc = new DateCalculator();
    val startDate = dc.dateFromIso8601("2023-04-06");
    val expiryDate = edc.getDateAfterWorkingDays(startDate, 5);
    val expiryLocalDate = dc.dateToLocalDate(expiryDate);

    // 06 + 5 working days + holidays + weekend => 17
    assertEquals(17, expiryLocalDate.getDayOfMonth());
    assertEquals(Month.APRIL, expiryLocalDate.getMonth());
  }

  @Test
  void shouldCalculateWorkingDaysOverChristmas() {
    val dc = DateConverter.getInstance();
    val edc = new DateCalculator();
    val startDate = dc.localDateFromIso("2023-12-20");
    val expiryDate = edc.getDateAfterWorkingDays(startDate, 6);

    assertEquals(2, expiryDate.getDayOfMonth());
    assertEquals(Month.JANUARY, expiryDate.getMonth());
    assertEquals(2024, expiryDate.getYear());
  }

  @Test
  void shouldCalculateWorkingDaysForWholeYear() {
    val dc = DateConverter.getInstance();
    val edc = new DateCalculator();
    val startDate = dc.dateFromIso8601("2023-01-01");
    val expectedDate = dc.dateFromIso8601("2024-01-02");

    val expiryDate = edc.getDateAfterWorkingDays(startDate, 252);
    assertEquals(expectedDate, expiryDate);
  }

  @Test
  void shouldIncrementMonths() {
    val dc = DateConverter.getInstance();
    val edc = new DateCalculator();
    val startDate = dc.dateFromIso8601("2022-08-31");

    val expiryDate = edc.getDateAfterMonths(startDate, 3);
    assertEquals(dc.dateFromIso8601("2022-11-30"), expiryDate);

    val expiryLD = dc.dateToLocalDate(expiryDate);
    assertEquals(30, expiryLD.getDayOfMonth());
    assertEquals(Month.NOVEMBER, expiryLD.getMonth());
    assertEquals(2022, expiryLD.getYear());
  }

  @Test
  void shouldIncrementMonthsOverNewYear() {
    val dc = DateConverter.getInstance();
    val edc = new DateCalculator();
    val startDate = dc.dateFromIso8601("2022-10-31");

    val expiryDate = edc.getDateAfterMonths(startDate, 2);
    assertEquals(dc.dateFromIso8601("2022-12-31"), expiryDate);

    val expiryLD = dc.dateToLocalDate(expiryDate);
    assertEquals(31, expiryLD.getDayOfMonth());
    assertEquals(Month.DECEMBER, expiryLD.getMonth());
    assertEquals(2022, expiryLD.getYear());
  }
}
