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
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.r4.erp.GemDispenseOperationParameters;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.r4.model.Resource;

/**
 * Legacy ERP command to dispense a prescription by sending a {@link ErxMedicationDispenseBundle} to
 * the FHIR endpoint without closing the task.
 *
 * <p>Behavior:
 *
 * <ul>
 *   <li>Performs a POST to {@code Task/{taskId}/$dispense} with the required {@code secret} query
 *       parameter.
 *   <li>If {@link GemDispenseOperationParameters} are provided, they are used as the request body
 *       (preferred for newer profiles).
 *   <li>Otherwise, builds a {@link ErxMedicationDispenseBundle} from the provided {@link
 *       ErxMedicationDispense} list.
 * </ul>
 *
 * <p>Deprecation:
 *
 * <ul>
 *   <li>Deprecated and planned for removal after 2026-03-26 (RU 1.21.0).
 *   <li>Use the newer command implementation that supports the updated dispense flow.
 * </ul>
 *
 * Inputs/Outputs:
 *
 * <ul>
 *   <li>Input: {@link TaskId}, {@link Secret}, either {@link GemDispenseOperationParameters} or a
 *       list of {@link ErxMedicationDispense}.
 *   <li>Output: {@link ErxMedicationDispenseBundle} as the expected response type.
 * </ul>
 *
 * Edge cases:
 *
 * <ul>
 *   <li>Null {@code dispenseParameters} falls back to building a bundle from {@code
 *       medicationDispenses}.
 *   <li>Empty {@code medicationDispenses} should be avoided unless server-side generation via
 *       parameters is intended.
 * </ul>
 *
 * @deprecated since 1.21.0; use the updated dispense command for current workflows.
 */
@Deprecated(since = "Fachdienst 1.21.0")
public class DispensePrescriptionAsBundleCommandOld
    extends BaseCommand<ErxMedicationDispenseBundle> {
  private List<ErxMedicationDispense> medicationDispenses;
  private GemDispenseOperationParameters dispenseParameters;

  /**
   * Create a legacy command for a single {@link ErxMedicationDispense}.
   *
   * @param taskId ERP task identifier
   * @param secret secret from Task/$accept
   * @param medicationDispense single dispense resource
   */
  public DispensePrescriptionAsBundleCommandOld(
      TaskId taskId, Secret secret, ErxMedicationDispense medicationDispense) {
    this(taskId, secret, List.of(medicationDispense));
  }

  /**
   * Create a legacy command for a list of {@link ErxMedicationDispense} resources.
   *
   * @param taskId ERP task identifier
   * @param secret secret from Task/$accept
   * @param medicationDispenses list of dispense resources
   */
  public DispensePrescriptionAsBundleCommandOld(
      TaskId taskId, Secret secret, List<ErxMedicationDispense> medicationDispenses) {
    super(ErxMedicationDispenseBundle.class, HttpRequestMethod.POST, "Task", taskId.getValue());
    this.medicationDispenses = medicationDispenses;
    queryParameters.add(new QueryParameter("secret", secret.getValue()));
  }

  /**
   * Create a legacy command using server-side {@link GemDispenseOperationParameters}.
   *
   * @param taskId ERP task identifier
   * @param secret secret from Task/$accept
   * @param dispenseParameters parameters to instruct server-side generation
   */
  public DispensePrescriptionAsBundleCommandOld(
      TaskId taskId, Secret secret, GemDispenseOperationParameters dispenseParameters) {
    super(ErxMedicationDispenseBundle.class, HttpRequestMethod.POST, "Task", taskId.getValue());

    this.dispenseParameters = dispenseParameters;
    queryParameters.add(new QueryParameter("secret", secret.getValue()));
  }

  /**
   * Returns the target endpoint including the {@code $dispense} operation and encoded query
   * parameters.
   */
  @Override
  public String getRequestLocator() {
    return this.getResourcePath() + "/$dispense" + this.encodeQueryParameters();
  }

  /**
   * Selects the request body: {@link GemDispenseOperationParameters} if present (preferred),
   * otherwise builds an {@link ErxMedicationDispenseBundle} from provided resources.
   */
  @Override
  public Optional<Resource> getRequestBody() {
    // for newer profiles the closeParameters are used and preferred here
    if (dispenseParameters != null) {
      return Optional.of(dispenseParameters);
    }
    return Optional.of(ErxMedicationDispenseBundleBuilder.of(medicationDispenses).build());
  }
}
