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
import de.gematik.test.erezept.client.rest.param.QueryParameter;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.Optional;
import javax.annotation.Nullable;
import org.hl7.fhir.r4.model.Resource;

public class TaskGetByIdCommand extends BaseCommand<ErxPrescriptionBundle> {

  public TaskGetByIdCommand(TaskId taskId) {
    this(taskId, null, null);
  }

  public TaskGetByIdCommand(TaskId taskId, AccessCode accessCode) {
    this(taskId, accessCode, null);
  }

  public TaskGetByIdCommand(TaskId taskId, Secret secret) {
    this(taskId, null, secret);
  }

  private TaskGetByIdCommand(
      TaskId taskId, @Nullable AccessCode accessCode, @Nullable Secret secret) {
    super(ErxPrescriptionBundle.class, HttpRequestMethod.GET, "Task", taskId.getValue());

    if (accessCode != null) {
      this.headerParameters.put("X-AccessCode", accessCode.getValue());
    }

    if (secret != null) {
      queryParameters.add(new QueryParameter("secret", secret.getValue()));
    }
  }

  /**
   * Get the FHIR-Resource for the Request-Body (of the inner-HTTP)
   *
   * @return FHIR-Resource for the Request-Body
   */
  @Override
  public Optional<Resource> getRequestBody() {
    return Optional.empty();
  }
}
