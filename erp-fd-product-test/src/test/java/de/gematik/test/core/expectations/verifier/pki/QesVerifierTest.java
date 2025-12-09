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

package de.gematik.test.core.expectations.verifier.pki;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.Hba;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.konnektor.soap.mock.LocalSigner;
import de.gematik.test.konnektor.soap.mock.utils.OcspTokenGenerator;
import eu.europa.esig.dss.spi.x509.revocation.ocsp.OCSPToken;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class QesVerifierTest {

  private static Hba hba;
  private static OCSPToken selfSignedOcspToken;
  private static OCSPToken onlineOcspToken;

  @BeforeAll
  static void setup() {
    hba = SmartcardArchive.fromResources().getHbaByICCSN("80276883110000095767");
    onlineOcspToken =
        OcspTokenGenerator.with(
                hba.getQesCertificate(CryptoSystem.DEFAULT_CRYPTO_SYSTEM).getX509Certificate())
            .asOnlineToken();

    val producedAt = ZonedDateTime.now();
    val updatedAt = producedAt.minusDays(5);
    selfSignedOcspToken =
        OcspTokenGenerator.with(
                hba.getQesCertificate(CryptoSystem.DEFAULT_CRYPTO_SYSTEM).getX509Certificate())
            .asSelfSignedToken(producedAt, updatedAt);
  }

  @Test
  void shouldVerifyValidQes() {
    CoverageReporter.getInstance().startTestcase("don't care");

    val validSignedQes =
        LocalSigner.signQES(hba, CryptoSystem.DEFAULT_CRYPTO_SYSTEM)
            .signDocument(List.of(onlineOcspToken), new byte[] {1, 2, 3});
    val acceptBundle = mock(ErxAcceptBundle.class);
    when(acceptBundle.getSignedKbvBundle()).thenReturn(validSignedQes);

    val verifierQes = OCSPRespVerifier.isQesValid();
    verifierQes.apply(acceptBundle);

    val verifierContainsOnlineOcsp = OCSPRespVerifier.containsOcspResp(onlineOcspToken);
    verifierContainsOnlineOcsp.apply(acceptBundle);
  }

  @Test
  void shouldReplaceOcspResp() {
    CoverageReporter.getInstance().startTestcase("don't care");
    val validSignedQes =
        LocalSigner.signQES(hba, CryptoSystem.DEFAULT_CRYPTO_SYSTEM)
            .signDocument(true, new byte[] {1, 2, 3});
    val acceptBundle = mock(ErxAcceptBundle.class);
    when(acceptBundle.getSignedKbvBundle()).thenReturn(validSignedQes);

    val verifier = OCSPRespVerifier.replacedOcspResp(selfSignedOcspToken);
    verifier.apply(acceptBundle);
  }
}
