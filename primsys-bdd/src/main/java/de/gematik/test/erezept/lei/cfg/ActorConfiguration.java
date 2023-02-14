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

package de.gematik.test.erezept.lei.cfg;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.cfg.ErpClientConfiguration;
import de.gematik.test.erezept.client.cfg.INamedConfiguration;
import de.gematik.test.smartcard.Crypto;
import lombok.Data;
import lombok.val;

@Data
public abstract class ActorConfiguration implements INamedConfiguration {

  private String name;

  /** which algorithm to use for the Smartcards */
  private String algorithm = "R2048";

  /** Client ID required by the IDP Client */
  private String clientId = "gematikTestPs";

  /** Redirect URL required by the IDP Client */
  private String redirectUrl = "http://test-ps.gematik.de/erezept";

  private String acceptMime = "application/fhir+xml";
  private String sendMime = "application/fhir+xml";
  private String acceptCharset = "utf-8";

  private boolean validateRequest = false;
  private boolean validateResponse = false;

  public Crypto getCryptoAlgorithm() {
    return Crypto.fromString(algorithm);
  }

  private ErpClientConfiguration initializeBaseConfiguration(ClientType type) {
    val erpClientConfig = new ErpClientConfiguration();
    erpClientConfig.setName(this.getName());
    erpClientConfig.setClientId(this.getClientId());
    erpClientConfig.setRedirectUrl(this.getRedirectUrl());
    erpClientConfig.setAcceptMime(this.getAcceptMime());
    erpClientConfig.setSendMime(this.getSendMime());
    erpClientConfig.setAcceptCharset(this.getAcceptCharset());
    erpClientConfig.setRedirectUrl(this.getRedirectUrl());
    erpClientConfig.setValidateRequest(this.isValidateRequest());
    erpClientConfig.setValidateResponse(this.isValidateResponse());
    erpClientConfig.setClientType(type);
    return erpClientConfig;
  }

  public ErpClientConfiguration toErpClientConfig(
      EnvironmentConfiguration environment, ClientType type) {
    val tsl = environment.getTslBaseUrl();
    BackendRouteConfiguration fdRoutes;
    if (type.equals(ClientType.PS)) {
      fdRoutes = environment.getTi();
    } else if (type.equals(ClientType.FDV)) {
      fdRoutes = environment.getInternet();
    } else {
      throw new IllegalArgumentException(format("Given ClientType {0} is not supported", type));
    }

    val erpClientConfig = this.initializeBaseConfiguration(type);

    erpClientConfig.setDiscoveryDocumentUrl(fdRoutes.getDiscoveryDocumentUrl());
    erpClientConfig.setFdBaseUrl(fdRoutes.getFdBaseUrl());
    erpClientConfig.setTslBaseUrl(tsl);
    erpClientConfig.setUserAgent(fdRoutes.getUserAgent());

    return erpClientConfig;
  }
}
