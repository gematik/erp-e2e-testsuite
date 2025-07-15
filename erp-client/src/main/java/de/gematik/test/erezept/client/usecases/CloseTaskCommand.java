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
import de.gematik.test.erezept.client.rest.param.QueryParameter;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBundleBuilder;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.r4.erp.GemCloseOperationParameters;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.r4.model.Resource;

public class CloseTaskCommand extends BaseCommand<ErxReceipt> {

  private List<ErxMedicationDispense> medicationDispenses;
  private GemCloseOperationParameters closeParameters;

  /**
   * The Constructor is intended to close a Prescription without transmitting a MedicationDispense
   * it requires that a MedicationDispense has been submitted before, for example by using the
   * DispensePrescriptionAsBundleCommand
   *
   * @param taskId of the task to be closed
   * @param secret of the task to be closed
   */
  public CloseTaskCommand(TaskId taskId, Secret secret) {
    this(taskId, secret, List.of());
  }

  /**
   * The Constructor is intended to close a Prescription and transmitting a single
   * MedicationDispense the MedicationDispense will be sent as MedicationDispenseBundle with a
   * single entry DispensePrescriptionAsBundleCommand
   *
   * @param taskId of the task to be closed
   * @param secret of the task to be closed
   * @param medicationDispense to be used while closing the task
   */
  public CloseTaskCommand(TaskId taskId, Secret secret, ErxMedicationDispense medicationDispense) {
    this(taskId, secret, List.of(medicationDispense));
  }

  public CloseTaskCommand(
      TaskId taskId, Secret secret, List<ErxMedicationDispense> medicationDispenses) {
    super(ErxReceipt.class, HttpRequestMethod.POST, "Task", taskId.getValue());
    this.medicationDispenses = medicationDispenses;

    queryParameters.add(new QueryParameter("secret", secret.getValue()));
  }

  public CloseTaskCommand(
      TaskId taskId, Secret secret, GemCloseOperationParameters closeParameters) {
    super(ErxReceipt.class, HttpRequestMethod.POST, "Task", taskId.getValue());
    this.closeParameters = closeParameters;

    queryParameters.add(new QueryParameter("secret", secret.getValue()));
  }

  /**
   * This method returns the last (tailing) part of the URL of the inner-HTTP Request e.g.
   * /Task/[id] or /Communication?[queryParameter]
   *
   * @return the tailing part of the URL which combines to full URL like [baseUrl][tailing Part]
   */
  @Override
  public String getRequestLocator() {
    return this.getResourcePath() + "/$close" + this.encodeQueryParameters();
  }

  /**
   * Get the FHIR-Resource for the Request-Body (of the inner-HTTP)
   *
   * @return an Optional.of(FHIR-Resource) for the Request-Body or an empty Optional if Request-Body
   *     is empty
   */
  @Override
  public Optional<Resource> getRequestBody() {
    Optional<Resource> ret;

    // for newer profiles the closeParameters are used and preferred here
    if (closeParameters != null) {
      return Optional.of(closeParameters);
    }

    if (medicationDispenses.size() == 1) {
      ret = Optional.of(medicationDispenses.get(0));
    } else if (medicationDispenses.size() > 1) {
      ret = Optional.of(ErxMedicationDispenseBundleBuilder.of(medicationDispenses).build());
    } else {
      ret = Optional.empty();
    }
    return ret;
  }
}
