/*
 * Copyright (c) 2022 gematik GmbH
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
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@AllArgsConstructor
public enum Ciphers {
  AES_GCM("AES/GCM/NoPadding");
  private final String transformation;

  @SneakyThrows
  private Cipher getCipherFor(int mode, Key key, AlgorithmParameterSpec spec) {
    val cipher = Cipher.getInstance(transformation, BC.getSecurityProvider());
    cipher.init(mode, key, spec);
    return cipher;
  }

  @SneakyThrows
  public Cipher initDecryptMode(Key key, AlgorithmParameterSpec spec) {
    return getCipherFor(Cipher.DECRYPT_MODE, key, spec);
  }

  @SneakyThrows
  public Cipher initEncryptMode(Key key, AlgorithmParameterSpec spec) {
    return getCipherFor(Cipher.ENCRYPT_MODE, key, spec);
  }
}
