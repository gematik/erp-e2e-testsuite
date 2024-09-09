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

package de.gematik.test.core.expectations.verifier;

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItemBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import java.util.function.Predicate;
import lombok.val;

public class ChargeItemBundleVerifier {

  private ChargeItemBundleVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<ErxChargeItemBundle> chargeItemIdIsEqualTo(
      PrescriptionId prescriptionId) {
    Predicate<ErxChargeItemBundle> predicate =
        erxChargeItemBundle ->
            erxChargeItemBundle.getChargeItem().getIdPart().equals(prescriptionId.getValue());
    val step =
        new VerificationStep.StepBuilder<ErxChargeItemBundle>(
            ErpAfos.A_24471, "die ID des ChargeItem entspricht nicht der RezeptId");
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxChargeItemBundle> chargeItemIdIsNotEqualTo(
      PrescriptionId prescriptionId) {
    Predicate<ErxChargeItemBundle> predicate =
        erxChargeItemBundle ->
            !erxChargeItemBundle.getChargeItem().getIdPart().equals(prescriptionId.getValue());
    val step =
        new VerificationStep.StepBuilder<ErxChargeItemBundle>(
            ErpAfos.A_24471, "die ID des ChargeItem entspricht nicht: " + prescriptionId);
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxChargeItemBundle> prescriptionIdIsEqualTo(TaskId taskId) {
    Predicate<ErxChargeItemBundle> predicate =
        erxChargeItemBundle ->
            erxChargeItemBundle
                .getChargeItem()
                .getPrescriptionId()
                .getValue()
                .equals(taskId.getValue());
    val step =
        new VerificationStep.StepBuilder<ErxChargeItemBundle>(
            ErpAfos.A_22136_01, "die Prescription ID im ChargeItem entspricht nicht: " + taskId);
    return step.predicate(predicate).accept();
  }
}
