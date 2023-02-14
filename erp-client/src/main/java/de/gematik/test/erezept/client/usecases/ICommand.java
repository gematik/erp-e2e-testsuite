/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.client.usecases;

import de.gematik.test.erezept.client.rest.HttpRequestMethod;
import java.util.Map;
import java.util.Optional;
import org.hl7.fhir.r4.model.Resource;

public interface ICommand<R extends Resource> {

  /**
   * This is required (mostly for VAU) to define on which resource the request shall be executed.
   * e.g. [baseUrl]/[resourcePath]
   *
   * @return the FHIR Resource
   */
  String getFhirResource();

  /**
   * This method returns the last (tailing) part of the URL of the inner-HTTP Request e.g.
   * /Task/[id] or /Communication?[queryParameter]
   *
   * @return the tailing part of the URL which combines to full URL like [baseUrl][tailing Part]
   */
  String getRequestLocator();

  String getFullUrl(String baseUrl);

  /**
   * Defines which HTTP-Method (for the inner-HTTP Request) shall be used.
   *
   * @return the HTTP-Method of the Command
   */
  HttpRequestMethod getMethod();

  /**
   * Get a Map of required Header-Parameters for this specific command. By Default an empty map is
   * returned which indicates no Header-Parameters
   *
   * @return map of Header-Parameters
   */
  Map<String, String> getHeaderParameters();

  /**
   * Get the FHIR-Resource for the Request-Body (of the inner-HTTP)
   *
   * @return an Optional.of(FHIR-Resource) for the Request-Body or an empty Optional if Request-Body
   *     is empty
   */
  Optional<Resource> getRequestBody();

  Class<R> expectedResponseBody();
}
