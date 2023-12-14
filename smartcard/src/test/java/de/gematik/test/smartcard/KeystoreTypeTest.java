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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.test.smartcard.exceptions.InvalidFileExtensionException;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class KeystoreTypeTest {

  @Test
  void shouldGetCorrectKeystoreType() {
    val types = List.of(KeystoreType.JKS, KeystoreType.P12);
    types.forEach(t -> assertEquals(t, KeystoreType.fromFileExtension(t.getFileExtension())));
  }
  @Test
  void shouldThrowOnKeystoreTypeNull(){
    assertThrows(NullPointerException.class, () -> KeystoreType.fromFileExtension(null));
  }

  @Test
  void shouldGetCorrectKeystoreTypeFromFilename() {
    val typeP12 = KeystoreType.fromFileExtension("keystore.p12");
    assertEquals(KeystoreType.P12, typeP12);

    val typeJks = KeystoreType.fromFileExtension("keystore.jks");
    assertEquals(KeystoreType.JKS, typeJks);
  }

  @Test
  void shouldGetCorrectKeystoreTypeFromPlainExtension() {
    val typeP12 = KeystoreType.fromFileExtension("p12");
    assertEquals(KeystoreType.P12, typeP12);

    val typeJks = KeystoreType.fromFileExtension("JKS");
    assertEquals(KeystoreType.JKS, typeJks);
  }

  @Test
  void shouldThrowOnInvalidExtension() {
    val invalid = List.of("test.txt", "keystore.p120", "exe", "");
    invalid.forEach(
        ext ->
            assertThrows(
                InvalidFileExtensionException.class, () -> KeystoreType.fromFileExtension(ext)));
  }
}
