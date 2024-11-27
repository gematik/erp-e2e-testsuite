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

package de.gematik.test.erezept.client.vau;

import de.gematik.bbriccs.rest.HttpBRequest;
import de.gematik.bbriccs.rest.HttpBResponse;
import de.gematik.bbriccs.rest.HttpVersion;
import de.gematik.bbriccs.rest.RawHttpCodec;
import de.gematik.bbriccs.rest.headers.AuthHttpHeaderKey;
import de.gematik.bbriccs.rest.headers.HttpHeader;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.UnirestRetryWrapper;
import de.gematik.test.erezept.client.vau.protocol.VauProtocol;
import de.gematik.test.erezept.client.vau.protocol.VauVersion;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestInstance;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class VauClient {

  private final RawHttpCodec httpCodec = RawHttpCodec.defaultCodec();
  private final VauProtocol vauProtocol;
  private final String fdBaseUrl;
  private final String xApiKey;
  private final String userAgent;
  private final ClientType clientType;
  private final UnirestInstance unirest;
  private final UnirestRetryWrapper retryWrapper = new UnirestRetryWrapper();

  /**
   * UserPseudonym which will be used for VAU-Sessions. Initially each VAU-Session starts with
   * UserPseudonym equals to 0
   */
  private String vauUserPseudonym = "0";

  public VauClient(
      String fdBaseUrl,
      ClientType clientType,
      X509Certificate vauCertificate,
      @Nullable String xApiKey,
      @Nullable String userAgent) {
    this.fdBaseUrl = fdBaseUrl;
    this.vauProtocol = new VauProtocol(VauVersion.V1, (ECPublicKey) vauCertificate.getPublicKey());
    this.xApiKey = xApiKey;
    this.userAgent = userAgent;
    this.clientType = clientType;
    this.unirest = Unirest.spawnInstance();
  }

  @SneakyThrows
  public VauClient initialize() {
    // init SSL Context
    val vauTrustManager = new VauTrustManager();
    val sslCtx = SSLContext.getInstance("TLS");
    sslCtx.init(null, new TrustManager[] {vauTrustManager}, new SecureRandom());

    this.unirest
        .config()
        .verifySsl(false)
        .useSystemProperties(true)
        .retryAfter(true)
        .connectTimeout(UnirestRetryWrapper.CONNECT_TIMEOUT * 1000)
        .sslContext(sslCtx);
    return this;
  }

  public HttpBResponse send(HttpBRequest innerHttpRequest, String accessToken, String erpResource) {
    Objects.requireNonNull(
        this.unirest, "VauClient is not initialized: missing call to initialize()?");

    val encodedRequest = httpCodec.encode(innerHttpRequest);
    val reqBody =
        vauProtocol.encryptRawVauRequest(
            accessToken, encodedRequest.getBytes(StandardCharsets.UTF_8));
    log.info(
        "Sending VAU-Request as {} to: {} with Request ID {}",
        clientType.toString(),
        getVauRequestUrl(),
        Base64.getEncoder().encodeToString(vauProtocol.getRequestId()));
    log.trace("\n------- inner VAU-Request -------\n{}\n-------", encodedRequest);

    val req = this.unirest.post(getVauRequestUrl()).body(reqBody);

    // additional/conditional outer VAU-Request Headers
    StandardHttpHeaderKey.CONTENT_TYPE.apply("application/octet-stream", req::header);
    StandardHttpHeaderKey.USER_AGENT.apply(this.userAgent, req::header);
    // erpResource is optional? check if that is still true
    Optional.ofNullable(erpResource)
        .map(rh -> rh.replaceFirst("/", ""))
        .ifPresent(rh -> VauHeader.X_ERP_RESOURCE.apply(rh, req::header));
    VauHeader.X_ERP_USER.apply(this.getErpUserHeaderValue(), req::header);
    // API-Key is optional because only required/used by FdVs
    Optional.ofNullable(xApiKey)
        .ifPresent(ak -> AuthHttpHeaderKey.X_API_KEY.apply(ak, req::header));

    val start = System.currentTimeMillis();
    try {
      val outerResponse = this.retryWrapper.requestWithRetries(req);
      return decodeResponse(outerResponse);
    } finally {
      val duration = System.currentTimeMillis() - start;
      log.info("VAU-Request took {}ms", duration);
    }
  }

  private HttpBResponse decodeResponse(HttpResponse<byte[]> outerResponse) {
    // store the userpseudonym for next request
    this.vauUserPseudonym =
        outerResponse.getHeaders().get("Userpseudonym").stream().findFirst().orElse("0");

    val responseId = outerResponse.getHeaders().getFirst("X-Request-Id");
    // check if the response is octet-stream (encrypted) before decrypting
    val contentType = outerResponse.getHeaders().getFirst("content-type");
    val isOctetStream = Objects.requireNonNull(contentType).contains("octet-stream");
    val xRequestId = Base64.getEncoder().encodeToString(vauProtocol.getRequestId());

    log.info(
        "Received VAU-Response with Status Code {} for Request ID {} (X-Request-Id {}) with"
            + " VAU Userpseudonym: {}",
        outerResponse.getStatus(),
        xRequestId,
        responseId,
        vauUserPseudonym);

    if (isOctetStream) {
      try {
        val decrypted = vauProtocol.decryptRawVauResponse(outerResponse.getBody());
        log.trace(
            "\n------- inner VAU-Response -------\n{}\n-------",
            new String(decrypted, StandardCharsets.UTF_8));
        return httpCodec.decodeResponse(decrypted);
      } catch (BadPaddingException e) {
        val innerHttp = outerResponse.getBody();
        val b64Body = Base64.getEncoder().encodeToString(innerHttp);
        log.error(
            "Error while decoding VAU inner-HTTP of length {}\n{}", innerHttp.length, b64Body);
        throw new VauException("Error while decoding VAU", e);
      }
    } else {
      // response seems to be unencrypted, return simply the plain response
      val headers =
          outerResponse.getHeaders().all().stream()
              .map(h -> new HttpHeader(h.getName(), h.getValue()))
              .toList();
      val response =
          new HttpBResponse(
              HttpVersion.HTTP_1_1, outerResponse.getStatus(), headers, outerResponse.getBody());
      log.trace(
          "\n------- missing inner VAU-Response -------\n{}\n-------", httpCodec.encode(response));
      return response;
    }
  }

  private String getVauRequestUrl() {
    return fdBaseUrl + "/VAU/" + vauUserPseudonym;
  }

  /**
   * @return "v" if communication from App otherwise "l"
   */
  private String getErpUserHeaderValue() {
    String ret = "l";
    if (clientType == ClientType.FDV) {
      ret = "v";
    }
    return ret;
  }
}
