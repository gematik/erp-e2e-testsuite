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

package de.gematik.test.erezept.client.vau;

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/** VAU-Trust-Manager To be extended on further demand! */
public class VauTrustManager implements X509TrustManager {
  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return new X509Certificate[0];
  }

  @Override
  public void checkServerTrusted( // NOSONAR
      X509Certificate[] paramArrayOfX509Certificate, String paramString) {
    // No certificate checking, but that's ok for now: running only as testsuite not within a real
    // product!
  }

  @Override
  public void checkClientTrusted( // NOSONAR
      X509Certificate[] paramArrayOfX509Certificate, String paramString) {
    // No certificate checking, but that's ok for now: running only as testsuite not within a real
    // product!
  }
}
