/*
 * Copyright 2023 gematik GmbH
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.ParseException;
import java.time.Month;
import java.util.Calendar;
import lombok.val;
import org.junit.jupiter.api.Test;

class DateConverterTest {

  @Test
  void shouldConvertToDateFromISO8601String() {
    val isoDate = "2023-03-13";
    val date = DateConverter.getInstance().dateFromIso8601(isoDate);
    val calendar = Calendar.getInstance();
    calendar.setTime(date);

    assertEquals(2023, calendar.get(Calendar.YEAR));
    assertEquals(2, calendar.get(Calendar.MONTH));
    assertEquals(13, calendar.get(Calendar.DAY_OF_MONTH));
  }

  @Test
  void shouldConvertDateToLocalDate() {
    val isoDate = "2023-03-27";
    val date = DateConverter.getInstance().dateFromIso8601(isoDate);
    val localDate = DateConverter.getInstance().dateToLocalDate(date);

    assertEquals(27, localDate.getDayOfMonth());
    assertEquals(Month.MARCH, localDate.getMonth());
  }

  @Test
  void shouldConvertFromLocalDateToDate() {
    val isoDate = "2023-03-27";
    val localDate = DateConverter.getInstance().localDateFromIso(isoDate);
    val date = DateConverter.getInstance().localDateToDate(localDate);

    val calendar = Calendar.getInstance();
    calendar.setTime(date);
    assertEquals(27, calendar.get(Calendar.DAY_OF_MONTH));
    assertEquals(2, calendar.get(Calendar.MONTH));
  }

  @Test
  void shouldConvertToLocalDateFromISO8601String() {
    val isoDate = "2023-03-13";
    val date = DateConverter.getInstance().localDateFromIso(isoDate);

    assertEquals(2023, date.getYear());
    assertEquals(Month.MARCH, date.getMonth());
    assertEquals(13, date.getDayOfMonth());
  }

  @Test
  void shouldSneakyThrowOnInvalidFormat() {
    val dc = DateConverter.getInstance();
    assertThrows(ParseException.class, () -> dc.dateFromIso8601("hello world"));
  }
}
