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
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBundleBuilder;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.r4.erp.GemDispenseOperationParameters;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.r4.model.Resource;

public class DispensePrescriptionAsBundleCommandWithoutSecret
    extends BaseCommand<ErxMedicationDispenseBundle> {
  private List<ErxMedicationDispense> medicationDispenses;

  private GemDispenseOperationParameters closeParameters;

  public DispensePrescriptionAsBundleCommandWithoutSecret(
      TaskId taskId, ErxMedicationDispense medicationDispense) {
    this(taskId, List.of(medicationDispense));
  }

  public DispensePrescriptionAsBundleCommandWithoutSecret(
      TaskId taskId, List<ErxMedicationDispense> medicationDispenses) {
    super(ErxMedicationDispenseBundle.class, HttpRequestMethod.POST, "Task", taskId.getValue());
    this.medicationDispenses = medicationDispenses;
  }

  public DispensePrescriptionAsBundleCommandWithoutSecret(
      TaskId taskId, GemDispenseOperationParameters closeParameters) {
    super(ErxMedicationDispenseBundle.class, HttpRequestMethod.POST, "Task", taskId.getValue());
    this.closeParameters = closeParameters;
  }

  @Override
  public String getRequestLocator() {
    return this.getResourcePath() + "/$dispense" + this.encodeQueryParameters();
  }

  @Override
  public Optional<Resource> getRequestBody() {

    // for newer profiles the closeParameters are used and preferred here
    if (closeParameters != null) {
      return Optional.of(closeParameters);
    }

    return Optional.of(ErxMedicationDispenseBundleBuilder.of(medicationDispenses).build());
  }
}
