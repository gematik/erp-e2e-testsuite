/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.client;

import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.bbriccs.rest.HttpBClient;
import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.fd.FhirBRequest;
import de.gematik.bbriccs.rest.fd.FhirBResponse;
import de.gematik.bbriccs.rest.fd.FhirBResponseCreator;
import de.gematik.bbriccs.rest.fd.FhirClient;
import de.gematik.bbriccs.rest.fd.MediaType;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.smartcards.Smartcard;
import de.gematik.idp.client.IdpClient;
import de.gematik.idp.client.IdpClientRuntimeException;
import de.gematik.idp.client.IdpTokenResult;
import de.gematik.idp.crypto.model.PkiIdentity;
import de.gematik.test.erezept.client.rest.ValidationResultHelper;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@Getter
@Builder
public class ErpClient implements FhirClient {

  private final ClientType clientType;

  // configuration
  private final String baseFdUrl;
  private final String acceptCharset;
  private final MediaType acceptMime;
  private final MediaType sendMime;
  private final boolean validateRequest;

  // client capabilities
  private final IdpClient idpClient;
  private final FhirCodec fhir;
  private final FhirBResponseCreator responseFactory;
  private final HttpBClient vauClient;

  // client state
  private IdpTokenResult idpToken;
  private Supplier<IdpTokenResult> authentication;
  @Default private Instant idpTokenValidUntil = Instant.now();

  /**
   * Initializes the ERP-Client to use vau client and idp client. Beforehand, the authentication
   * method via connector or via smartcard must be selected.
   */
  public void initialize() {
    Objects.requireNonNull(authentication);
    vauClient.init();
    try {
      idpClient.initialize();
    } catch (NullPointerException npe) {
      // rewrap the NPE to an IdpClientRuntimeException will show tests as compromised instead of
      // broken!
      log.warn("Something went wrong during initialization of IDP-Client");
      throw new IdpClientRuntimeException("Caught NullPointer from IDP-Client", npe);
    }
  }

  /**
   * Authenticates the ERP-Client via smartcard. A konnektor isn't required.
   *
   * @param smartcard is the smartcard which will be used for authentication with the IDP
   */
  public void authenticateWith(final Smartcard smartcard) {
    authentication =
        () -> {
          val autCertificate = smartcard.getAutCertificate();
          val pki =
              PkiIdentity.builder()
                  .certificate(autCertificate.getX509Certificate())
                  .privateKey(autCertificate.getPrivateKey())
                  .build();
          return idpClient.login(pki);
        };
    this.initialize();
  }

  /**
   * Authenticates the ERP client via konnektor which can sign the idp server challenge.
   *
   * @param authPubCert is the C.AUT certificate of a smartcard, which can readout with the
   *     connector operation ReadCardCertificate
   * @param challenge is the IDP server challenge that have to be signed. The connector operation
   *     ExternalAuthenticate can be used for this.
   */
  public void authenticateWith(
      final X509Certificate authPubCert, final UnaryOperator<byte[]> challenge) {
    authentication = () -> idpClient.login(authPubCert, challenge);
    this.initialize();
  }

  public void refreshIdpToken() {
    if (needsIdpTokenRefresh()) {
      log.info("Refresh the IDP Token");
      try {
        idpToken = authentication.get();
        idpTokenValidUntil = Instant.now().plus(idpToken.getExpiresIn(), ChronoUnit.SECONDS);
      } catch (NullPointerException npe) {
        // rewrap the NPE to an IdpClientRuntimeException will show tests as compromised instead of
        // broken!
        log.warn("Something went wrong during authentication on IDP");
        throw new IdpClientRuntimeException("Caught NullPointer from IDP-Client", npe);
      }
    } else {
      log.info("IDP Token is still valid, no need to refresh");
    }
  }

  private boolean needsIdpTokenRefresh() {
    val refreshInstant = idpTokenValidUntil.minus(120, ChronoUnit.SECONDS);
    return Instant.now().isAfter(refreshInstant);
  }

  @Override
  public String encode(Resource resource) {
    return this.encode(resource, false);
  }

  @Override
  public String encode(Resource resource, boolean prettyPrint) {
    return this.encode(resource, this.acceptMime.toFhirEncoding(), prettyPrint);
  }

  public String encode(Resource resource, EncodingType encoding) {
    return this.encode(resource, encoding, false);
  }

  public String encode(Resource resource, EncodingType encoding, boolean prettyPrint) {
    return fhir.encode(resource, encoding, prettyPrint);
  }

  @Override
  public <R extends Resource> R decode(Class<R> type, String content) {
    return fhir.decode(type, content);
  }

  @Override
  public boolean isValid(String content) {
    return fhir.isValid(content);
  }

  @Override
  public ValidationResult validate(String content) {
    return fhir.validate(content);
  }

  @SneakyThrows
  @Override
  public <T extends Resource, R extends Resource> FhirBResponse<R> request(
      FhirBRequest<T, R> command) {
    this.refreshIdpToken(); // make sure before each request that the IDP token is not outdated

    val reqBody = this.encode(command.getRequestBody(), sendMime.toFhirEncoding());
    this.validateRequestFhirContent(reqBody);

    val accessToken = idpToken.getAccessToken().getRawString();

    val innerHttpRequest = createInnerHttpRequest(command, accessToken, reqBody);

    val start = Instant.now();
    val response = vauClient.send(innerHttpRequest);
    val duration = Duration.between(start, Instant.now());
    log.info("Request against {} took {} msec", baseFdUrl, duration.toMillis());

    return responseFactory
        .takeExpectationFrom(command)
        .usedAccessToken(accessToken)
        .received(response)
        .withDuration(duration);
  }

  private void validateRequestFhirContent(String content) {
    if (this.validateRequest) {
      val vr = fhir.validate(content);
      ValidationResultHelper.throwOnInvalidValidationResult(vr);
    }
  }

  private <R extends Resource> HttpBRequest createInnerHttpRequest(
      FhirBRequest<?, R> command, String accessToken, String body) {
    val headersMap = command.getHeaderParameters();
    headersMap.put("Accept-Charset", acceptCharset);
    headersMap.put("Authorization", "Bearer " + accessToken);
    headersMap.put("Accept", acceptMime.asString());
    headersMap.put("Content-Type", sendMime.asString());
    headersMap.put("Content-Length", String.valueOf(body.getBytes(StandardCharsets.UTF_8).length));

    val headers =
        headersMap.entrySet().stream()
            .map(es -> new HttpHeader(es.getKey(), es.getValue()))
            .collect(Collectors.toList()); // VAU-client will add its own headers as well

    return HttpBRequest.method(command.getMethod())
        .urlPath(command.getRequestLocator())
        .headers(headers)
        .withPayload(body);
  }
}
