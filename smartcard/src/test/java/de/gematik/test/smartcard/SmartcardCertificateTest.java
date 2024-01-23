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

import de.gematik.test.erezept.crypto.certificate.Oid;
import java.math.BigInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SmartcardCertificateTest {

  private SmartcardCertificate smartcardCertificate;

  @BeforeEach
  void setUp() {
    smartcardCertificate = new SmartcardCertificate("80276883110000113312-C_CH_AUT_E256.p12");
  }

  @Test
  void getX509Certificate() {
    Assertions.assertNotNull(smartcardCertificate.getX509Certificate());
    Assertions.assertEquals(
        "1.2.840.10045.4.3.2", smartcardCertificate.getX509Certificate().getSigAlgOID());
  }

  @Test
  void getX509CertificateHolder() {
    Assertions.assertNotNull(smartcardCertificate.getX509CertificateHolder());
    Assertions.assertEquals(
        new BigInteger("773450634999243"),
        smartcardCertificate.getX509CertificateHolder().getSerialNumber());
  }

  @Test
  void getOid() {
    Assertions.assertNotNull(smartcardCertificate.getX509Certificate());
    Assertions.assertEquals(Oid.OID_EGK_AUT, smartcardCertificate.getOid());
  }

  @Test
  void getInputStreamSupplier() {
    Assertions.assertDoesNotThrow(() -> smartcardCertificate.getInputStreamSupplier());
    Assertions.assertNotNull(smartcardCertificate.getInputStreamSupplier().get());
  }

  @Test
  void getPrivateKey() {
    Assertions.assertNotNull(smartcardCertificate.getPrivateKey());
  }

  @Test
  void getAlgorithm() {
    Assertions.assertEquals(Algorithm.ECC_256, smartcardCertificate.getAlgorithm());
  }
}
