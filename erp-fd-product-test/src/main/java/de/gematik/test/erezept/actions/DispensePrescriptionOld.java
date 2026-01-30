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

package de.gematik.test.erezept.actions;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.client.usecases.DispensePrescriptionAsBundleCommandOld;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.r4.erp.GemDispenseOperationParameters;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;

/**
 * Legacy dispense action for ERP workflows that sends a MedicationDispense bundle without closing
 * the task. This action wraps {@link DispensePrescriptionAsBundleCommandOld} and returns the
 * resulting {@link ErxMedicationDispenseBundle} as an {@link ErpInteraction} for further
 * verification in Screenplay.
 *
 * <p>Scope and behavior:
 *
 * <ul>
 *   <li>Builds and executes a dispense command against the ERP FHIR endpoint
 *   <li>Accepts either a prepared list of {@link ErxMedicationDispense} resources or a {@link
 *       GemDispenseOperationParameters} instance
 *   <li>Does not perform task/$close; use the newer API for the current workflow
 * </ul>
 *
 * <p>Deprecation:
 *
 * <ul>
 *   <li>This class is deprecated and planned for removal after 2026-03-26 (RU 1.21.0).
 *   <li>Use {@link DispensePrescriptionNew} instead, which implements the updated dispense flow
 *       using {@code DispensePrescriptionCommandNew} and returns an {@code EmptyResource} body.
 * </ul>
 *
 * @deprecated since 1.21.0; use {@link DispensePrescriptionNew} for the current dispense workflow.
 * @see de.gematik.test.erezept.fhir.builder.kbv.KbvPatientBuilder for general Javadoc style and
 *     deprecation pattern
 */
@Deprecated(since = "Fachdienst 1.21.0")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DispensePrescriptionOld extends ErpAction<ErxMedicationDispenseBundle> {

  private final DispensePrescriptionAsBundleCommandOld dispensePrescriptionAsBundleCommand;

  public static Builder withCredentials(TaskId taskId, Secret secret) {
    return new Builder(taskId, secret);
  }

  /**
   * Executes the configured legacy dispense command as the given Screenplay {@link Actor}.
   *
   * <p>The returned {@link ErpInteraction} contains the {@link ErxMedicationDispenseBundle} that
   * can be validated with {@code Verify.that(interaction)...} expectations.
   *
   * @param actor the actor with the required ERP client abilities
   * @return the interaction wrapping the expected {@link ErxMedicationDispenseBundle}
   */
  @Override
  @Step("{0} dispensiert ein E-Rezept mit #taskId und #accessCode ohne close")
  public ErpInteraction<ErxMedicationDispenseBundle> answeredBy(Actor actor) {

    return this.performCommandAs(dispensePrescriptionAsBundleCommand, actor);
  }

  /** Builder for {@link DispensePrescriptionOld}. */
  @AllArgsConstructor
  public static class Builder {
    private final TaskId taskId;
    private final Secret secret;

    // kept for compatibility; manipulators are not applied within this legacy path
    @SuppressWarnings("unused")
    private final List<Consumer<KbvErpBundle>> manipulator = new LinkedList<>();

    /**
     * Configures dispense with explicit MedicationDispense resources.
     *
     * @param erxMedicationDispenses list of {@link ErxMedicationDispense} to be sent
     * @return the configured {@link DispensePrescriptionOld} action
     */
    public DispensePrescriptionOld withMedDsp(List<ErxMedicationDispense> erxMedicationDispenses) {
      val cmd = new DispensePrescriptionAsBundleCommandOld(taskId, secret, erxMedicationDispenses);
      return new DispensePrescriptionOld(cmd);
    }

    /**
     * Configures the dispense using operation parameters that instruct the server to create the
     * MedicationDispense data server-side.
     *
     * @param params {@link GemDispenseOperationParameters} controlling the dispense operation
     * @return the configured {@link DispensePrescriptionOld} action
     */
    public DispensePrescriptionOld withParameters(GemDispenseOperationParameters params) {
      val cmd = new DispensePrescriptionAsBundleCommandOld(taskId, secret, params);
      return new DispensePrescriptionOld(cmd);
    }
  }
}
