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

package de.gematik.test.erezept.client.rest;

import static java.text.MessageFormat.*;

import ca.uhn.fhir.validation.*;
import de.gematik.test.erezept.client.exceptions.*;
import java.time.Duration;
import java.util.*;
import javax.annotation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
public class ErpResponse<R extends Resource> {

  /** The HTTP-Status Code */
  @Getter private final int statusCode;

  @Getter private final Duration duration;
  @Getter private final String usedJwt;

  private final Map<String, String> headers;
  @Nullable private final Resource resource;
  @Getter private final Class<R> expectedType;
  private final ValidationResult validationResult;

  private ErpResponse(
      int statusCode,
      Duration duration,
      String usedJwt,
      Map<String, String> headers,
      ValidationResult validationResult,
      @Nullable Resource resource,
      Class<R> expectedType) {
    this.statusCode = statusCode;
    this.usedJwt = usedJwt;
    this.headers = headers;
    this.resource = resource;
    this.expectedType = expectedType;
    this.duration = duration;
    this.validationResult = validationResult;
  }

  public static <E extends Resource> ErpResponseBuilder<E> forPayload(
      @Nullable Resource resource, Class<E> expectType) {
    return new ErpResponseBuilder<>(resource, expectType);
  }

  public boolean isValidPayload() {
    return this.validationResult.isSuccessful();
  }

  /**
   * This will retrieve the FHIR Payload Resource as the Base-Class without any validation.
   *
   * @return the FHIR Payload as an untyped Resource
   */
  @Nullable
  public Resource getAsBaseResource() {
    return resource;
  }

  public OperationOutcome getAsOperationOutcome() {
    return getResourceAs(OperationOutcome.class);
  }

  public R getExpectedResource() {
    return getResourceAs(this.expectedType);
  }

  public Optional<R> getResourceOptional() {
    return getResourceOptional(this.expectedType);
  }

  /**
   * @param clazz
   * @return
   * @param <U>
   */
  @SuppressWarnings("unchecked")
  private <U extends Resource> U getResourceAs(Class<U> clazz) {
    return getResourceOptional(clazz)
        .orElseThrow(() -> new UnexpectedResponseResourceError(clazz, resource));
  }

  @SuppressWarnings("unchecked")
  public <U extends Resource> Optional<U> getResourceOptional(Class<U> clazz) {
    if (isResourceOfType(clazz)) {
      this.ensureValidationResult();
      return Optional.ofNullable((U) resource);
    } else {
      return Optional.empty();
    }
  }

  @Nullable
  public Class<? extends Resource> getResourceType() {
    Class<? extends Resource> ret = null;
    if (resource != null) {
      ret = resource.getClass();
    }
    return ret;
  }

  public boolean isResourceOfType(Class<? extends Resource> clazz) {
    if (clazz.equals(Resource.class)) {
      return true;
    } else {
      return clazz.equals(getResourceType());
    }
  }

  public boolean isOfExpectedType() {
    return isResourceOfType(this.expectedType);
  }

  public boolean isOperationOutcome() {
    return OperationOutcome.class.equals(this.getResourceType());
  }

  public boolean isEmptyBody() {
    return this.getContentLength() == 0;
  }

  public String getHeaderValue(String key) {
    return this.headers.getOrDefault(key.toLowerCase(), "");
  }

  public MediaType getContentType() {
    return MediaType.fromString(this.headers.get("content-type"));
  }

  public long getContentLength() {
    val contentLengthHeader = this.headers.get("content-length");
    long contentLength = 0;
    if (contentLengthHeader != null && !contentLengthHeader.equals("")) {
      contentLength = Long.parseLong(contentLengthHeader);
    }

    return contentLength;
  }

  public boolean isJson() {
    return this.getContentType() == MediaType.FHIR_JSON;
  }

  public boolean isXML() {
    return this.getContentType() == MediaType.FHIR_XML;
  }

  private void ensureValidationResult() {
    ValidationResultHelper.throwOnInvalidValidationResult(
        this.getResourceType(), this.validationResult);
  }

  @Override
  public String toString() {
    val resourceType =
        this.getResourceType() != null ? this.getResourceType().getSimpleName() : "NULL";
    return format(
        "ErpResponse(rc={0}, payloadType={1}, duration={2})",
        this.getStatusCode(), resourceType, duration.toMillis());
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class ErpResponseBuilder<E extends Resource> {
    @Nullable private final Resource resource;
    private final Class<E> expectType;
    private int statusCode;
    private String usedJwt;
    private Duration duration = Duration.ZERO;
    private Map<String, String> headers;

    public ErpResponseBuilder<E> withStatusCode(int statusCode) {
      this.statusCode = statusCode;
      return this;
    }

    public ErpResponseBuilder<E> withDuration(Duration duration) {
      this.duration = duration;
      return this;
    }

    public ErpResponseBuilder<E> usedJwt(String jwt) {
      this.usedJwt = jwt;
      return this;
    }

    public ErpResponseBuilder<E> withHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }

    public ErpResponse<E> andValidationResult(ValidationResult vr) {
      return new ErpResponse<>(
          statusCode, duration, usedJwt, fixHeaders(headers), vr, resource, expectType);
    }

    /**
     * headers are case-insensitive https://tools.ietf.org/html/rfc2616#section-4.2 transform all
     * header parameters to lower case and store in a Map for later use
     *
     * @param headers originally given Headers-Map
     * @return fixed Headers-Map with lower-cased keys
     */
    private Map<String, String> fixHeaders(Map<String, String> headers) {
      val fixedMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
      fixedMap.putAll(headers);
      return fixedMap;
    }
  }
}
