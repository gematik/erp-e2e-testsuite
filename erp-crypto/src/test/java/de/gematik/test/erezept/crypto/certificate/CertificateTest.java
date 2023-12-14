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

package de.gematik.test.erezept.crypto.certificate;

import java.security.*;
import java.security.KeyStore.*;
import java.security.cert.*;
import lombok.*;
import org.junit.jupiter.api.*;

class CertificateTest {

  private X509CertificateWrapper smcbAutEccCert;
  private X509CertificateWrapper smcbEncRsaCert;

  @BeforeEach
  void setUp() {
    smcbEncRsaCert = loadCertificateFrom("80276883110000116873-C_HCI_ENC_R2048.p12");
    smcbAutEccCert = loadCertificateFrom("80276001011699900861-C_SMCB_AUT_E256_X509.p12");
  }

  @SneakyThrows
  private X509CertificateWrapper loadCertificateFrom(@NonNull String p12File) {
    val is = ClassLoader.getSystemResourceAsStream(p12File);
    val ks = KeyStore.getInstance("PKCS12");
    ks.load(is, "00".toCharArray());
    val alias =
        ks.aliases()
            .nextElement(); // use only the first element as each file has only a single alias
    val privateKeyEntry =
        (PrivateKeyEntry) ks.getEntry(alias, new PasswordProtection("00".toCharArray()));
    return new X509CertificateWrapper((X509Certificate) privateKeyEntry.getCertificate());
  }

  @Test
  void validateCertificateInformations() {
    Assertions.assertEquals(
        "3-SMC-B-Testkarte-883110000116873", smcbEncRsaCert.getProfessionId().orElseThrow());
    Assertions.assertEquals(
        Oid.OID_SMC_B_ENC, smcbEncRsaCert.getCertificateTypeOid().orElseThrow());
    Assertions.assertTrue(smcbEncRsaCert.isRsaEncryption());
    Assertions.assertNotNull(smcbEncRsaCert.toCertificateHolder());

    Assertions.assertEquals(
        "5-2-KH-APO-Waldesrand-01", smcbAutEccCert.getProfessionId().orElseThrow());
    Assertions.assertEquals(
        Oid.OID_SMC_B_AUT, smcbAutEccCert.getCertificateTypeOid().orElseThrow());
    Assertions.assertFalse(smcbAutEccCert.isRsaEncryption());
    Assertions.assertNotNull(smcbAutEccCert.toCertificateHolder());
  }
}
