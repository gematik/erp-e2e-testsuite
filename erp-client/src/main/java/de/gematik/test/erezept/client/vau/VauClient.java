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

package de.gematik.test.erezept.client.vau;

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.client.*;
import de.gematik.test.erezept.client.vau.protocol.*;
import java.io.*;
import java.nio.charset.*;
import java.security.*;
import java.security.cert.*;
import java.security.interfaces.*;
import java.util.*;
import javax.annotation.*;
import javax.crypto.*;
import javax.net.ssl.*;
import kong.unirest.*;
import lombok.*;
import lombok.extern.slf4j.*;

@Slf4j
public class VauClient {

  private VauProtocol vauProtocol;
  private X509Certificate vauCertificate;

  private final String fdBaseUrl;
  private final String xApiKey;
  private final String userAgent;
  private final ClientType clientType;

  /**
   * UserPseudonym which will be used for VAU-Sessions. Initially each VAU-Session starts with
   * UserPseudonym equals to 0
   */
  private String vauUserPseudonym = "0";

  public VauClient(
      @NonNull String fdBaseUrl,
      @NonNull ClientType clientType,
      @NonNull X509Certificate vauCertificate,
      @Nullable String xApiKey,
      @Nullable String userAgent) {
    this.fdBaseUrl = fdBaseUrl;
    this.vauCertificate = vauCertificate;
    this.xApiKey = xApiKey;
    this.userAgent = userAgent;
    this.clientType = clientType;
  }

  @SneakyThrows
  public VauClient initialize() {
    // init SSL Context
    val vauTrustManager = new VauTrustManager();
    val sslCtx = SSLContext.getInstance("TLS");
    sslCtx.init(null, new TrustManager[] {vauTrustManager}, new SecureRandom());

    Unirest.primaryInstance()
        .config()
        .verifySsl(false)
        .hostnameVerifier(new VauHostVerifier())
        .sslContext(sslCtx);

    vauProtocol = new VauProtocol(VauVersion.V1, (ECPublicKey) vauCertificate.getPublicKey());
    return this;
  }

  public Response send(
      @NonNull String innerHttpRequest, @NonNull String accessToken, String erpResource) {
    Objects.requireNonNull(vauProtocol);

    val req =
        Unirest.post(getVauRequestUrl())
            // VAU Header
            .header("Content-Type", "application/octet-stream")
            .header(VauHeader.X_ERP_USER.getValue(), this.getErpUserHeaderValue())
            .body(
                vauProtocol.encryptRawVauRequest(
                    accessToken, innerHttpRequest.getBytes(StandardCharsets.UTF_8)));
    if (erpResource != null) {
      req.header(VauHeader.X_ERP_RESOURCE.getValue(), erpResource.replaceFirst("/", ""));
    } else {
      log.warn("parameter resource isn't set");
    }
    if (userAgent != null) {
      req.header(VauHeader.X_ERP_USER_AGENT.getValue(), userAgent);
      log.info(format("Set Header Parameter 'User-Agent' to: {0}", userAgent));
    }
    if (clientType == ClientType.FDV) {
      req.header(VauHeader.X_API_KEY.getValue(), xApiKey);
      log.info(format("Set Header Parameter 'X-api-key' to: {0}", xApiKey));
    }

    log.info(
        format(
            "Sending VAU-Request to: {0} with Request ID {1}",
            getVauRequestUrl(), Base64.getEncoder().encodeToString(vauProtocol.getRequestId())));
    val outerResponse = req.asBytes();
    return createResponse(outerResponse);
  }

  private Response createResponse(HttpResponse<byte[]> outerResponse) {
    // store the userpseudonym for next request
    if (outerResponse.getHeaders().containsKey("Userpseudonym")) {
      vauUserPseudonym = outerResponse.getHeaders().getFirst("Userpseudonym");
    } else {
      // reset
      vauUserPseudonym = "0";
    }
    val responseId = outerResponse.getHeaders().getFirst("X-Request-Id");
    // check if the response is octet-stream (encrypted) before decrypting
    val contentType = outerResponse.getHeaders().getFirst("content-type");
    val isOctetStream = Objects.requireNonNull(contentType).contains("octet-stream");

    log.info(
        format(
            "Received VAU-Response with Status Code {0} for Request ID {1} (X-Request-Id {2}) with"
                + " VAU Userpseudonym: {3}",
            outerResponse.getStatus(),
            Base64.getEncoder().encodeToString(vauProtocol.getRequestId()),
            responseId,
            vauUserPseudonym));

    if (isOctetStream) {
      try {
        val decrypted = vauProtocol.decryptRawVauResponse(outerResponse.getBody());
        return InnerHttp.decode(decrypted);
      } catch (BadPaddingException e) {
        val innerHttp = outerResponse.getBody();
        val b64Body = Base64.getEncoder().encodeToString(innerHttp);
        log.error(
            format(
                "Error while decoding VAU inner-HTTP of length {0}\n{1}",
                innerHttp.length, b64Body));
        throw new VauException("Error while decoding VAU", e);
      }
    } else {
      // response seems to be unencrypted, return simply the plain response
      val header = new HashMap<String, String>();
      // problematic if a header key has multiple values
      outerResponse.getHeaders().all().forEach(x -> header.put(x.getName(), x.getValue()));
      return new Response(
          InnerHttp.HTTP_V,
          outerResponse.getStatus(),
          header,
          new String(outerResponse.getBody(), StandardCharsets.UTF_8));
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
