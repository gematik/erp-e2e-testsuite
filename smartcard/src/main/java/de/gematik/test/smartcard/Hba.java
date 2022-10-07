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
import java.io.IOException;
import java.nio.file.Files;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Hba extends Smartcard {

  @Getter @Setter private Certificate[] qesCertificateChain;

  @Setter @Getter private X509Certificate qesCertificate;

  @Setter @Getter private File qesP12File; // required for CAdESSignature

  @Setter @Getter private String qesP12Password; // required for CAdESSignature

  @Setter @Getter private PrivateKey qesPrivateKey;

  public Hba(String iccsn) {
    super(iccsn, SmartcardType.HBA);
  }

  @Override
  public void destroy() {
    if (Files.exists(qesP12File.toPath())) {
      try {
        Files.delete(qesP12File.toPath());
        log.info("Successfully deleted temporary HBA file: " + qesP12File);
      } catch (IOException e) {
        log.error("Could not delete temporary HBA file: " + qesP12File, e);
      }
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    final Hba hba = (Hba) o;
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
