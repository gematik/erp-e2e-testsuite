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

package de.gematik.test.erezept.fhir.date;

import com.ibm.icu.util.DateRule;
import com.ibm.icu.util.EasterHoliday;
import com.ibm.icu.util.SimpleHoliday;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import lombok.val;

public class DateCalculator {

  private static final List<DateRule> GERMAN_NATIONAL_HOLIDAYS =
      List.of(
          WeekendRule.forWeekends(),
          SimpleHoliday.NEW_YEARS_DAY,
          SimpleHoliday.EPIPHANY,
          EasterHoliday.GOOD_FRIDAY,
          EasterHoliday.EASTER_MONDAY,
          SimpleHoliday.MAY_DAY,
          EasterHoliday.ASCENSION,
          EasterHoliday.WHIT_MONDAY,
          new SimpleHoliday(Calendar.OCTOBER, 3, "Day of german unity"),
          SimpleHoliday.CHRISTMAS_EVE,
          SimpleHoliday.CHRISTMAS,
          SimpleHoliday.BOXING_DAY,
          SimpleHoliday.NEW_YEARS_EVE);

  private final DateConverter converter;

  public DateCalculator() {
    this.converter = DateConverter.getInstance();
  }

  public LocalDate getDateAfterCalendarDays(LocalDate base, int calendarDays) {
    return base.plusDays(calendarDays);
  }

  public Date getDateAfterCalendarDays(Date base, int calendarDays) {
    val c = Calendar.getInstance();
    c.setTime(base);
    c.add(Calendar.DAY_OF_YEAR, calendarDays);
    return c.getTime();
  }

  public LocalDate getDateAfterWorkingDays(LocalDate base, int workingDays) {
    val target = getDateAfterWorkingDays(this.converter.localDateToDate(base), workingDays);
    return this.converter.dateToLocalDate(target);
  }

  public Date getDateAfterWorkingDays(Date base, int workingDays) {
    val c = Calendar.getInstance();
    c.setTime(base);
    while (workingDays > 0) {
      c.add(Calendar.DAY_OF_YEAR, 1);
      if (!isOnHoliday(c.getTime())) {
        workingDays--;
      }
    }
    return c.getTime();
  }

  public Date getDateAfterMonths(Date base, int months) {
    val c = Calendar.getInstance();
    c.setTime(base);
    c.add(Calendar.MONTH, months);
    return this.converter.truncate(c.getTime());
  }

  public boolean isOnHoliday(Date date) {
    for (var h : GERMAN_NATIONAL_HOLIDAYS) {
      if (h.isOn(date)) {
        return true;
      }
    }
    return false;
  }

  /**
   * This method compares dates independent of the actual time
   *
   * @param expectation of the date
   * @param actual date
   * @return true if dates are equal
   */
  public boolean equalDates(Date expectation, Date actual) {
    return converter.truncate(expectation).equals(converter.truncate(actual));
  }

  public boolean isToday(@Nullable Date date) {
    if (date == null) return false;
    return isToday(converter.dateToLocalDate(date));
  }

  public boolean isToday(@Nullable LocalDate date) {
    if (date == null) return false;
    return date.isEqual(LocalDate.now());
  }

  public boolean isBeforeToday(@Nullable Date date) {
    if (date == null) return false;
    return isBeforeToday(converter.dateToLocalDate(date));
  }

  public boolean isBeforeToday(@Nullable LocalDate date) {
    if (date == null) return false;
    return date.isBefore(LocalDate.now());
  }

  public boolean isAfterToday(@Nullable Date date) {
    if (date == null) return false;
    return isAfterToday(converter.dateToLocalDate(date));
  }

  public boolean isAfterToday(@Nullable LocalDate date) {
    if (date == null) return false;
    return date.isAfter(LocalDate.now());
  }
}
