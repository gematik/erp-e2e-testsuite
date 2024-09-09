/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.client.vau.protocol;

import de.gematik.bbriccs.crypto.BC;
import de.gematik.test.erezept.client.vau.protocol.exceptions.VauProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import javax.crypto.BadPaddingException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;

public class VauProtocol {
  @Getter private final VauVersion vauVersion;
  private final ECPublicKey publicKey;
  private final SecretKey decryptionKey;
  @Getter private byte[] requestId;

  public VauProtocol(VauVersion vauVersion, ECPublicKey publicKey) {
    this.vauVersion = vauVersion;
    this.publicKey = publicKey;
    this.decryptionKey = genDecryptKey(vauVersion.getKeySize());
  }

  @SneakyThrows
  private static SecretKey genDecryptKey(int keySize) {
    val keyGenerator = KeyGenerator.getInstance("AES", BC.getSecurityProvider());
    keyGenerator.init(keySize);
    return keyGenerator.generateKey();
  }

  private static byte[] genRequestId(int size) {
    val requestId = new byte[size];
    new SecureRandom().nextBytes(requestId);
    return requestId;
  }

  public byte[] encryptRawVauRequest(final String bearer, final byte[] innerHttp) {
    val symmetricalKeyHex = toLowerCaseHex(decryptionKey.getEncoded());
    requestId = genRequestId(vauVersion.getReqIdSize());
    val requestIdHex = toLowerCaseHex(requestId);
    return vauVersion
        .getAsymmetricMethod()
        .encrypt(
            publicKey,
            composeInnerHttp(
                innerHttp,
                vauVersion.getVersionForInnerHttp(),
                bearer.getBytes(StandardCharsets.UTF_8),
                requestIdHex,
                symmetricalKeyHex));
  }

  private byte[] toLowerCaseHex(byte[] data) {
    val buffer = new byte[data.length * 2];
    for (int i = 0; i < data.length; i++) {
      val value = data[i] & 0xFF;
      buffer[i * 2] = hexMap((value / 16) % 16);
      buffer[i * 2 + 1] = hexMap(value % 16);
    }
    return buffer;
  }

  @SneakyThrows
  private byte hexMap(int intValue) {
    if (intValue >= 0 && intValue <= 9) {
      val integerValue = Integer.valueOf(intValue + 48);
      return integerValue.byteValue();
    } else if (intValue >= 10 && intValue <= 15) {
      val integerValue = Integer.valueOf(intValue + 97 - 10);
      return integerValue.byteValue();
    } else {
      throw new VauProtocolException("parameter must be between 0 to 15");
    }
  }

  @SuppressWarnings({
    "java:S1130"
  }) // possibility required to catch BadPaddingExceptions on this call
  public byte[] decryptRawVauResponse(byte[] innerHttp) throws BadPaddingException {
    return vauVersion.getSymmetricMethod().decrypt(decryptionKey, innerHttp);
  }

  private byte[] composeInnerHttp(
      byte[] innerHttp, char version, byte[] bearer, byte[] requestId, byte[] symmetricalKey) {
    val byteSpace = (byte) 32;
    val len =
        1
            + 1
            + bearer.length
            + 1
            + requestId.length
            + 1
            + symmetricalKey.length
            + 1
            + innerHttp.length;
    return ByteBuffer.allocate(len)
        .put((byte) version)
        .put(byteSpace)
        .put(bearer)
        .put(byteSpace)
        .put(requestId)
        .put(byteSpace)
        .put(symmetricalKey)
        .put(byteSpace)
        .put(innerHttp)
        .array();
  }
}
