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

package de.gematik.test.core.expectations.verifier;

import static java.text.MessageFormat.format;

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.Requirement;
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.function.Predicate;
import lombok.val;
import org.hl7.fhir.r4.model.Task;

public class TaskVerifier {

  private TaskVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<ErxTask> hasWorkflowType(PrescriptionFlowType flowType) {
    return hasWorkflowType(flowType, ErpAfos.A_19112);
  }

  public static VerificationStep<ErxTask> hasWorkflowType(
      PrescriptionFlowType flowType, RequirementsSet req) {
    Predicate<ErxTask> predicate = task -> task.getFlowType().equals(flowType);
    val step =
        new VerificationStep.StepBuilder<ErxTask>(
            req.getRequirement(),
            format("Der WorkflowType muss {0} entsprechen", flowType.getCode()));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxTask> isInDraftStatus() {
    return isInStatus(Task.TaskStatus.DRAFT, ErpAfos.A_19114.getRequirement());
  }

  public static VerificationStep<ErxTask> isInReadyStatus() {
    return isInStatus(Task.TaskStatus.READY, ErpAfos.A_19128.getRequirement());
  }

  public static VerificationStep<ErxTask> isInStatus(Task.TaskStatus status, Requirement req) {
    Predicate<ErxTask> predicate = task -> status.equals(task.getStatus());
    val step =
        new VerificationStep.StepBuilder<ErxTask>(
            req, format("Der Status eines neuen Tasks muss sich im Status {0} befinden", status));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxTask> hasValidPrescriptionId() {
    Predicate<ErxTask> predicate =
        task -> {
          try {
            return task.getPrescriptionId().check();
          } catch (MissingFieldException mfe) {
            return false;
          }
        };
    val step =
        new VerificationStep.StepBuilder<ErxTask>(
            ErpAfos.A_19019.getRequirement(),
            "Die Rezept-ID muss valide gemäß der Bildungsregel [gemSpec_DM_eRp#19217] sein");
    return step.predicate(predicate).accept();
  }
}
