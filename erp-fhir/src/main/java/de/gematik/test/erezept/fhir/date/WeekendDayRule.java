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

import com.ibm.icu.util.DateRule;
import java.util.Calendar;
import java.util.Date;
import lombok.val;

public class WeekendDayRule implements DateRule {

  private final int weekendDay;

  private WeekendDayRule(int weekendDay) {
    this.weekendDay = weekendDay;
  }

  public static WeekendDayRule forSunday() {
    return new WeekendDayRule(Calendar.SUNDAY);
  }

  public static WeekendDayRule forSaturday() {
    return new WeekendDayRule(Calendar.SATURDAY);
  }

  @Override
  public Date firstAfter(Date start) {
    val c = Calendar.getInstance();
    c.setTime(start);
    val dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

    if (dayOfWeek == this.weekendDay) {
      return start;
    }

    if (this.weekendDay == Calendar.SUNDAY) {
      // sunday == 1 but we need it to be 8 for this algorithm to work properly
      val diffDaysUntilSaturday = 8 - dayOfWeek;
      c.add(Calendar.DAY_OF_WEEK, diffDaysUntilSaturday);
    } else {
      val diffDaysUntilSaturday = this.weekendDay - dayOfWeek;
      c.add(Calendar.DAY_OF_WEEK, diffDaysUntilSaturday);
    }

    return c.getTime();
  }

  @Override
  public Date firstBetween(Date start, Date end) {
    val firstAfter = firstAfter(start);
    if (firstAfter.after(end)) {
      return null;
    } else {
      return firstAfter;
    }
  }

  @Override
  public boolean isOn(Date date) {
    val c = Calendar.getInstance();
    c.setTime(date);
    val dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

    return dayOfWeek == this.weekendDay;
  }

  @Override
  public boolean isBetween(Date start, Date end) {
    return firstBetween(start, end) != null;
  }
}
