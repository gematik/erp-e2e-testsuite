/*
 * Copyright 2025 gematik GmbH
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

package de.gematik.test.erezept.client.vau;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/** VAU-Host-Verifier To be extended on further demand! */
public class VauHostVerifier implements HostnameVerifier {
  @Override
  public boolean verify(String paramString, SSLSession paramSSLSession) {
    // just accept everything for now!
    return true; // NOSONAR no TLS checks, but that's ok for now!
  }
}
