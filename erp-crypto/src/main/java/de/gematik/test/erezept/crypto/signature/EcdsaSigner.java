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

package de.gematik.test.erezept.crypto.signature;

import java.security.PrivateKey;
import java.security.PublicKey;
import lombok.SneakyThrows;
import lombok.val;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.crypto.signers.StandardDSAEncoding;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;

public class EcdsaSigner implements Signature<PrivateKey, PublicKey> {

  @SneakyThrows
  @Override
  public byte[] sign(PrivateKey key, byte[] data) {
    val signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
    val param = ECUtil.generatePrivateKeyParameter(key);
    signer.init(true, param);
    val signature = signer.generateSignature(data);
    val encoder = new StandardDSAEncoding();
    val parameters = ((ECPrivateKeyParameters) param).getParameters();
    return encoder.encode(parameters.getN(), signature[0], signature[1]);
  }

  @Override
  public byte[] verify(PublicKey key, byte[] data) {
    throw new UnsupportedOperationException();
  }
}
