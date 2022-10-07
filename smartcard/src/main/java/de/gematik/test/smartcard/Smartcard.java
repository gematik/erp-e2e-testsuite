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

import static java.text.MessageFormat.format;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import lombok.Data;

@Data
public abstract class Smartcard {

  private final String iccsn;
  private final SmartcardType type;

  private BigInteger serialnumber;
  private Crypto algorithm;

  private Certificate[] authCertificateChain;
  private X509Certificate authCertificate;
  private PrivateKey authPrivateKey;

  private SmartcardOwnerData owner;

  protected Smartcard(String iccsn, SmartcardType type) {
    this.iccsn = iccsn;
    this.type = type;
  }

  public abstract void destroy();

  @Override
  public String toString() {
    return format(
        "Smartcard {0} [iccsn={1}, sn={2}, algorithm={3}]", type, iccsn, serialnumber, algorithm);
  }
}
