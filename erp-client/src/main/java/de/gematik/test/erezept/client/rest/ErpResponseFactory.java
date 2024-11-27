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

package de.gematik.test.erezept.client.rest;

import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.validation.ValidationResult;
import com.google.common.base.Strings;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
public class ErpResponseFactory {

  private final FhirParser parser;
  private final boolean validateResponse;

  public ErpResponseFactory(FhirParser parser, boolean validateResponse) {
    this.parser = parser;
    this.validateResponse = validateResponse;
  }

  public <R extends Resource> ErpResponse<R> createFrom(
      int status, Map<String, String> headers, String usedJwt, String content, Class<R> expect) {
    return createFrom(status, Duration.ZERO, headers, usedJwt, content, expect);
  }

  public <R extends Resource> ErpResponse<R> createFrom(
      int status,
      Duration duration,
      Map<String, String> headers,
      String usedJwt,
      String content,
      Class<R> expect) {

    if (status >= 500) {
      // log unhandled errors from backend for better analysis
      log.error("Server Error {}: {}", status, content);
    }

    val resource =
        Optional.ofNullable(content)
            .filter(c -> !Strings.isNullOrEmpty(content) && !content.isBlank())
            .map(c -> decode(c, expect))
            .orElse(null);
    return ErpResponse.forPayload(resource, expect)
        .withStatusCode(status)
        .withDuration(duration)
        .usedJwt(usedJwt)
        .withHeaders(headers)
        .andValidationResult(validateContent(content));
  }

  private Resource decode(String content, Class<? extends Resource> expect) {
    Resource ret;
    try {
      ret = parser.decode(expect, content);
    } catch (DataFormatException | IllegalArgumentException e) {
      // try to decode without an expected class (and let HAPI decide) as this case may occur:
      // 1. DataFormatException happens, if the Backend responds with an OperationOutcome (or any
      // other unexpected resource) while another resource was expected
      // 2. IllegalArgumentException is thrown if an empty response is expected, but we still get a
      // resource (probably an OperationOutcome)
      log.info(
          "Given content of length {} could not be decoded as {}, try without expectation",
          content.length(),
          expect.getSimpleName());
      ret = parser.decode(content);
    }
    return ret;
  }

  private ValidationResult validateContent(String content) {
    ValidationResult vr;
    if (!validateResponse || Strings.isNullOrEmpty(content) || content.isBlank()) {
      // simply create an empty validation results which will always be successful
      vr = new ValidationResult(this.parser.getCtx(), List.of());
    } else {
      vr = this.parser.validate(content);
      if (!vr.isSuccessful()) {
        log.error("FHIR Content is invalid\n{}", content);
      }
    }

    return vr;
  }
}
