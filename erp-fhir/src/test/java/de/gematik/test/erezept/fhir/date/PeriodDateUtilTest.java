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

import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import lombok.val;
import org.hl7.fhir.r4.model.Period;
import org.junit.jupiter.api.Test;

class PeriodDateUtilTest {

  @Test
  void shouldNotCallConstructor() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(PeriodDateUtil.class));
  }

  @Test
  void shouldDetectValidPeriod01() {
    val p = new Period();
    val yesterday =
        Date.from(LocalDate.now().minusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
    val tomorrow =
        Date.from(LocalDate.now().plusDays(100).atStartOfDay().toInstant(ZoneOffset.UTC));
    p.getStartElement().setValue(yesterday);
    p.getEndElement().setValue(tomorrow);
    assertTrue(PeriodDateUtil.isStillValid(p));
  }

  @Test
  void shouldDetectValidPeriod02() {
    val p = new Period();
    val yesterday =
        Date.from(LocalDate.now().minusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
    p.getStartElement().setValue(yesterday);
    assertTrue(PeriodDateUtil.isStillValid(p));
  }

  @Test
  void shouldDetectValidPeriod03() {
    val p = new Period();
    assertFalse(PeriodDateUtil.isStillValid(p));
  }

  @Test
  void shouldUpdatePeriod() {
    val p = new Period();
    val yesterday = Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC));
    val tomorrow = Date.from(LocalDate.of(1970, 1, 3).atStartOfDay().toInstant(ZoneOffset.UTC));
    p.setStart(yesterday);
    p.setEnd(tomorrow);

    val p2 = PeriodDateUtil.updatePeriod(p);

    val start = Calendar.getInstance();
    start.setTime(p2.getStart());
    val startDay = start.get(Calendar.DAY_OF_YEAR);

    val end = Calendar.getInstance();
    end.setTime(p2.getEnd());
    val endDay = end.get(Calendar.DAY_OF_YEAR);

    // this assertion will fail on 30/31 of december!
    assertEquals(2, endDay - startDay);
  }

  @Test
  void shouldUpdatePeriodWithoutEnd() {
    val edc = new DateCalculator();
    val p = new Period();
    val yesterday = Date.from(LocalDate.of(1970, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC));
    p.setStart(yesterday);

    val p2 = PeriodDateUtil.updatePeriod(p);

    val start = p2.getStart();
    assertTrue(edc.isToday(start));
    assertNull(p2.getEnd());
  }

  @Test
  void shouldNotUpdateFuturePeriods() {
    val edc = new DateCalculator();
    val p = new Period();
    val base = LocalDate.now().plusDays(2);
    val tomorrow = Date.from(base.atStartOfDay().toInstant(ZoneOffset.UTC));
    val tomorrow2 = Date.from(base.plusDays(2).atStartOfDay().toInstant(ZoneOffset.UTC));
    p.setStart(tomorrow);
    p.setEnd(tomorrow2);

    val p2 = PeriodDateUtil.updatePeriod(p);
    // start must not be changed, thus must not be today
    assertTrue(edc.isAfterToday(p2.getStart()));
    val nStart = p2.getStart().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    assertTrue(base.isEqual(nStart));

    // dates remain unchanged!
    assertEquals(tomorrow, p2.getStart());
    assertEquals(tomorrow2, p2.getEnd());
  }

  @Test
  void shouldUpdateNowForEmptyPeriods() {
    val edc = new DateCalculator();
    val p = new Period();
    val p2 = PeriodDateUtil.updatePeriod(p);
    assertNotNull(p2.getStart());
    assertTrue(edc.isToday(p2.getStart()));
    assertNull(p2.getEnd());
  }
}
