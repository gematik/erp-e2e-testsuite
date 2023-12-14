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

package de.gematik.test.erezept.client.usecases;

import de.gematik.test.erezept.client.rest.HttpRequestMethod;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hl7.fhir.r4.model.Resource;

/**
 * @param <R> the type of the expected Response Body
 */
public abstract class BaseCommand<R extends Resource> implements ICommand<R> {

  private final Class<R> expectedResponse;
  private final HttpRequestMethod httpRequestMethod;
  private final String fhirResource;
  private final String resourceId;

  protected final List<IQueryParameter> queryParameters;
  protected final Map<String, String> headerParameters;

  protected BaseCommand(Class<R> expect, HttpRequestMethod method, String fhirResource) {
    this(expect, method, fhirResource, null);
  }

  protected BaseCommand(
      Class<R> expect, HttpRequestMethod method, String fhirResource, String resourceId) {
    this.expectedResponse = expect;
    this.httpRequestMethod = method;
    this.fhirResource = fhirResource;
    this.resourceId = resourceId;
    this.queryParameters = new ArrayList<>();
    this.headerParameters = new HashMap<>();
  }

  /**
   * This is required (mostly for VAU) to define on which resource the request shall be executed.
   * e.g. [baseUrl]/[resourcePath]
   *
   * @return the FHIR Resource
   */
  public final String getFhirResource() {
    return fhirResource.startsWith("/") ? fhirResource : "/" + fhirResource;
  }

  /**
   * This method provides the full Path to a Resource for a Request. e.g. /Task if Task was provided
   * without an ID and /Task/[id] if an ID was provided
   *
   * @return the Path
   */
  protected final String getResourcePath() {
    String ret = this.getFhirResource();
    if (resourceId != null) {
      ret += "/" + resourceId;
    }
    return ret;
  }

  public final String getFullUrl(String baseUrl) {
    return baseUrl + this.getRequestLocator();
  }

  /**
   * This method returns the last (tailing) part of the URL of the inner-HTTP Request e.g.
   * /Task/[id] or /Communication?[queryParameter]
   *
   * @return the tailing part of the URL which combines to full URL like [baseUrl][tailing Part]
   */
  @Override
  public String getRequestLocator() {
    return this.getResourcePath() + this.encodeQueryParameters();
  }

  /**
   * Defines which HTTP-Method (for the inner-HTTP Request) shall be used.
   *
   * @return the HTTP-Method of the Command
   */
  public final HttpRequestMethod getMethod() {
    return httpRequestMethod;
  }

  /**
   * Get a Map of required Header-Parameters for this specific command. By Default an empty map is
   * returned which indicates no Header-Parameters
   *
   * @return map of Header-Parameters
   */
  public final Map<String, String> getHeaderParameters() {
    return headerParameters;
  }

  /**
   * What type of Response-Body does this command expect. This methode is required for the
   * FHIR-Parser to decode the Response-Body (of the inner-HTTP) to a FHIR-Resource
   *
   * @return the Type of the expected Response-Body
   */
  public final Class<R> expectedResponseBody() {
    return expectedResponse;
  }

  protected String encodeQueryParameters() {
    StringBuilder ret = new StringBuilder();
    if (!this.queryParameters.isEmpty()) {
      ret.append("?");
      this.queryParameters.forEach(param -> ret.append(param.encode()).append("&"));

      ret.deleteCharAt(ret.length() - 1); // remove the last &
    }

    return ret.toString();
  }
}
