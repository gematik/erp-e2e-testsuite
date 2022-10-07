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

package de.gematik.test.smartcard.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import de.gematik.test.smartcard.Crypto;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.exceptions.CardNotFoundException;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SmartcardFactoryTest {

  private SmartcardArchive sca;

  @Before
  public void setup() {
    sca = SmartcardFactory.readArchive();
  }

  @After
  public void teardown() {
    sca.destroy();
  }

  @Test
  public void createDefaultArchive() {
    assertEquals(
        "Expected and actual number of all HBAs does not match", 7, sca.getHbaCards().size());

    assertEquals(
        "Expected and actual number of RSA HBAs does not match",
        4,
        sca.getHbaCards(Crypto.RSA_2048).size());
    assertEquals(
        "Expected and actual number of ECC HBAs does not match",
        3,
        sca.getHbaCards(Crypto.ECC_256).size());

    assertEquals(
        "Expected and actual number of all SMC-B does not match", 11, sca.getSmcbCards().size());
    assertEquals(
        "Expected and actual number of RSA SMC-B does not match",
        6,
        sca.getSmcbCards(Crypto.RSA_2048).size());
    assertEquals(
        "Expected and actual number of ECC SMC-B does not match",
        5,
        sca.getSmcbCards(Crypto.ECC_256).size());
  }

  @Test
  public void getHbasByIccsn() {
    val hbaIccsn = "80276001081699900578";

    val hbaRsa = sca.getHbaByICCSN(hbaIccsn, Crypto.RSA_2048);
    assertEquals("ICCSN of HBA does not match", hbaIccsn, hbaRsa.getIccsn());
    assertEquals("Algorithm of HBA does not match", Crypto.RSA_2048, hbaRsa.getAlgorithm());

    val hbaEcc = sca.getHbaByICCSN(hbaIccsn, Crypto.ECC_256);
    assertEquals("ICCSN of HBA does not match", hbaIccsn, hbaEcc.getIccsn());
    assertEquals("Algorithm of HBA does not match", Crypto.ECC_256, hbaEcc.getAlgorithm());
  }

  @Test
  public void shouldThrowOnInvalidHbaIccsn() {
    val hbaIccsn = "0000000";
    assertThrows(CardNotFoundException.class, () -> sca.getHbaByICCSN(hbaIccsn, Crypto.RSA_2048));
    assertThrows(CardNotFoundException.class, () -> sca.getHbaByICCSN(hbaIccsn, Crypto.ECC_256));
  }

  @Test
  public void getSmcBsByIccsn() {
    val smcbIccsn = "80276001011699910102";

    val smcbRsa = sca.getSmcbByICCSN(smcbIccsn, Crypto.RSA_2048);
    assertEquals("ICCSN of SMC-B does not match", smcbIccsn, smcbRsa.getIccsn());
    assertEquals("Algorithm of SMC-B does not match", Crypto.RSA_2048, smcbRsa.getAlgorithm());

    val smcbEcc = sca.getSmcbByICCSN(smcbIccsn, Crypto.ECC_256);
    assertEquals("ICCSN of SMC-B does not match", smcbIccsn, smcbEcc.getIccsn());
    assertEquals("Algorithm of SMC-B does not match", Crypto.ECC_256, smcbEcc.getAlgorithm());
  }

  @Test
  public void shouldThrowOnInvalidSmcBIccsn() {
    val smcbIccsn = "0000000";
    assertThrows(CardNotFoundException.class, () -> sca.getSmcbByICCSN(smcbIccsn, Crypto.RSA_2048));
    assertThrows(CardNotFoundException.class, () -> sca.getSmcbByICCSN(smcbIccsn, Crypto.ECC_256));
  }
}
