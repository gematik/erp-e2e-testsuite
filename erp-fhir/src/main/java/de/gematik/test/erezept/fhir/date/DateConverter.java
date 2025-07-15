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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import lombok.SneakyThrows;

public class DateConverter {

  private static DateConverter instance;

  private final ZoneId zoneId;

  public static DateConverter getInstance() {
    if (instance == null) {
      instance = new DateConverter();
    }
    return instance;
  }

  private DateConverter() {
    this.zoneId = ZoneId.systemDefault();
  }

  public Date truncate(Date date) {
    return this.localDateToDate(this.dateToLocalDate(date).atStartOfDay(zoneId).toLocalDate());
  }

  public LocalDate dateToLocalDate(Date date) {
    return date.toInstant().atZone(this.zoneId).truncatedTo(ChronoUnit.DAYS).toLocalDate();
  }

  public Date localDateToDate(LocalDate date) {
    return Date.from(date.atStartOfDay(zoneId).toInstant());
  }

  /**
   * Convert the given date as string with the ISO 8601 pattern (YYYY-MM-DD) to a Date-Object
   *
   * @param date as string formatted as ISO 8601
   * @return a Date-Object
   */
  public Date dateFromIso8601(String date) {
    return from(date, new SimpleDateFormat("yyyy-MM-dd"));
  }

  public LocalDate localDateFromIso(String date) {
    return from(date, DateTimeFormatter.ISO_DATE);
  }

  @SneakyThrows
  public Date from(String date, SimpleDateFormat formatter) {
    return formatter.parse(date);
  }

  public LocalDate from(String date, DateTimeFormatter formatter) {
    return LocalDate.parse(date, formatter);
  }
}
