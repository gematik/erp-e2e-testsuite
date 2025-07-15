/*
 * Copyright 2025 gematik GmbH
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

package de.gematik.test.erezept.client.usecases;

import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.Optional;
import org.hl7.fhir.r4.model.Resource;

public class TaskAbortCommand extends BaseCommand<Resource> {

  private final Secret secret;

  public TaskAbortCommand(TaskId taskId) {
    this(taskId, null);
  }

  public TaskAbortCommand(TaskId taskId, AccessCode accessCode) {
    this(taskId, accessCode, null);
  }

  public TaskAbortCommand(TaskId taskId, AccessCode accessCode, Secret secret) {
    super(Resource.class, HttpRequestMethod.POST, "Task", taskId.getValue());

    if (accessCode != null) {
      this.headerParameters.put("X-AccessCode", accessCode.getValue());
    }

    this.secret = secret;
  }

  /**
   * This method returns the last (tailing) part of the URL of the inner-HTTP Request e.g.
   * /Task/[id] or /Communication?[queryParameter]
   *
   * @return the tailing part of the URL which combines to full URL like [baseUrl][tailing Part]
   */
  @Override
  public String getRequestLocator() {
    String locator = this.getResourcePath() + "/$abort";
    if (secret != null) {
      locator += "?secret=" + secret.getValue();
    }
    return locator;
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
