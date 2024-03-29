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

package de.gematik.test.erezept.client;

import static java.text.MessageFormat.format;

import de.gematik.idp.client.IdpClient;
import de.gematik.idp.client.IdpClientRuntimeException;
import de.gematik.idp.client.IdpTokenResult;
import de.gematik.idp.crypto.model.PkiIdentity;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.rest.ErpResponseFactory;
import de.gematik.test.erezept.client.rest.MediaType;
import de.gematik.test.erezept.client.rest.ValidationResultHelper;
import de.gematik.test.erezept.client.usecases.ICommand;
import de.gematik.test.erezept.client.vau.InnerHttp;
import de.gematik.test.erezept.client.vau.VauClient;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.smartcard.Smartcard;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@Getter
@Builder
public class ErpClient {

  private final ClientType clientType;

  // configuration
  private final String baseFdUrl;
  private final String acceptCharset;
  private final MediaType acceptMime;
  private final MediaType sendMime;
  private final boolean validateRequest;

  // client capabilities
  private final IdpClient idpClient;
  private final FhirParser fhir;
  private final ErpResponseFactory responseFactory;
  private final VauClient vauClient;

  // client state
  private IdpTokenResult idpToken;
  private Instant idpTokenUpdated; // point in time when the IDP token was updated
  private Supplier<IdpTokenResult> authentication;

  /**
   * Initializes the ERP-Client to use vau client and idp client. Beforehand, the authentication
   * method via connector or via smartcard must be selected.
   */
  public void initialize() {
    Objects.requireNonNull(authentication);
    vauClient.initialize();
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
    if (idpTokenExpired()) {
      log.info("Refresh the IDP Token");
      try {
        idpToken = authentication.get();
        idpTokenUpdated = Instant.now();
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

  private boolean idpTokenExpired() {
    boolean ret;
    if (idpToken == null) {
      ret = true; // actually not expired but hasn't been fetched yet
    } else {
      val now = Instant.now();
      val diff = Duration.between(idpTokenUpdated, now).getSeconds();
      ret = diff >= idpToken.getExpiresIn();
    }
    return ret;
  }

  // TODO: does not belong here, right? // NOSONAR still needs to be refactored
  public String encode(Resource resource, EncodingType encoding) {
    return this.encode(resource, encoding, false);
  }

  public String encode(Resource resource, EncodingType encoding, boolean prettyPrint) {
    val encoded = fhir.encode(resource, encoding, prettyPrint);
    log.trace("Encoded Resource:\n" + encoded);
    return encoded;
  }

  @SneakyThrows
  public <R extends Resource> ErpResponse<R> request(ICommand<R> command) {
    this.refreshIdpToken(); // make sure before each request that the IDP token is not outdated
    // Request-Body is optional: encode as FHIR if available, otherwise keep empty body
    val bodyBuilder = new StringBuilder();
    command
        .getRequestBody()
        .ifPresent(b -> bodyBuilder.append(fhir.encode(b, sendMime.toFhirEncoding())));

    val reqBody = bodyBuilder.toString();
    this.validateRequestFhirContent(reqBody, validateRequest);

    val accessToken = idpToken.getAccessToken().getRawString();

    val innerHttpRequest = createInnerHttpRequest(command, accessToken, reqBody);

    val start = Instant.now();
    val response = vauClient.send(innerHttpRequest, accessToken, command.getFhirResource());
    val duration = Duration.between(start, Instant.now());
    log.info(format("Request against {0} took {1} msec", baseFdUrl, duration.toMillis()));

    return responseFactory.createFrom(
        response.getStatusCode(),
        duration,
        response.getHeader(),
        accessToken,
        response.getBody(),
        command.expectedResponseBody());
  }

  private void validateRequestFhirContent(String content, boolean shouldValidate) {
    if (shouldValidate && content != null && !content.isEmpty()) {
      log.info(format("Validate FHIR Content with length {0}", content.length()));
      val vr = fhir.validate(content);
      ValidationResultHelper.throwOnInvalidValidationResult(vr);
    }
  }

  private <R extends Resource> String createInnerHttpRequest(
      @NonNull ICommand<R> command, @NonNull String accessToken, String body) {
    val headers = command.getHeaderParameters();
    headers.put("Accept-Charset", acceptCharset);
    headers.put("Authorization", "Bearer " + accessToken);
    headers.put("Accept", acceptMime.asString());
    headers.put("Content-Type", sendMime.asString());

    return InnerHttp.encode(command.getMethod(), command.getRequestLocator(), headers, body);
  }
}
