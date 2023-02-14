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

import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@AllArgsConstructor()
public class AesGcm implements EncryptionMethod<SecretKey, Key> {

  private final int ivSize;
  private final int tagSize;

  @SneakyThrows
  public byte[] decrypt(Key aesKey, byte[] encryptedData) {
    return Ciphers.AES_GCM
        .initDecryptMode(aesKey, new GCMParameterSpec(tagSize * 8, encryptedData, 0, ivSize))
        .doFinal(encryptedData, ivSize, encryptedData.length - ivSize);
  }

  @SneakyThrows
  public byte[] encrypt(SecretKey aesKey, byte[] plain) {
    val ivBytes = new byte[ivSize];
    new SecureRandom().nextBytes(ivBytes);
    val cipherBA =
        Ciphers.AES_GCM
            .initEncryptMode(aesKey, new GCMParameterSpec(tagSize * 8, ivBytes))
            .doFinal(plain);

    byte[] result = new byte[ivBytes.length + cipherBA.length];
    System.arraycopy(ivBytes, 0, result, 0, ivBytes.length);
    System.arraycopy(cipherBA, 0, result, ivBytes.length, cipherBA.length);
    return result;
  }
}
