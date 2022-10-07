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

package de.gematik.test.erezept.client.usecases;

import de.gematik.test.erezept.client.rest.HttpRequestMethod;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.Secret;
import java.util.Optional;
import org.hl7.fhir.r4.model.Resource;

public class TaskRejectCommand extends BaseCommand<Resource> {

  private final Secret secret;

  public TaskRejectCommand(String taskId, AccessCode accessCode, Secret secret) {
    super(Resource.class, HttpRequestMethod.POST, "Task", taskId);
    this.secret = secret;
    this.headerParameters.put("X-AccessCode", accessCode.getValue());
  }

  @Override
  public String getRequestLocator() {
    return this.getResourcePath() + "/$reject?secret=" + this.secret.getValue();
  }

  @Override
  public Optional<Resource> getRequestBody() {
    return Optional.empty();
  }
}
