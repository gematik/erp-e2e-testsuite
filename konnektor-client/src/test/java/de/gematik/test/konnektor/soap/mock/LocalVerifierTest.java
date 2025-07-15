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
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import java.nio.charset.StandardCharsets;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LocalVerifierTest {
  private static final String CONTENT = "ÜÄÖüöä!@#$%^&*()_+{}|:\"<>?`~[]\\;',./-= \n\t";
  private static byte[] qes;

  @BeforeAll
  public static void setup() {
    val archive = SmartcardArchive.fromResources();
    val hba = archive.getHba(0);
    qes =
        LocalSigner.signQES(hba, CryptoSystem.ECC_256)
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
  }
}
