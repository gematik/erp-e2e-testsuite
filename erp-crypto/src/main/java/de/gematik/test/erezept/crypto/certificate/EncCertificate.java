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

package de.gematik.test.erezept.crypto.certificate;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.crypto.exceptions.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;

public class EncCertificate extends Certificate {

  @SneakyThrows
  public EncCertificate(@NonNull X509Certificate cert) {
    super(cert);

    // TODO: only RSA is supported
    if (!isRsaEncCert()) {
      throw new CertificateException(
          format(
              "Certificate {} does not contain the RSA encryption oid",
              cert.getSubjectX500Principal().getName()));
    }
  }

  private boolean isRsaEncCert() {
    return certHolder
        .getSubjectPublicKeyInfo()
        .getAlgorithm()
        .getAlgorithm()
        .getId()
        .equals(PKCSObjectIdentifiers.rsaEncryption.getId());
  }

  public Optional<String> getTelematikId() {
    return getProfessionItemValue();
  }
}
