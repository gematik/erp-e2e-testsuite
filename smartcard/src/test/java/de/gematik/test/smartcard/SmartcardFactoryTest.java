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

package de.gematik.test.smartcard;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.smartcard.exceptions.CardNotFoundException;
import de.gematik.test.smartcard.exceptions.SmartCardKeyNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.*;
import java.util.*;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SmartcardFactoryTest {

  private static SmartcardArchive sca;

  @BeforeAll
  static void setup() {
    sca = SmartcardFactory.getArchive();
  }

  @Test
  void getHbasByIccsn() {
    val hbaIccsn = "80276001081699900578";
    val hba = sca.getHbaByICCSN(hbaIccsn);
    assertEquals(hbaIccsn, hba.getIccsn(), "ICCSN of HBA does not match");
  }

  @Test
  void getEgkByKvnr() {
    val kvnr = "X110465770";
    val egk = sca.getEgkByKvnr(kvnr);
    assertEquals(kvnr, egk.getKvnr());
    assertEquals("80276883110000113323", egk.getIccsn());
  }

  @Test
  void shouldThrowOnUnknownKvnr() {
    assertThrows(CardNotFoundException.class, () -> sca.getEgkByKvnr("X123123123"));
  }

  @Test
  void shouldReturnOwnerInformation() {
    val hbaRsa = sca.getHbaCards().get(0);
    assertDoesNotThrow(() -> hbaRsa.getOwner().getOwnerName());
  }

  @Test
  void shouldThrowSmartCardKeyNotFoundException() {
    val hba = new Hba(List.of(), "00000");
    assertThrows(SmartCardKeyNotFoundException.class, hba::getAutCertificate);
  }

  @Test
  void shouldThrowOnInvalidHbaIccsn() {
    val hbaIccsn = "0000000";
    assertThrows(CardNotFoundException.class, () -> sca.getHbaByICCSN(hbaIccsn));
    assertThrows(CardNotFoundException.class, () -> sca.getHbaByICCSN(hbaIccsn));
  }

  @Test
  void getSmcBsByIccsn() {
    val smcbIccsn = "80276001011699910102";

    val smcbRsa = sca.getSmcbByICCSN(smcbIccsn);
    assertEquals(smcbIccsn, smcbRsa.getIccsn(), "ICCSN of SMC-B does not match");
  }

  @Test
  void shouldThrowOnInvalidSmcBIccsn() {
    val smcbIccsn = "0000000";
    assertThrows(CardNotFoundException.class, () -> sca.getSmcbByICCSN(smcbIccsn));
    assertThrows(CardNotFoundException.class, () -> sca.getSmcbByICCSN(smcbIccsn));
  }

  @Test
  void getEgksByIccsn() {
    val egkIccsn = "80276883110000113323";

    val egkRsa = sca.getEgkByICCSN(egkIccsn);
    assertEquals(egkIccsn, egkRsa.getIccsn(), "ICCSN of EGK does not match");

    val egkEcc = sca.getEgkByICCSN(egkIccsn);
    assertEquals(egkIccsn, egkEcc.getIccsn(), "ICCSN of EGK does not match");
  }

  @Test
  void shouldThrowOnInvalidEgkIccsn() {
    val egkIccsn = "0000000";
    assertThrows(CardNotFoundException.class, () -> sca.getEgkByICCSN(egkIccsn));
    assertThrows(CardNotFoundException.class, () -> sca.getEgkByICCSN(egkIccsn));
  }

  @Test
  void constructorShouldNotBeCallable() throws NoSuchMethodException {
    Constructor<SmartcardFactory> constructor = SmartcardFactory.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    assertThrows(InvocationTargetException.class, constructor::newInstance);
  }
}
