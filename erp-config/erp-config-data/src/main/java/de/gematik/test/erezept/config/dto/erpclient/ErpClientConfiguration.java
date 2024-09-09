/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.config.dto.erpclient;

import de.gematik.test.erezept.config.dto.actor.BaseActorConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ErpClientConfiguration extends BaseActorConfiguration {

  /** User Agent to be used * */
  private String userAgent;

  /** Client ID required by the IDP Client */
  private String clientId;

  /** URL to the Discovery Document on the IDP Server is required by the IDP Client */
  private String discoveryDocumentUrl;

  /** Redirect URL required by the IDP Client */
  private String redirectUrl;

  /** E-Rezept FD URL/FQDN which shall be used by the E-Rezept Client */
  private String fdBaseUrl;

  /**
   * URL/FQDN for downloading the TSL for example from <a
   * href="https://download.tsl.ti-dienste.de/">TI Dienste</a>
   */
  private String tslBaseUrl;

  /** Client-Type distinguishing FDV and PS clients */
  private String clientType;

  /** API-Key required for FDV-Clients which are routing via Internet */
  private String xApiKey;
}
