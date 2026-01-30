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

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.test.erezept.client.rest.param.QueryParameter;
import de.gematik.test.erezept.fhir.r4.erp.GemDispenseOperationParameters;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.Optional;
import org.hl7.fhir.r4.model.Resource;

public class DispensePrescriptionCommandNew extends BaseCommand<EmptyResource> {

  private GemDispenseOperationParameters dispenseParameters;

  public DispensePrescriptionCommandNew(
      TaskId taskId, Secret secret, GemDispenseOperationParameters dispenseParameters) {
    super(EmptyResource.class, HttpRequestMethod.POST, "Task", taskId.getValue());
    this.dispenseParameters = dispenseParameters;
    queryParameters.add(new QueryParameter("secret", secret.getValue()));
  }

  @Override
  public String getRequestLocator() {
    return this.getResourcePath() + "/$dispense" + this.encodeQueryParameters();
  }

  @Override
  public Optional<Resource> getRequestBody() {
    if (dispenseParameters != null) {
      return Optional.of(dispenseParameters);
    }
    return Optional.empty();
  }
}
