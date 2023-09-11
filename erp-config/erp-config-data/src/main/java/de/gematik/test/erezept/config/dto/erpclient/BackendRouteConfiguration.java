/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.config.dto.erpclient;

import lombok.*;

@Data
public class BackendRouteConfiguration {

  private String discoveryDocumentUrl;
  private String fdBaseUrl;
  private String subscriptionServiceUrl;
  private String xapiKey;
  private String userAgent;

  /** Client ID required by the IDP Client */
  private String clientId = "gematikTestPs";

  /** Redirect URL required by the IDP Client */
  private String redirectUrl = "http://test-ps.gematik.de/erezept";

  private String vsdServiceHMacKey;
}