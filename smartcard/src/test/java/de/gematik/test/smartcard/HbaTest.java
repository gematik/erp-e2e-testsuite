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

package de.gematik.test.smartcard;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.crypto.certificate.Oid;
import de.gematik.test.smartcard.exceptions.SmartCardKeyNotFoundException;
import java.io.IOException;
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
  void shouldReadQesFromSupplier() {
    val first =
        archive.getHbaCards().stream()
            .filter(it -> it.getKey(Oid.OID_HBA_QES, Algorithm.RSA_2048).isPresent())
            .findFirst()
            .orElseThrow();
    val supplier = first.getQesCertificate(Algorithm.RSA_2048).getInputStreamSupplier();
    try (val stream = supplier.get()) {
      assertNotNull(stream);
    } catch (IOException e) {
      fail();
    }
  }

  @Test
  void shouldEqualOnSame() {
    val first = archive.getHbaCards().get(0);
    val second = archive.getHbaCards().get(0);
    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void shouldNotEqualOnNull() {
    val iccsn = "80276001081699900578";
    val first = archive.getHbaByICCSN(iccsn);
    assertNotEquals(null, first); // NOSONAR
  }

  @Test
  void shouldNotEqualOnDifferentCards() {
    val first = archive.getHbaCards().get(0);
    val second = archive.getHbaCards().get(1);
    assertNotEquals(first, second);
  }

  @Test
  void getTelematikId() {
    val hba = archive.getHbaCards().get(0);
    assertNotNull(hba.getTelematikId());
  }

  @Test
  void getEncCertificate() {
    val hba = archive.getHbaCards().get(0);
    assertDoesNotThrow(() -> hba.getEncCertificate(Algorithm.RSA_2048));
    assertNotNull(hba.getEncCertificate(Algorithm.RSA_2048));
  }

  @Test
  void shouldThrowSmartCardKeyNotFoundException() {
    val hba = archive.getHbaCards().get(0);
    assertThrows(
        SmartCardKeyNotFoundException.class, () -> hba.getEncCertificate(Algorithm.RSA_PSS_2048));
  }
}
