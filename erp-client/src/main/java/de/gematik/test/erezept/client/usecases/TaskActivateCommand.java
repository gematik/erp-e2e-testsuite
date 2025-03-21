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
 */

package de.gematik.test.erezept.client.usecases;

import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.test.erezept.fhir.builder.erp.PrescriptionBuilder;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.Optional;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Resource;

public class TaskActivateCommand extends BaseCommand<ErxTask> {

  private final AccessCode accessCode;
  private final Parameters parameters;

  public TaskActivateCommand(TaskId taskId, AccessCode accessCode, byte[] signedBundle) {
    this(taskId, accessCode, PrescriptionBuilder.builder().build(signedBundle));
  }

  public TaskActivateCommand(TaskId taskId, AccessCode accessCode, Parameters parameters) {
    super(ErxTask.class, HttpRequestMethod.POST, "Task", taskId.getValue());
    this.accessCode = accessCode;
    this.parameters = parameters;

    this.headerParameters.put("X-AccessCode", this.accessCode.getValue());
  }

  /**
   * This method returns the last (tailing) part of the URL of the inner-HTTP Request e.g.
   * /Task/[id] or /Communication?[queryParameter]
   *
   * @return the tailing part of the URL which combines to full URL like [baseUrl][tailing Part]
   */
  @Override
  public String getRequestLocator() {
    return this.getResourcePath() + "/$activate";
  }

  /**
   * Get the FHIR-Resource for the Request-Body (of the inner-HTTP)
   *
   * @return FHIR-Resource for the Request-Body
   */
  @Override
  public Optional<Resource> getRequestBody() {
    return Optional.of(parameters);
  }
}
