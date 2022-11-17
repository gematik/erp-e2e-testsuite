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

package de.gematik.test.erezept.crypto.signature;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Arrays;
import lombok.val;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.engines.RSABlindedEngine;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.jcajce.provider.util.DigestFactory;

public class RsaPssSigner implements Signature<PrivateKey, PublicKey> {

  private final byte trailer;
  private final Digest contentDigest;
  private final RSABlindedEngine cipher;
  private final int hLen;
  private final int sLen;

  private int emBits;
  private byte[] block;
  private SecureRandom random;
  private final int mgfhLen;

  private RsaPssSigner(PSSParameterSpec spec) {
    this.cipher = new RSABlindedEngine();
    this.contentDigest = DigestFactory.getDigest(spec.getDigestAlgorithm());
    this.trailer = org.bouncycastle.crypto.signers.PSSSigner.TRAILER_IMPLICIT;
    this.hLen = contentDigest.getDigestSize();
    this.mgfhLen = contentDigest.getDigestSize();
    this.sLen = spec.getSaltLength();
  }

  @Override
  public byte[] sign(PrivateKey key, byte[] data) {
    init(generatePrivateKeyParameter((RSAPrivateKey) key));
    val salt = new byte[sLen];
    // differing specifications
    val mDash = new byte[8 + sLen + hLen];
    System.arraycopy(data, 0, mDash, 8, data.length);
    // implemented from this point according to org.bouncycastle.crypto.signers.PSSSigner
    random.nextBytes(salt);
    System.arraycopy(salt, 0, mDash, mDash.length - sLen, sLen);
    val h = new byte[hLen];

    contentDigest.update(mDash, 0, mDash.length);
    contentDigest.doFinal(h, 0);

    block[block.length - sLen - 1 - hLen - 1] = 0x01;
    System.arraycopy(salt, 0, block, block.length - sLen - hLen - 1, sLen);

    byte[] dbMask = maskGeneratorFunction1(h, 0, h.length, block.length - hLen - 1);
    for (int i = 0; i != dbMask.length; i++) {
      block[i] ^= dbMask[i];
    }
    block[0] &= (0xff >> ((block.length * 8) - emBits));
    System.arraycopy(h, 0, block, block.length - hLen - 1, hLen);
    block[block.length - 1] = trailer;
    val ret = cipher.processBlock(block, 0, block.length);
    Arrays.fill(block, (byte) 0);
    return ret;
  }

  private byte[] maskGeneratorFunction1(byte[] z, int zOff, int zLen, int length) {
    val mask = new byte[length];
    val hashBuf = new byte[mgfhLen];
    val c = new byte[4];
    int counter = 0;
    contentDigest.reset();
    while (counter < (length / mgfhLen)) {
      int2Octet(counter, c);

      contentDigest.update(z, zOff, zLen);
      contentDigest.update(c, 0, c.length);
      contentDigest.doFinal(hashBuf, 0);

      System.arraycopy(hashBuf, 0, mask, counter * mgfhLen, mgfhLen);

      counter++;
    }
    if ((counter * mgfhLen) < length) {
      int2Octet(counter, c);

      contentDigest.update(z, zOff, zLen);
      contentDigest.update(c, 0, c.length);
      contentDigest.doFinal(hashBuf, 0);

      System.arraycopy(hashBuf, 0, mask, counter * mgfhLen, mask.length - (counter * mgfhLen));
    }
    return mask;
  }

  private void init(CipherParameters param) {
    random = new SecureRandom();
    val kParam = (RSAKeyParameters) param;
    this.cipher.init(true, param);
    emBits = kParam.getModulus().bitLength() - 1;
    block = new byte[(emBits + 7) / 8];
    reset();
  }

  /** int to octet string. */
  private void int2Octet(int i, byte[] sp) {
    sp[0] = (byte) (i >>> 24);
    sp[1] = (byte) (i >>> 16);
    sp[2] = (byte) (i >>> 8);
    sp[3] = (byte) (i);
  }

  private void reset() {
    contentDigest.reset();
  }

  private RSAKeyParameters generatePrivateKeyParameter(RSAPrivateKey key) {
    if (key instanceof RSAPrivateCrtKey crtKey) {
      return new RSAPrivateCrtKeyParameters(
          crtKey.getModulus(),
          crtKey.getPublicExponent(),
          crtKey.getPrivateExponent(),
          crtKey.getPrimeP(),
          crtKey.getPrimeQ(),
          crtKey.getPrimeExponentP(),
          crtKey.getPrimeExponentQ(),
          crtKey.getCrtCoefficient());
    } else {
      return new RSAKeyParameters(true, key.getModulus(), key.getPrivateExponent());
    }
  }

  public static RsaPssSigner sha256withMgf1() {
    val spec = new PSSParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), 32, 1);
    return new RsaPssSigner(spec);
  }

  @Override
  public byte[] verify(PublicKey key, byte[] data) {
    throw new UnsupportedOperationException();
  }
}
