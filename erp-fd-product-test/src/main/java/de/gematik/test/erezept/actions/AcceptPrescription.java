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
import de.gematik.test.erezept.client.usecases.TaskAcceptCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor
public class AcceptPrescription extends ErpAction<ErxAcceptBundle> {

  private final TaskId taskId;
  private final AccessCode accessCode;

  @Override
  @Step("{0} akzeptiert das E-Rezept mit #taskId und #accessCode")
  public ErpInteraction<ErxAcceptBundle> answeredBy(Actor actor) {
    val cmd = new TaskAcceptCommand(taskId, accessCode);
    return this.performCommandAs(cmd, actor);
  }

  public static AcceptPrescription forTheTaskFrom(
      ErpInteraction<ErxPrescriptionBundle> interaction) {
    return forTheTaskFrom(interaction.getExpectedResponse());
  }

  public static AcceptPrescription forTheTaskFrom(ErxPrescriptionBundle prescription) {
    return forTheTask(prescription.getTask());
  }

  public static AcceptPrescription forTheTask(ErxTask task) {
    return with(task.getTaskId(), task.getAccessCode());
  }

  public static AcceptPrescription with(TaskId taskId, AccessCode accessCode) {
    Object[] params = {taskId, accessCode};
    return new Instrumented.InstrumentedBuilder<>(AcceptPrescription.class, params).newInstance();
  }
}
