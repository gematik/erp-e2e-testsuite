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

package de.gematik.test.konnektor.soap.mock;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.Hba;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.konnektor.soap.mock.utils.OcspTokenGenerator;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LocalVerifierTest {
  private static final String CONTENT = "ÜÄÖüöä!@#$%^&*()_+{}|:\"<>?`~[]\\;',./-= \n\t";
  private static byte[] qes;
  private static Hba hba;

  @BeforeAll
  public static void setup() {
    val archive = SmartcardArchive.fromResources();
    hba = archive.getHbaByICCSN("80276883110000095767");
    qes =
        LocalSigner.signQES(hba, CryptoSystem.DEFAULT_CRYPTO_SYSTEM)
            .signDocument(true, CONTENT.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void shouldParseSuccessfully() {
    Assertions.assertDoesNotThrow(() -> LocalVerifier.parse(qes));
  }

  @Test
  void shouldThrowWhenInputIsInvalid() {
    assertThrows(UnsupportedOperationException.class, () -> LocalVerifier.verify(new byte[0]));
  }

  @Test
  void shouldReturnTrueWhenSignatureIsValid() {
    assertTrue(LocalVerifier.verify(qes));
  }

  @Test
  void shouldReturnADocumentAsString() {
    val localVerifier = LocalVerifier.parse(qes);
    assertNotNull(localVerifier.getDocument());
    assertEquals(CONTENT, localVerifier.getDocument());
    assertNotNull(localVerifier.getFirstDocument());
  }

  @Test
  void shouldContainOcspResponse() {
    val localVerifier = LocalVerifier.parse(qes);
    assertEquals(1, localVerifier.getOcspTokens().size());
  }

  @Test
  void shouldNotValidWithRevokedOcspResp() {
    val ocspToken =
        OcspTokenGenerator.with(
                hba.getQesCertificate(CryptoSystem.DEFAULT_CRYPTO_SYSTEM).getX509Certificate())
            .asSelfSignedRevokedToken(ZonedDateTime.now(), ZonedDateTime.now());
    val invalidQes =
        LocalSigner.signQES(hba, CryptoSystem.DEFAULT_CRYPTO_SYSTEM)
            .signDocument(List.of(ocspToken), CONTENT.getBytes(StandardCharsets.UTF_8));
    val localVerifier = LocalVerifier.parse(invalidQes);
    assertFalse(localVerifier.verify());
  }

  @Test
  void shouldNotValidWithInvalidOcspResp() {
    val ocspToken =
        OcspTokenGenerator.with(
                hba.getQesCertificate(CryptoSystem.DEFAULT_CRYPTO_SYSTEM).getX509Certificate())
            .asSelfSignedToken(ZonedDateTime.now(), ZonedDateTime.now());
    val invalidQes =
        LocalSigner.signQES(hba, CryptoSystem.DEFAULT_CRYPTO_SYSTEM)
            .signDocument(List.of(ocspToken), CONTENT.getBytes(StandardCharsets.UTF_8));
    val localVerifier = LocalVerifier.parse(invalidQes);
    assertFalse(localVerifier.verify());
  }
}
