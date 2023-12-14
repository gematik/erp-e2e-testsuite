/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.client.cfg;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.rest.MediaType;
import de.gematik.test.erezept.config.exceptions.ConfigurationException;
import java.util.Set;
import lombok.*;
import lombok.experimental.Delegate;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class ErpClientConfiguration {

  private interface DelegateExclude {
    // exclude native getClientType()-method because we want to override this one here!
    String getClientType();
  }

  @Delegate(excludes = DelegateExclude.class)
  private final de.gematik.test.erezept.config.dto.erpclient.ErpClientConfiguration dto;

  public ClientType getClientType() {
    return ClientType.fromString(this.dto.getClientType());
  }

  public MediaType getAcceptMimeType() {
    var ret = MediaType.fromString(dto.getAcceptMime());

    // fromString generates only send-types but not accept-types!
    if (ret == MediaType.FHIR_JSON) {
      ret = MediaType.ACCEPT_FHIR_JSON;
    } else if (ret == MediaType.FHIR_XML) {
      ret = MediaType.ACCEPT_FHIR_XML;
    } else {
      throw new ConfigurationException(
          format("Given Accept-Mime ''{0}'' is invalid", dto.getAcceptMime()));
    }
    return ret;
  }

  public MediaType getSendMimeType() {
    val ret = MediaType.fromString(dto.getSendMime());

    // check for invalid send mime-types
    if (ret != MediaType.FHIR_JSON && ret != MediaType.FHIR_XML) {
      throw new ConfigurationException(
          format("Given Send-Mime ''{0}'' is invalid", dto.getSendMime()));
    }
    return ret;
  }

  public Set<String> getErpScopes() {
    return Set.of(
        "openid", dto.getFdBaseUrl().startsWith("https://erp-dev.") ? "e-rezept-dev" : "e-rezept");
  }

  protected static ErpClientConfiguration fromDto(
      de.gematik.test.erezept.config.dto.erpclient.ErpClientConfiguration dto) {
    return new ErpClientConfiguration(dto);
  }
}
