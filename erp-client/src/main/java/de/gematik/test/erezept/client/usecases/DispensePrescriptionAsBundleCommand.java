/*
 * Copyright 2024 gematik GmbH
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
import de.gematik.test.erezept.client.rest.param.QueryParameter;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBundleBuilder;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.r4.model.Resource;

public class DispensePrescriptionAsBundleCommand extends BaseCommand<ErxMedicationDispenseBundle> {
  private final List<ErxMedicationDispense> medicationDispenses;

  public DispensePrescriptionAsBundleCommand(
      TaskId taskId, Secret secret, ErxMedicationDispense medicationDispense) {
    this(taskId, secret, List.of(medicationDispense));
  }

  public DispensePrescriptionAsBundleCommand(
      TaskId taskId, Secret secret, List<ErxMedicationDispense> medicationDispenses) {
    super(ErxMedicationDispenseBundle.class, HttpRequestMethod.POST, "Task", taskId.getValue());
    this.medicationDispenses = medicationDispenses;
    queryParameters.add(new QueryParameter("secret", secret.getValue()));
  }

  @Override
  public String getRequestLocator() {
    return this.getResourcePath() + "/$dispense" + this.encodeQueryParameters();
  }

  @Override
  public Optional<Resource> getRequestBody() {
    return Optional.of(ErxMedicationDispenseBundleBuilder.of(medicationDispenses).build());
  }
}
