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

package de.gematik.test.erezept.client.rest;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
public class ErpResponse {

  /** The HTTP-Status Code */
  @Getter private final int statusCode;

  private final Map<String, String> headers;
  private final Resource resource;

  public ErpResponse(int statusCode, Map<String, String> headers, Resource resource) {
    this.statusCode = statusCode;
    this.headers = fixHeaders(headers);
    this.resource = resource;
  }

  public Resource getResource() {
    return resource;
  }

  @SuppressWarnings("unchecked")
  public <U extends Resource> U getResource(Class<U> clazz) {
    if (isResourceOfType(clazz)) {
      return (U) resource;
    } else {
      throw new UnexpectedResponseResourceError(clazz, resource);
    }
  }

  @SuppressWarnings("unchecked")
  public <U extends Resource> Optional<U> getResourceOptional(Class<U> clazz) {
    if (isResourceOfType(clazz)) {
      return Optional.of((U) resource);
    } else {
      return Optional.empty();
    }
  }

  public boolean isResourceOfType(Class<? extends Resource> clazz) {
    if (clazz.equals(Resource.class)) {
      return true;
    } else {
      return clazz.equals(getResourceType());
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

  public boolean isOperationOutcome() {
    return OperationOutcome.class.equals(this.getResourceType());
  }

  public boolean isEmptyBody() {
    return this.getContentLength() == 0;
  }

  public String getHeaderValue(@NonNull String key) {
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

  /**
   * headers are case-insensitive https://tools.ietf.org/html/rfc2616#section-4.2 transform all
   * header parameters to lower case and store in a Map for later use
   *
   * @param headers originally given Headers-Map
   * @return fixed Headers-Map with lower-cased keys
   */
  private static Map<String, String> fixHeaders(Map<String, String> headers) {
    val fixedMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    fixedMap.putAll(headers);
    return fixedMap;
  }

  @Override
  public String toString() {
    val resourceType =
        this.getResourceType() != null ? this.getResourceType().getSimpleName() : "NULL";
    return format("ErpResponse(rc={0}, payloadType={1})", this.getStatusCode(), resourceType);
  }
}
