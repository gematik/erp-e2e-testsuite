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

package de.gematik.test.erezept.fhir.extensions.kbv;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import java.math.BigDecimal;
import java.util.Calendar;
import lombok.val;
import org.junit.jupiter.api.Test;

class MultiplePrescriptionExtensionTest {

  @Test
  void shouldCreateNonMultiple() {
    val mvo = MultiplePrescriptionExtension.asNonMultiple();
    val ext = mvo.asExtension();

    val kennzeichen = ext.getExtensionByUrl("Kennzeichen");
    val isMvo = (Boolean) kennzeichen.getValueAsPrimitive().getValue();
    assertFalse(isMvo);
  }

  @Test
  void shouldCreateIndefinitelyMultiple() {
    val mvo = MultiplePrescriptionExtension.asMultiple(1, 4).withoutEndDate();
    val ext = mvo.asExtension();

    val kennzeichen = ext.getExtensionByUrl("Kennzeichen");
    val isMvo = (Boolean) kennzeichen.getValueAsPrimitive().getValue();
    assertTrue(isMvo);

    val nummerierung = ext.getExtensionByUrl("Nummerierung");
    val ratio = nummerierung.getValue().castToRatio(nummerierung.getValue());
    assertEquals(new BigDecimal(1), ratio.getNumerator().getValue());
    assertEquals(new BigDecimal(4), ratio.getDenominator().getValue());

    val zeitraum = ext.getExtensionByUrl("Zeitraum");
    assertNotNull(zeitraum);
  }

  @Test
  void shouldBuildCorrectValuePeriod() {
    val mvo = MultiplePrescriptionExtension.asMultiple(1, 4).fromNow().validForDays(30);
    val ext = mvo.asExtension();

    val zeitraum = ext.getExtensionByUrl("Zeitraum");
    assertNotNull(zeitraum);
    val period = zeitraum.getValue().castToPeriod(zeitraum.getValue());
    assertNotNull(period.getStartElement().getValue());
    assertNotNull(period.getEndElement().getValue());
  }

  @Test
  void shouldBuildCorrectValuePeriodWithoutAutoStart() {
    val mvo = MultiplePrescriptionExtension.asMultiple(1, 4).validForDays(30, false);
    val ext = mvo.asExtension();

    val zeitraum = ext.getExtensionByUrl("Zeitraum");
    assertNotNull(zeitraum);
    val period = zeitraum.getValue().castToPeriod(zeitraum.getValue());
    assertNull(period.getStartElement().getValue());
    assertNotNull(period.getEndElement().getValue());
  }

  @Test
  void shouldBuildWithStartDaysDiff() {
    val mvo = MultiplePrescriptionExtension.asMultiple(1, 4).starting(10).withoutEndDate();
    val ext = mvo.asExtension();

    val zeitraum = ext.getExtensionByUrl("Zeitraum");
    assertNotNull(zeitraum);
    val period = zeitraum.getValue().castToPeriod(zeitraum.getValue());
    val startDate = period.getStartElement().getValue();
    assertNotNull(startDate);

    val cal = Calendar.getInstance();
    cal.add(Calendar.DATE, 10);
    val expected = cal.getTime();
    val diff = expected.getTime() - startDate.getTime();
    assertTrue(diff < 1000L);
  }

  @Test
  void shouldBuildWithValidThrough() {
    val mvo = MultiplePrescriptionExtension.asMultiple(1, 4).validThrough(10, 30);

    assertTrue(mvo.isMultiple());
    assertTrue(mvo.getStart().isPresent());
    assertTrue(mvo.getEnd().isPresent());

    val start = mvo.getStart().orElseThrow();
    val end = mvo.getEnd().orElseThrow();
    assertTrue(end.after(start));
  }

  @Test
  void shouldBuildPeriodWithoutEnd() {
    val mvo = MultiplePrescriptionExtension.asMultiple(1, 4).fromNow().withoutEndDate();
    val ext = mvo.asExtension();

    val zeitraum = ext.getExtensionByUrl("Zeitraum");
    assertNotNull(zeitraum);
    val period = zeitraum.getValue().castToPeriod(zeitraum.getValue());
    assertNotNull(period.getStartElement().getValue());
    assertNull(period.getEndElement().getValue());
  }

  @Test
  void shouldBuildPeriodWithoutEndDateAndAutoStart() {
    val mvo = MultiplePrescriptionExtension.asMultiple(1, 4).withoutEndDate();
    val ext = mvo.asExtension();

    val zeitraum = ext.getExtensionByUrl("Zeitraum");
    assertNotNull(zeitraum);
    val period = zeitraum.getValue().castToPeriod(zeitraum.getValue());
    assertNotNull(period.getStartElement().getValue());
    assertNull(period.getEndElement().getValue());
  }

  @Test
  void shouldBuildPeriodWithoutEndDateAndWithoutAutoStart() {
    val mvo = MultiplePrescriptionExtension.asMultiple(1, 4).withoutEndDate(false);
    val ext = mvo.asExtension();

    val zeitraum = ext.getExtensionByUrl("Zeitraum");
    assertNotNull(zeitraum);
    val period = zeitraum.getValue().castToPeriod(zeitraum.getValue());
    assertNull(period.getStartElement().getValue());
    assertNull(period.getEndElement().getValue());
  }

  @Test
  void shouldCreateWithId() {
    val mvo = MultiplePrescriptionExtension.asMultiple(1, 4)
            .withId("123").withoutEndDate(false);
    val ext = mvo.asExtension(KbvItaErpVersion.V1_1_0);
    
    val idExt = ext.getExtensionByUrl("ID");
    val mvoId = idExt.getValue().castToIdentifier(idExt.getValue()).getValue();
    assertTrue(mvoId.contains("123"));
  }

  @Test
  void shouldIgnoreIdOnOldProfiles() {
    val mvo = MultiplePrescriptionExtension.asMultiple(1, 4)
            .withId("123").withoutEndDate(false);
    val ext = mvo.asExtension(KbvItaErpVersion.V1_0_2);

    val idExt = ext.getExtensionByUrl("ID");
    assertNull(idExt);
  }
}
