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

import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.cert.X509CertificateHolder;

public abstract class Certificate {
  @Getter protected final X509CertificateHolder certHolder;

  @SneakyThrows
  protected Certificate(@NonNull X509Certificate cert) {
    this.certHolder = new X509CertificateHolder(cert.getEncoded());
  }

  protected <T extends ASN1Encodable> Optional<T> getAsn1ElementByType(
      Iterator<ASN1Encodable> iter, Class<T> type) {
    while (iter.hasNext()) {
      val element = iter.next();
      if (type.isInstance(element)) {
        return Optional.of(type.cast(element));
      } else if (element instanceof ASN1Sequence) {
        val ret = getAsn1ElementByType(((ASN1Sequence) element).iterator(), type);
        if (ret.isPresent()) {
          return ret;
        }
      }
    }
    return Optional.empty();
  }
}
