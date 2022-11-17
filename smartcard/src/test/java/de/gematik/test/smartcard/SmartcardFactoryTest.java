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

package de.gematik.test.smartcard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.smartcard.exceptions.CardNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
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
  void createDefaultArchive() {
    assertEquals(
        7, sca.getHbaCards().size(), "Expected and actual number of all HBAs does not match");
    assertEquals(
        4,
        sca.getHbaCards(Crypto.RSA_2048).size(),
        "Expected and actual number of RSA HBAs does not match");
    assertEquals(
        3,
        sca.getHbaCards(Crypto.ECC_256).size(),
        "Expected and actual number of ECC HBAs does not match");

    assertEquals(
        13, sca.getSmcbCards().size(), "Expected and actual number of all SMC-B does not match");
    assertEquals(
        7,
        sca.getSmcbCards(Crypto.RSA_2048).size(),
        "Expected and actual number of RSA SMC-B does not match");
    assertEquals(
        6,
        sca.getSmcbCards(Crypto.ECC_256).size(),
        "Expected and actual number of ECC SMC-B does not match");

    assertEquals(
        11, sca.getEgkCards().size(), "Expected and actual number of all EGK does not match");
    assertEquals(
        6,
        sca.getEgkCards(Crypto.RSA_2048).size(),
        "Expected and actual number of RSA EGK does not match");
    assertEquals(
        5,
        sca.getEgkCards(Crypto.ECC_256).size(),
        "Expected and actual number of ECC EGK does not match");
  }

  @Test
  void getHbasByIccsn() {
    val hbaIccsn = "80276001081699900578";

    val hbaRsa = sca.getHbaByICCSN(hbaIccsn, Crypto.RSA_2048);
    assertEquals(hbaIccsn, hbaRsa.getIccsn(), "ICCSN of HBA does not match");
    assertEquals(Crypto.RSA_2048, hbaRsa.getAlgorithm(), "Algorithm of HBA does not match");

    val hbaEcc = sca.getHbaByICCSN(hbaIccsn, Crypto.ECC_256);
    assertEquals(hbaIccsn, hbaEcc.getIccsn(), "ICCSN of HBA does not match");
    assertEquals(Crypto.ECC_256, hbaEcc.getAlgorithm(), "Algorithm of HBA does not match");
  }

  @Test
  void shouldThrowOnInvalidHbaIccsn() {
    val hbaIccsn = "0000000";
    assertThrows(CardNotFoundException.class, () -> sca.getHbaByICCSN(hbaIccsn, Crypto.RSA_2048));
    assertThrows(CardNotFoundException.class, () -> sca.getHbaByICCSN(hbaIccsn, Crypto.ECC_256));
  }

  @Test
  void getSmcBsByIccsn() {
    val smcbIccsn = "80276001011699910102";

    val smcbRsa = sca.getSmcbByICCSN(smcbIccsn, Crypto.RSA_2048);
    assertEquals(smcbIccsn, smcbRsa.getIccsn(), "ICCSN of SMC-B does not match");
    assertEquals(Crypto.RSA_2048, smcbRsa.getAlgorithm(), "Algorithm of SMC-B does not match");

    val smcbEcc = sca.getSmcbByICCSN(smcbIccsn, Crypto.ECC_256);
    assertEquals(smcbIccsn, smcbEcc.getIccsn(), "ICCSN of SMC-B does not match");
    assertEquals(Crypto.ECC_256, smcbEcc.getAlgorithm(), "Algorithm of SMC-B does not match");
  }

  @Test
  void shouldThrowOnInvalidSmcBIccsn() {
    val smcbIccsn = "0000000";
    assertThrows(CardNotFoundException.class, () -> sca.getSmcbByICCSN(smcbIccsn, Crypto.RSA_2048));
    assertThrows(CardNotFoundException.class, () -> sca.getSmcbByICCSN(smcbIccsn, Crypto.ECC_256));
  }

  @Test
  void getEgksByIccsn() {
    val egkIccsn = "80276883110000113323";

    val egkRsa = sca.getEgkByICCSN(egkIccsn, Crypto.RSA_2048);
    assertEquals(egkIccsn, egkRsa.getIccsn(), "ICCSN of EGK does not match");
    assertEquals(Crypto.RSA_2048, egkRsa.getAlgorithm(), "Algorithm of EGK does not match");

    val egkEcc = sca.getEgkByICCSN(egkIccsn, Crypto.ECC_256);
    assertEquals(egkIccsn, egkEcc.getIccsn(), "ICCSN of EGK does not match");
    assertEquals(Crypto.ECC_256, egkEcc.getAlgorithm(), "Algorithm of EGK does not match");
  }

  @Test
  void shouldThrowOnInvalidEgkIccsn() {
    val egkIccsn = "0000000";
    assertThrows(CardNotFoundException.class, () -> sca.getEgkByICCSN(egkIccsn, Crypto.RSA_2048));
    assertThrows(CardNotFoundException.class, () -> sca.getEgkByICCSN(egkIccsn, Crypto.ECC_256));
  }

  @Test
  void constructorShouldNotBeCallable() throws NoSuchMethodException {
    Constructor<SmartcardFactory> constructor = SmartcardFactory.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    assertThrows(InvocationTargetException.class, constructor::newInstance);
  }
}
