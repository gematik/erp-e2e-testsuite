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
import de.gematik.test.erezept.crypto.exceptions.EncryptionException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECPublicKeySpec;

@AllArgsConstructor
public class Ecies implements EncryptionMethod<ECPublicKey, PrivateKey> {
  private final byte version;
  private final byte[] info;
  private final int ivSize;
  private final int aesSize;
  @Getter private final String curve;

  @SneakyThrows
  private SecretKeySpec genAesKey(ECPublicKey otherECPublicKey, PrivateKey privateKey) {
    val keyAgreement = KeyAgreement.getInstance("ECDH", BC.getSecurityProvider());
    keyAgreement.init(privateKey);
    keyAgreement.doPhase(otherECPublicKey, true);
    val secret = keyAgreement.generateSecret();

    val aesKey = new byte[aesSize];
    val hkdfBytesGenerator = new HKDFBytesGenerator(new SHA256Digest());
    hkdfBytesGenerator.init(new HKDFParameters(secret, null, info));
    hkdfBytesGenerator.generateBytes(aesKey, 0, aesKey.length);

    return new SecretKeySpec(aesKey, "AES");
  }

  @SneakyThrows
  public byte[] encrypt(ECPublicKey otherECPublicKey, byte[] plain) {
    val ivBytes = new byte[ivSize];
    new SecureRandom().nextBytes(ivBytes);
    val ivSpec = new IvParameterSpec(ivBytes);

    val keyPairGenerator = KeyPairGenerator.getInstance("EC", BC.getSecurityProvider());
    keyPairGenerator.initialize(new ECGenParameterSpec(curve));
    val eKp = keyPairGenerator.generateKeyPair();

    val aesKey = genAesKey(otherECPublicKey, eKp.getPrivate());
    val cipherText = Ciphers.AES_GCM.initEncryptMode(aesKey, ivSpec).doFinal(plain);

    val x = ((ECPublicKey) eKp.getPublic()).getW().getAffineX().toByteArray();
    val y = ((ECPublicKey) eKp.getPublic()).getW().getAffineY().toByteArray();

    byte[] ret = new byte[1 + 32 * 2 + ivSize + cipherText.length];
    // copy order is important
    System.arraycopy(y, 0, ret, 1 + 32 + 32 - y.length, y.length);
    System.arraycopy(x, 0, ret, 1 + 32 - x.length, x.length);
    ret[0] = version;
    System.arraycopy(ivBytes, 0, ret, 1 + 32 + 32, ivBytes.length);
    System.arraycopy(cipherText, 0, ret, 1 + 32 + 32 + ivSize, cipherText.length);

    return ret;
  }

  @SneakyThrows
  @SuppressWarnings("java:S3329")
  public byte[] decrypt(PrivateKey privateKey, byte[] encryptedData) {
    if (encryptedData[0] != version) {
      throw new EncryptionException("Invalid version byte");
    }
    if (encryptedData.length <= 1 + 32 * 2 + ivSize) {
      throw new EncryptionException("Ciphertext too small!");
    }
    val x = new BigInteger(1, copyOfRange(encryptedData, 1, 1 + 32));
    val y = new BigInteger(1, copyOfRange(encryptedData, 1 + 32, 1 + 32 * 2));

    val curveSpec = ECNamedCurveTable.getParameterSpec(curve);
    val ecPublicKeySpec = new ECPublicKeySpec(curveSpec.getCurve().createPoint(x, y), curveSpec);
    val otherPublicKey = KeyFactory.getInstance("EC").generatePublic(ecPublicKeySpec);

    val ivSpec = new IvParameterSpec(encryptedData, 1 + 32 * 2, ivSize);

    val aesKey = genAesKey((ECPublicKey) otherPublicKey, privateKey);
    return Ciphers.AES_GCM
        .initDecryptMode(aesKey, ivSpec)
        .doFinal(encryptedData, 1 + 32 * 2 + ivSize, encryptedData.length - (1 + 32 * 2 + ivSize));
  }

  private byte[] copyOfRange(byte[] data, int fromIndex, int toIndex) {
    val ret = new byte[toIndex - fromIndex];
    System.arraycopy(data, fromIndex, ret, 0, ret.length);
    return ret;
  }
}
