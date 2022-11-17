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

package de.gematik.test.smartcard;

import java.io.File;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Objects;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.val;

@SuperBuilder
@Getter
public class Hba extends Smartcard {

  private Certificate[] qesCertificateChain;

  private X509Certificate qesCertificate;

  private File qesP12File; // required for CAdESSignature

  private String qesP12Password; // required for CAdESSignature

  private PrivateKey qesPrivateKey;

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    val hba = (Hba) o;
    return Arrays.equals(getQesCertificateChain(), hba.getQesCertificateChain())
        && Objects.equals(getQesCertificate(), hba.getQesCertificate())
        && Objects.equals(getQesP12Password(), hba.getQesP12Password())
        && Objects.equals(getQesPrivateKey(), hba.getQesPrivateKey());
  }

  @Override
  public int hashCode() {
    int result =
        Objects.hash(
            super.hashCode(), getQesCertificate(), getQesP12Password(), getQesPrivateKey());
    result = 31 * result + Arrays.hashCode(getQesCertificateChain());
    return result;
  }
}
