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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.client.cfg;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.rest.headers.AuthHttpHeaderKey;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import de.gematik.idp.client.IdpClient;
import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.UnirestRetryWrapper;
import de.gematik.test.erezept.client.rest.ErpResponseFactory;
import de.gematik.test.erezept.client.vau.VauClient;
import de.gematik.test.erezept.client.vau.VauException;
import de.gematik.test.erezept.config.dto.actor.BaseActorConfiguration;
import de.gematik.test.erezept.config.dto.actor.PatientConfiguration;
import de.gematik.test.erezept.config.dto.actor.PsActorConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.BackendRouteConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import kong.unirest.core.Unirest;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@UtilityClass
public class ErpClientFactory {

  private static final UnirestRetryWrapper retryWrapper = new UnirestRetryWrapper();
  private static X509Certificate vauCertificate;

  public static ErpClient createErpClient(
      EnvironmentConfiguration environment, PsActorConfiguration actor) {
    val erpClientConfig = toErpClientConfig(environment.getTi(), ClientType.PS, actor);
    return createErpClient(erpClientConfig, getVauCertificate(erpClientConfig));
  }

  public static ErpClient createErpClient(
      EnvironmentConfiguration environment, PatientConfiguration actor) {
    val erpClientConfig = toErpClientConfig(environment.getInternet(), ClientType.FDV, actor);
    erpClientConfig.setXApiKey(environment.getInternet().getXapiKey());
    return createErpClient(erpClientConfig, getVauCertificate(erpClientConfig));
  }

  /**
   * Build, configure and assemble the E-Rezept Client from a given configuration
   *
   * @param cfg is the given Configuration
   * @return a properly configured {@link ErpClient}
   */
  private static ErpClient createErpClient(
      ErpClientConfiguration cfg, X509Certificate vauCertificate) {
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
            cfg.getFdBaseUrl(),
            cfg.getClientType(),
            vauCertificate,
            cfg.getXApiKey(),
            cfg.getUserAgent());

    val fhir = new FhirParser(); // fhir parser which we will use for encode/decode and validation
    // build, configure and assemble the Erp-Client
    return ErpClient.builder()
        .clientType(cfg.getClientType())
        .idpClient(idp)
        .vauClient(vau)
        .fhir(fhir)
        .responseFactory(new ErpResponseFactory(fhir, cfg.isValidateResponse()))
        .baseFdUrl(cfg.getFdBaseUrl())
        .acceptCharset(cfg.getAcceptCharset())
        .acceptMime(cfg.getAcceptMimeType())
        .sendMime(cfg.getSendMimeType())
        .validateRequest(cfg.isValidateRequest())
        .build();
  }

  private static ErpClientConfiguration toErpClientConfig(
      BackendRouteConfiguration route, ClientType type, BaseActorConfiguration actor) {
    val erpClientConfig = initializeBaseConfiguration(actor, type);

    erpClientConfig.setDiscoveryDocumentUrl(route.getDiscoveryDocumentUrl());
    erpClientConfig.setFdBaseUrl(route.getFdBaseUrl());
    erpClientConfig.setTslBaseUrl(route.getTslBaseUrl());
    erpClientConfig.setUserAgent(route.getUserAgent());
    erpClientConfig.setClientId(route.getClientId());
    erpClientConfig.setRedirectUrl(route.getRedirectUrl());

    return erpClientConfig;
  }

  private static ErpClientConfiguration initializeBaseConfiguration(
      BaseActorConfiguration actor, ClientType type) {
    val dto = new de.gematik.test.erezept.config.dto.erpclient.ErpClientConfiguration();
    dto.setName(actor.getName());
    dto.setAcceptMime(actor.getAcceptMime());
    dto.setSendMime(actor.getSendMime());
    dto.setAcceptCharset(actor.getAcceptCharset());
    dto.setValidateRequest(actor.isValidateRequest());
    dto.setValidateResponse(actor.isValidateResponse());
    dto.setClientType(type.toString());
    return de.gematik.test.erezept.client.cfg.ErpClientConfiguration.fromDto(dto);
  }

  private static X509Certificate getVauCertificate(ErpClientConfiguration clientConfig) {
    if (vauCertificate == null) {
      vauCertificate = requestVauCertificate(clientConfig);
    }
    return vauCertificate;
  }

  @SneakyThrows
  private static X509Certificate requestVauCertificate(ErpClientConfiguration clientConfig) {
    val certificateFactory = CertificateFactory.getInstance("X.509");
    val certUrl = format("{0}/VAUCertificate", clientConfig.getFdBaseUrl());

    val xApiKey = clientConfig.getXApiKey();
    val userAgent = clientConfig.getUserAgent();

    val headers = new HashMap<String, String>();
    if (xApiKey != null && !xApiKey.isEmpty()) {
      AuthHttpHeaderKey.X_API_KEY.createHeader(xApiKey).apply(headers::put);
    }

    if (userAgent != null && !userAgent.isEmpty()) {
      StandardHttpHeaderKey.USER_AGENT.createHeader(userAgent).apply(headers::put);
    }

    log.info("Request {} Certificate from {}", certificateFactory.getType(), certUrl);
    val req = Unirest.get(certUrl).headers(headers);

    try {
      val response = retryWrapper.requestWithRetries(req);
      val data = response.getBody();
      log.info(
          "Received response with {} bytes ({}) from VAU-Certificate with status code {}",
          data.length,
          response.getHeaders().getFirst("content-type"),
          response.getStatus());
      val byteArrayInputStream = new ByteArrayInputStream(data);
      val cert = (X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream);
      log.info("Received VAU-Certificate successfully");
      return cert;
    } catch (CertificateException e) {
      throw new VauException("Error during Requesting VAU-Certificate", e);
    }
  }
}
