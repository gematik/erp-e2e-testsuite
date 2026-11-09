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
import de.gematik.bbriccs.rest.plugins.HttpBRequestCustomizer;
import de.gematik.test.erezept.client.rest.ValidationResultHelper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
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
  private final FhirCodec fhir;
  private final FhirBResponseCreator responseFactory;
  private final HttpBClient vauClient;

  @Builder.Default
  private final List<HttpBRequestCustomizer> requestCustomizers = new LinkedList<>();

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

    val reqBody = this.encode(command.getRequestBody(), sendMime.toFhirEncoding());
    this.validateRequestFhirContent(reqBody);

    val innerHttpRequest = createInnerHttpRequest(command, reqBody);
    val accessToken = innerHttpRequest.getBearerToken().orElse("");

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
      FhirBRequest<?, R> command, String body) {
    val headersMap = command.getHeaderParameters();
    headersMap.put("Accept-Charset", acceptCharset);
    headersMap.put("Accept", acceptMime.asString());
    headersMap.put("Content-Type", sendMime.asString());
    headersMap.put("Content-Length", String.valueOf(body.getBytes(StandardCharsets.UTF_8).length));

    val headers =
        headersMap.entrySet().stream()
            .map(es -> new HttpHeader(es.getKey(), es.getValue()))
            .collect(Collectors.toList()); // VAU-client will add its own headers as well

    val request =
        HttpBRequest.method(command.getMethod())
            .urlPath(command.getRequestLocator())
            .headers(headers)
            .withPayload(body);

    this.requestCustomizers.forEach(it -> it.customize(request));
    return request;
  }
}
