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

import java.time.LocalDate;
import lombok.val;
import org.hl7.fhir.r4.model.Period;

public class PeriodDateUtil {

  private static final DateConverter converter = DateConverter.getInstance();

  private PeriodDateUtil() {
    throw new AssertionError("util class");
  }

  public static boolean isStillValid(Period period) {
    if (period.getStart() == null) {
      return false;
    }

    val maxStartAge = LocalDate.now().minusDays(7);
    val minEndAge = LocalDate.now().plusDays(7);

    val start = converter.dateToLocalDate(period.getStart());
    if (period.getEnd() != null) {
      val end = converter.dateToLocalDate(period.getEnd());
      return start.isAfter(maxStartAge) && end.isAfter(minEndAge);
    } else {
      return start.isAfter(maxStartAge);
    }
  }

  public static Period updatePeriod(Period period) {
    if (period.getStart() == null) {
      period.setStart(converter.localDateToDate(LocalDate.now()));
      return period;
    }

    if (converter.dateToLocalDate(period.getStart()).isAfter(LocalDate.now())) {
      // no need to change anything, start date is still in the future
      return period;
    }

    val originalStart = converter.dateToLocalDate(period.getStart());
    val newStart = converter.localDateToDate(LocalDate.now());
    period.setStart(newStart);

    if (period.getEnd() != null) {
      val originalEnd = converter.dateToLocalDate(period.getEnd());
      val diff = java.time.Period.between(originalStart, originalEnd);

      val newEnd = (LocalDate) diff.addTo(converter.dateToLocalDate(newStart));
      period.setEnd(converter.localDateToDate(newEnd));
    }

    return period;
  }
}
