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

import de.gematik.test.erezept.crypto.certificate.*;
import java.util.*;
import lombok.*;
import org.junit.jupiter.api.*;

class SmcBTest {

  private static SmartcardArchive archive;

  @BeforeAll
  static void setupArchive() {
    archive = SmartcardFactory.getArchive();
  }

  @Test
  void getOSigCertificate() {
    val smcb = archive.getSmcbCards().get(0);
    Assertions.assertNotNull(smcb.getOSigCertificate(Crypto.RSA_2048));
  }

  @Test
  void getEncCertificate() {
    val smcb = archive.getSmcbCards().get(0);
    Assertions.assertNotNull(smcb.getEncCertificate(Crypto.RSA_2048));
  }

  @Test
  void getAutOids() {
    val smcb = archive.getSmcbCards().get(0);
    Assertions.assertEquals(List.of(Oid.OID_SMC_B_AUT), smcb.getAutOids());
  }
}
