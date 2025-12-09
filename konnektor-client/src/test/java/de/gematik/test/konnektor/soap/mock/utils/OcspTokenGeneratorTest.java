/*
 * Copyright 2025 gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.konnektor.soap.mock.utils;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.bbriccs.smartcards.SmartcardCertificate;
import eu.europa.esig.dss.enumerations.CertificateStatus;
import java.time.ZonedDateTime;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OcspTokenGeneratorTest {

  private static SmartcardCertificate validSigningCert;

  @BeforeAll
  static void setup() {
    SmartcardArchive smartcardArchive = SmartcardArchive.fromResources();
    validSigningCert =
        smartcardArchive
            .getHbaByICCSN("80276883110000095767")
            .getQesCertificate(CryptoSystem.DEFAULT_CRYPTO_SYSTEM);
  }

  @Test
  void shouldGenerateOnlineOcspToken() {
    val generator = OcspTokenGenerator.with(validSigningCert.getX509Certificate());
    assertDoesNotThrow(generator::asOnlineToken);

    val ocspToken = generator.asOnlineToken();
    assertNotNull(ocspToken);
    assertNotEquals(CertificateStatus.REVOKED, ocspToken.getLatestSingleResp().getCertStatus());
  }

  @Test
  void shouldGenerateSelfSignedOcspToken() {
    val generator = OcspTokenGenerator.with(validSigningCert.getX509Certificate());
    val producedAt = ZonedDateTime.now();
    val updateAt = producedAt.minusDays(5);

    assertDoesNotThrow(() -> generator.asSelfSignedToken(producedAt, updateAt));

    val ocspToken = generator.asSelfSignedToken(producedAt, updateAt);
    assertNotNull(ocspToken);
    assertNotEquals(CertificateStatus.REVOKED, ocspToken.getLatestSingleResp().getCertStatus());
  }
}
