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

package de.gematik.test.erezept

import de.gematik.test.erezept.crypto.certificate.Oid
import de.gematik.test.erezept.transformation.CardType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.security.PrivateKey

internal class CertificateElementTest {

  private lateinit var cert: CertificateElement<PrivateKey>

  @BeforeEach
  fun setup() {
    cert = Path.of("src/test/resources/80276883110000113311-C_CH_AUT_R2048.p12")
            .getCertificateElements().first()
  }

  @Test
  fun `check if the correct Iccsn is returned`() {
    assertEquals("80276883110000113311", cert.getIccsn())
  }

  @Test
  fun `check if the correct kvnr is returned`() {
    assertEquals("X110406067", cert.getKvid())
  }

  @Test
  fun `check if the correct crypto method is returned`() {
    assertEquals(Crypto.RSA, cert.getCrypto())
  }

  @Test
  fun `check if the correct cert type is returned`() {
    assertEquals(CertType.AUT, cert.getCertType())
  }

  @Test
  fun `check if the correct cert type oid is returned`() {
    assertEquals(Oid.OID_EGK_AUT, cert.getCertTypeOid())
  }

  @Test
  fun `check if the correct card type is returned`() {
    assertEquals(CardType.EGK, cert.getCardType())
  }
}