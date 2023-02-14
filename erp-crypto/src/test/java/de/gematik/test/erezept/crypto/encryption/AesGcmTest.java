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

import de.gematik.test.erezept.crypto.BC;
import java.nio.charset.StandardCharsets;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AesGcmTest {

  private static SecretKey symKey;

  @SneakyThrows
  @BeforeAll
  static void setup() {
    val keyGen = KeyGenerator.getInstance("AES", BC.getSecurityProvider());
    keyGen.init(128);
    symKey = keyGen.generateKey();
  }

  @Test
  void encryptAndDecrypt() {
    val aesGcm = new AesGcm(12, 16);
    val encryptedContent = aesGcm.encrypt(symKey, "Test".getBytes(StandardCharsets.UTF_8));
    val decrypted = aesGcm.decrypt(symKey, encryptedContent);
    Assertions.assertEquals("Test", new String(decrypted, StandardCharsets.UTF_8));
  }
}
