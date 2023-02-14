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

package de.gematik.test.erezept.client.cfg;

import static java.text.MessageFormat.format;

import de.gematik.idp.field.IdpScope;
import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.exceptions.ConfigurationException;
import de.gematik.test.erezept.client.rest.MediaType;
import java.util.Set;
import lombok.Data;
import lombok.val;

@Data
public class ErpClientConfiguration implements INamedConfiguration {

  /** Just an identifier to distinguish from other clients within config.yaml */
  private String name;

  /** Client ID required by the IDP Client */
  private String clientId = "gematikTestPs";

  private String userAgent;

  private String xApiKey;

  /** Redirect URL required by the IDP Client */
  private String redirectUrl;

  /** URL to the Discovery Document on the IDP Server is required by the IDP Client */
  private String discoveryDocumentUrl;

  /** E-Rezept FD URL/FQDN which shall be used by the E-Rezept Client */
  private String fdBaseUrl;

  /**
   * URL/FQDN for downloading the TSL for example from <a
   * href="https://download.tsl.ti-dienste.de/">TI Dienste</a>
   */
  private String tslBaseUrl;

  private String clientType;
  private String acceptMime;
  private String sendMime;
  private String acceptCharset = "utf-8";

  // TODO: get from cfg: validation does not work yet, though!  // NOSONAR still needs to be checked
  // validator throws: java.lang.NoClassDefFoundError: org/apache/commons/codec/Charsets
  private boolean validateRequest = false;
  private boolean validateResponse = false;

  public ClientType getClientType() {
    return ClientType.fromString(clientType);
  }

  public void setClientType(String type) {
    this.clientType = type;
  }

  public void setClientType(ClientType type) {
    this.setClientType(type.toString());
  }

  public MediaType getAcceptMimeType() {
    var ret = MediaType.fromString(acceptMime);

    // fromString generates only send-types but not accept-types!
    if (ret == MediaType.FHIR_JSON) {
      ret = MediaType.ACCEPT_FHIR_JSON;
    } else if (ret == MediaType.FHIR_XML) {
      ret = MediaType.ACCEPT_FHIR_XML;
    } else {
      throw new ConfigurationException(format("Given Accept-Mime ''{0}'' is invalid", acceptMime));
    }
    return ret;
  }

  public MediaType getSendMimeType() {
    val ret = MediaType.fromString(sendMime);

    // check for invalid send mime-types
    if (ret != MediaType.FHIR_JSON && ret != MediaType.FHIR_XML) {
      throw new ConfigurationException(format("Given Send-Mime ''{0}'' is invalid", sendMime));
    }
    return ret;
  }

  public Set<IdpScope> getErpScopes() {
    return Set.of(
        IdpScope.OPENID,
        getFdBaseUrl().startsWith("https://erp-dev.") ? IdpScope.EREZEPTDEV : IdpScope.EREZEPT);
  }
}
