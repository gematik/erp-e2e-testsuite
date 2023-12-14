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
import de.gematik.test.erezept.fhir.resources.erp.ErxTaskBundle;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.r4.model.Resource;

public class TaskGetCommand extends BaseCommand<ErxTaskBundle> {

  /** Get all Tasks without any sorting or filtering */
  public TaskGetCommand() {
    super(ErxTaskBundle.class, HttpRequestMethod.GET, "Task");
  }

  /**
   * Get Tasks with QueryParameters for sorting and/or filtering
   *
   * @param searchParameters
   */
  public TaskGetCommand(List<IQueryParameter> searchParameters) {
    super(ErxTaskBundle.class, HttpRequestMethod.GET, "Task");
    queryParameters.addAll(searchParameters);
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
   * Get the FHIR-Resource for the Request-Body (of the inner-HTTP)
   *
   * @return an Optional.of(FHIR-Resource) for the Request-Body or an empty Optional if Request-Body
   *     is empty
   */
  @Override
  public Optional<Resource> getRequestBody() {
    return Optional.empty();
  }
}
