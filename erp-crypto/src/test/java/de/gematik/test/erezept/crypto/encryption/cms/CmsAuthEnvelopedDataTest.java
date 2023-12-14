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

package de.gematik.test.erezept.crypto.encryption.cms;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CmsAuthEnvelopedDataTest {

  private PrivateKey privateKey;
  private X509Certificate x509Certificate;

  @SneakyThrows
  @BeforeEach
  void setUp() {
    val is = ClassLoader.getSystemResourceAsStream("80276883110000116873-C_HCI_ENC_R2048.p12");
    val ks = KeyStore.getInstance("PKCS12");
    ks.load(is, "00".toCharArray());
    val alias =
        ks.aliases()
            .nextElement(); // use only the first element as each file has only a single alias
    val privateKeyEntry =
        (PrivateKeyEntry) ks.getEntry(alias, new PasswordProtection("00".toCharArray()));
    x509Certificate = (X509Certificate) privateKeyEntry.getCertificate();
    privateKey = privateKeyEntry.getPrivateKey();
  }

  @Test
  void test() {
    val cmsAuthEnvelopedData = new CmsAuthEnvelopedData();
    val encrypt =
        cmsAuthEnvelopedData.encrypt(
            List.of(x509Certificate), "Test".getBytes(StandardCharsets.UTF_8));

    val decrypt = cmsAuthEnvelopedData.decrypt(privateKey, encrypt);
    Assertions.assertEquals("Test", new String(decrypt, StandardCharsets.UTF_8));
  }
}
