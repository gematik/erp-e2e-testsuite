/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.fhir.builder;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import lombok.val;
import org.junit.jupiter.api.Test;

class GemFakerTest {

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
    ErxCommunication.CommunicationType type = ErxCommunication.CommunicationType.INFO_REQ;
    val message = GemFaker.fakerCommunicationMessage(type);
    assertTrue(message.length() > 0);
  }

  @Test
  void testRepresentative() {
    ErxCommunication.CommunicationType type = ErxCommunication.CommunicationType.REPRESENTATIVE;
    val message = GemFaker.fakerCommunicationMessage(type);
    assertTrue(message.length() > 0);
  }

  @Test
  void testDispenseRequest() {
    ErxCommunication.CommunicationType type = ErxCommunication.CommunicationType.DISP_REQ;
    val message = GemFaker.fakerCommunicationMessage(type);
    assertTrue(message.length() > 0);
  }

  @Test
  void testReply() {
    ErxCommunication.CommunicationType type = ErxCommunication.CommunicationType.REPLY;
    val message = GemFaker.fakerCommunicationMessage(type);
    assertTrue(message.length() > 0);
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
}
