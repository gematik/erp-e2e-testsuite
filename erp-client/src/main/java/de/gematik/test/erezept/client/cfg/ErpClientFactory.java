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

package de.gematik.test.erezept.client.cfg;

import de.gematik.idp.client.IdpClient;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.rest.ErpResponseFactory;
import de.gematik.test.erezept.client.vau.VauClient;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class ErpClientFactory {

  private ErpClientFactory() {
    // don't instantiate!
    throw new AssertionError();
  }

  /**
   * Build, configure and assemble the E-Rezept Client from a given configuration
   *
   * @param cfg is the given Configuration
   * @return a properly configured {@link ErpClient}
   */
  @SneakyThrows
  public static ErpClient createErpClient(ErpClientConfiguration cfg) {
    // build and configure the IDP-Client
    val idp =
        IdpClient.builder()
            .clientId(cfg.getClientId())
            .redirectUrl(cfg.getRedirectUrl())
            .discoveryDocumentUrl(cfg.getDiscoveryDocumentUrl())
            .scopes(cfg.getErpScopes())
            .build();

    // build and configure the VAU-Client
    val vau =
        new VauClient(
            cfg.getFdBaseUrl(), cfg.getClientType(), cfg.getXApiKey(), cfg.getUserAgent());

    val fhir = new FhirParser(); // fhir parser which we will use for encode/decode and validation
    // build, configure and assemble the Erp-Client
    return ErpClient.builder()
        .idpClient(idp)
        .vauClient(vau)
        .fhir(fhir)
        .responseFactory(new ErpResponseFactory(fhir))
        .baseFdUrl(cfg.getFdBaseUrl())
        .acceptCharset(cfg.getAcceptCharset())
        .acceptMime(cfg.getAcceptMimeType())
        .sendMime(cfg.getSendMimeType())
        .validateRequest(cfg.isValidateRequest())
        .validateResponse(cfg.isValidateResponse())
        .build();
  }
}
