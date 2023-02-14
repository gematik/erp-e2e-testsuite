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

package de.gematik.test.erezept.crypto.encryption;

import static org.junit.Assert.assertEquals;

import de.gematik.test.erezept.crypto.BC;
import de.gematik.test.erezept.crypto.exceptions.EncryptionException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class EciesTest {

  private static KeyPair keyPair;
  private static Ecies ecies;

  @SneakyThrows
  @BeforeAll
  public static void setup() {
    ecies =
        new Ecies((byte) 1, "empty".getBytes(StandardCharsets.UTF_8), 12, 16, "brainpoolP256r1");

    val keyPairGenerator = KeyPairGenerator.getInstance("EC", BC.getSecurityProvider());
    keyPairGenerator.initialize(new ECGenParameterSpec(ecies.getCurve()));
    keyPair = keyPairGenerator.generateKeyPair();
  }

  @Test
  void encryptAndDecrypt() {
    val encrypt =
        ecies.encrypt((ECPublicKey) keyPair.getPublic(), "Test".getBytes(StandardCharsets.UTF_8));
    val decrypt = ecies.decrypt(keyPair.getPrivate(), encrypt);
    assertEquals("Test", new String(decrypt, StandardCharsets.UTF_8));
  }

  @Test
  void invalidVersionType() {
    val encrypt = new byte[1];
    val privateKey = keyPair.getPrivate();
    val thrown =
        Assertions.assertThrows(
            EncryptionException.class, () -> ecies.decrypt(privateKey, encrypt));
    Assertions.assertEquals("Invalid version byte", thrown.getMessage());
  }

  @Test
  void invalidCipherTextLength() {
    val encrypt = new byte[1 + 32 * 2 + 12];
    encrypt[0] = (byte) 1;
    val privateKey = keyPair.getPrivate();
    val thrown =
        Assertions.assertThrows(
            EncryptionException.class, () -> ecies.decrypt(privateKey, encrypt));
    Assertions.assertEquals("Ciphertext too small!", thrown.getMessage());
  }
}
