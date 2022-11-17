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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class HbaTest {

  private static SmartcardArchive archive;

  @BeforeAll
  static void setupArchive() {
    archive = SmartcardFactory.getArchive();
  }

  @Test
  void shouldEqualOnSame() {
    val first = archive.getHbaCards().get(0);
    val second = archive.getHbaCards().get(0);
    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void shouldEqualOnSameData() {
    val first = archive.getHbaCards().get(0);
    val second = mock(Hba.class);
    when(second.getQesCertificateChain()).thenReturn(first.getQesCertificateChain());
    when(second.getQesCertificate()).thenReturn(first.getQesCertificate());
    when(second.getQesP12Password()).thenReturn(first.getQesP12Password());
    when(second.getQesPrivateKey()).thenReturn(first.getQesPrivateKey());
    when(second.getSerialnumber()).thenReturn(first.getSerialnumber());
    when(second.getAlgorithm()).thenReturn(first.getAlgorithm());
    when(second.getType()).thenReturn(first.getType());
    when(second.getIccsn()).thenReturn(first.getIccsn());

    assertNotSame(first, second);
    assertEquals(first, second);
  }

  @Test
  void shouldNotEqualOnDifferentCrypto() {
    val iccsn = "80276001081699900578";
    val first = archive.getHbaByICCSN(iccsn, Crypto.RSA_2048);
    val second = archive.getHbaByICCSN(iccsn, Crypto.ECC_256);
    assertNotEquals(first, second);
  }

  @Test
  void shouldNotEqualOnDifferentCards() {
    val first = archive.getHbaCards().get(0);
    val second = archive.getHbaCards().get(1);
    assertNotEquals(first, second);
  }
}
