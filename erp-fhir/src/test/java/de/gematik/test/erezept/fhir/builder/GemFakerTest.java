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

package de.gematik.test.erezept.fhir.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.exceptions.FakerException;
import de.gematik.test.erezept.fhir.resources.erp.CommunicationType;
import de.gematik.test.erezept.fhir.values.DoctorProfession;
import de.gematik.test.erezept.fhir.valuesets.PayorType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.val;
import org.junit.jupiter.api.Test;

class GemFakerTest {

  @Test
  void fakerProfessionWorks() {
    val result = GemFaker.fakerProfession();
    assertFalse(result.isEmpty());
  }

  @Test
  void fakerDocProfessionAsStringWorks() {
    val result = GemFaker.fakerDoctorProfessionAsString();
    assertFalse(result.isEmpty());
  }

  @Test
  void fakerDocProfessionWorks() {
    val result = GemFaker.fakerDoctorProfession();
    assertFalse(result.getNaming().isEmpty());
  }

  @Test
  void fakerDoctorProfessionWorks() {
    DoctorProfession doctorProfession = DoctorProfession.VISZERALCHIRURGIE;
    assertEquals("Viszeral Chirurg:in", doctorProfession.getNaming());
  }

  @Test
  void fakerDoctorProfessionGetDescriptionWorks() {
    val descriptionOfDoctorProfession = DoctorProfession.ANGIOLOGIE.getDescription();
    assertFalse(descriptionOfDoctorProfession.isEmpty());
  }

  @Test
  void emptyDoctorProfessionDescriptionGivesAnswer() {
    val description = DoctorProfession.ALLGEMEINMEDIZIN.getDescription();
    assertFalse(description.isEmpty());
  }

  @Test
  void fakerFutureExpirationShouldBetweenOneAndFourWeeks() {
    var expirationDate = GemFaker.fakerFutureExpirationDate();
    Date minDate = new Date(new Date().getTime() + TimeUnit.HOURS.toMillis((long) 24 * 7));
    Date maxDate = new Date(new Date().getTime() + TimeUnit.HOURS.toMillis((long) 24 * 28));
    assertTrue(expirationDate.getTime() >= minDate.getTime());
    assertTrue(expirationDate.getTime() <= maxDate.getTime());
  }

  @Test
  void shouldThrowOnConstructorCall() throws NoSuchMethodException {
    Constructor<GemFaker> constructor = GemFaker.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    assertThrows(InvocationTargetException.class, constructor::newInstance);
  }

  @Test
  void testInsuranceNameMaxLength() {
    int loopCount = 10;
    for (int i = 0; i < loopCount; i++) {
      val tmpInsuranceName = GemFaker.insuranceName();
      assertTrue(tmpInsuranceName.length() <= 45);
    }
  }

  @Test
  void testInfoRequest() {
    val type = CommunicationType.INFO_REQ;
    val message = GemFaker.fakerCommunicationMessage(type);
    assertTrue(message.length() > 0);
  }

  @Test
  void testRepresentative() {
    val type = CommunicationType.REPRESENTATIVE;
    val message = GemFaker.fakerCommunicationMessage(type);
    assertTrue(message.length() > 0);
  }

  @Test
  void testDispenseRequest() {
    val type = CommunicationType.DISP_REQ;
    val message = GemFaker.fakerCommunicationMessage(type);
    assertTrue(message.length() > 0);
  }

  @Test
  void testReply() {
    val type = CommunicationType.REPLY;
    val message = GemFaker.fakerCommunicationMessage(type);
    assertTrue(message.length() > 0);
  }

  @Test
  void shouldPickRandomFromValueSet() {
    val e = GemFaker.fakerValueSet(PayorType.class);
    assertNotNull(e);
  }

  @Test
  void shouldPickRandomFromValueSetWithExclude() {
    val e = GemFaker.fakerValueSet(PayorType.class, PayorType.SKT);
    assertNotNull(e);
    assertEquals(PayorType.UK, e); // because we only have SKT and UK within the PayorType
  }

  @Test
  void shouldPickRandomFromValueSetWithExclude2() {
    val exclude = List.of(PayorType.SKT, PayorType.UK); // all possible values!
    assertThrows(FakerException.class, () -> GemFaker.fakerValueSet(PayorType.class, exclude));
  }

  @Test
  void testGenerateControlNoTrue() {
    // (4+6+5) * 4 + (5+4+6) * 9 = 195  >> mod 10 = 5
    assertEquals(5, GemFaker.generateControlNo("456456"));
    assertEquals(7, GemFaker.generateControlNo("754236"));
  }

  @Test
  void testGenerateContrNoMax() {
    assertEquals(9, GemFaker.generateControlNo("999999"));
  }

  @Test
  void testGenerateContrNoMin() {
    assertEquals(1, GemFaker.generateControlNo("1"));
  }

  @Test
  void shouldGenerateRandomSecret() {
    assertNotNull(GemFaker.fakerSecret());
  }

  @Test
  void shouldGenerateRandomMvoExtension() {
    val mvo = GemFaker.mvo();
    assertNotNull(mvo);
  }

  @Test
  void shouldGenerateRandomUnSetMvoExtension() {
    val mvo = GemFaker.mvo(false);
    assertNotNull(mvo);
    assertFalse(mvo.isMultiple());
  }

  @Test
  void shouldGenerateRandomSetMvoExtension() {
    val mvo = GemFaker.mvo(true);
    assertNotNull(mvo);
    assertTrue(mvo.isMultiple());
    assertTrue(mvo.getNumerator() > 0);
    assertTrue(mvo.getDenominator() > 0);
    assertTrue(mvo.getDenominator() <= 4);
    assertTrue(mvo.getDenominator() >= mvo.getNumerator());

    assertTrue(mvo.getStart().isPresent());
    assertNotNull(mvo.getEnd());
  }

  @Test
  void shouldGenerateVatRateInRange() {
    for (var i = 0; i < 10; i++) {
      val vat = GemFaker.vatRate();
      assertTrue(vat >= 10.0f);
      assertTrue(vat <= 30.0f);
    }
  }

  @Test
  void shouldThrowOnInvalidVatRanges() {
    assertThrows(BuilderException.class, () -> GemFaker.vatRate(-0.1f, 10f));
    assertThrows(BuilderException.class, () -> GemFaker.vatRate(10.0f, 100.1f));
    assertThrows(BuilderException.class, () -> GemFaker.vatRate(10.0f, 10f));
    assertThrows(BuilderException.class, () -> GemFaker.vatRate(15.0f, 10f));
  }

  @Test
  void shouldGenerateRandomCost() {
    for (var i = 0; i < 10; i++) {
      val cost = GemFaker.cost();
      assertTrue(cost > 0.0f);
    }
  }

  @Test
  void shouldGenerateRandomMinCost() {
    val min = 23.45f;
    for (var i = 0; i < 10; i++) {
      val cost = GemFaker.cost(min);
      assertTrue(cost >= min);
    }
  }

  @Test
  void shouldGenerateRandomRangedCost() {
    val min = 23.45f;
    val max = 324.12f;
    for (var i = 0; i < 10; i++) {
      val cost = GemFaker.cost(min, max);
      assertTrue(cost >= min);
      assertTrue(cost <= max);
    }
  }

  @Test
  void shouldThrowOnInvalidCostRanges() {
    assertThrows(BuilderException.class, () -> GemFaker.cost(-0.1f, 10f));
    assertThrows(BuilderException.class, () -> GemFaker.cost(9.1f, -10f));
    assertThrows(BuilderException.class, () -> GemFaker.cost(90.1f, 80f));
  }
}
