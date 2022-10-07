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

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
public class SmcB extends Smartcard {

  @Getter @Setter private Certificate[] encCertificateChain;
  @Getter @Setter private X509Certificate encCertificate;
  @Getter @Setter private PrivateKey encPrivateKey;

  public SmcB(String iccsn) {
    super(iccsn, SmartcardType.SMC_B);
  }

  public String getTelematikId() {
    return this.getOwner().getOrganization();
  }

  @Override
  public void destroy() {
    // nothing to be done!
  }
}
