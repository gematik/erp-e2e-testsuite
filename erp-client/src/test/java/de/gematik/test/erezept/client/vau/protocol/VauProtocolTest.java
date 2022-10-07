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

package de.gematik.test.erezept.client.vau.protocol;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.crypto.BC;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class VauProtocolTest {

  private static VauProtocol vauProtocol;
  private static PrivateKey privateKey;

  @SneakyThrows
  @BeforeAll
  static void setup() {
    val keyPairGenerator = KeyPairGenerator.getInstance("EC", BC.getSecurityProvider());
    keyPairGenerator.initialize(new ECGenParameterSpec(VauVersion.V1.getCurve()));
    val keyPair = keyPairGenerator.generateKeyPair();

    // the public key corresponds to VauCertificate from the erp server
    // private key of the VauCertificate, which is known only by the erp server
    privateKey = keyPair.getPrivate();
    vauProtocol = new VauProtocol(VauVersion.V1, (ECPublicKey) keyPair.getPublic());
  }

  @SneakyThrows
  @Test
  void shouldEncryptAndDecrypt() {
    val testBearer = "123456";
    val text = "test";
    byte[] encrypt =
        vauProtocol.encryptRawVauRequest(testBearer, text.getBytes(StandardCharsets.UTF_8));

    // simulates erp server vau decryption and encryption without changing the message of the client
    byte[] serverDecrypt =
        vauProtocol.getVauVersion().getAsymmetricMethod().decrypt(privateKey, encrypt);
    val encodedKey = hexToByteArray(extractSymKey(serverDecrypt));
    val symKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    val serverEncrypt =
        vauProtocol.getVauVersion().getSymmetricMethod().encrypt(symKey, serverDecrypt);

    val clientDecrypt = vauProtocol.decryptRawVauResponse(serverEncrypt);
    val message = new String(clientDecrypt, StandardCharsets.UTF_8);
    assertTrue(message.endsWith(text));
  }

  private byte[] extractSymKey(byte[] data) {
    int startPos = 0;
    int length = 0;
    int countByteSpace = 1;
    for (int i = 0; i < data.length; i++) {
      if (data[i] == (byte) 32) {
        countByteSpace++;
      }
      if (countByteSpace == 3) startPos = i + 1;
      if (countByteSpace == 4) length = i - startPos;
    }
    val hexByteArray = new byte[length];
    System.arraycopy(data, startPos + 1, hexByteArray, 0, length);
    return hexByteArray;
  }

  private byte[] hexToByteArray(byte[] input) {
    val len = input.length;
    val data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] =
          (byte) ((Character.digit(input[i], 16) << 4) + Character.digit(input[i + 1], 16));
    }
    return data;
  }
}
