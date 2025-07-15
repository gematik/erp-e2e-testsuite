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
import de.gematik.test.erezept.client.usecases.CloseTaskCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor
public class ClosePrescriptionWithoutDispensation extends ErpAction<ErxReceipt> {
  private final TaskId task;
  private final Secret secret;

  public static ClosePrescriptionWithoutDispensation forTheTask(ErxTask forTask, Secret secret) {
    return forTheTask(forTask.getTaskId(), secret);
  }

  public static ClosePrescriptionWithoutDispensation forTheTask(TaskId taskId, Secret secret) {
    Object[] params = {taskId, secret};
    return new Instrumented.InstrumentedBuilder<>(
            ClosePrescriptionWithoutDispensation.class, params)
        .newInstance();
  }

  @Override
  @Step(
      "{0} schließt eine Dispensierung mit $close ab ohne eine MedicationDispense zu übergeben für"
          + " #task")
  public ErpInteraction<ErxReceipt> answeredBy(Actor actor) {
    val cmd = new CloseTaskCommand(task, secret);
    return this.performCommandAs(cmd, actor);
  }
}
