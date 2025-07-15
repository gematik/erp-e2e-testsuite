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
import java.util.Date;

public class WeekendRule implements DateRule {

  private final WeekendDayRule saturday;
  private final WeekendDayRule sunday;

  private WeekendRule() {
    this.saturday = WeekendDayRule.forSaturday();
    this.sunday = WeekendDayRule.forSunday();
  }

  public static WeekendRule forWeekends() {
    return new WeekendRule();
  }

  @Override
  public Date firstAfter(Date start) {
    if (this.sunday.isOn(start)) {
      return this.sunday.firstAfter(start);
    } else {
      return this.saturday.firstAfter(start);
    }
  }

  @Override
  public Date firstBetween(Date start, Date end) {
    if (this.sunday.isOn(start)) {
      return this.sunday.firstBetween(start, end);
    } else {
      return this.saturday.firstBetween(start, end);
    }
  }

  @Override
  public boolean isOn(Date date) {
    return this.saturday.isOn(date) || this.sunday.isOn(date);
  }

  @Override
  public boolean isBetween(Date start, Date end) {
    return this.saturday.isBetween(start, end) || this.sunday.isBetween(start, end);
  }
}
