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

package de.gematik.test.erezept.actions;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.TaskId;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;

@Slf4j
public class TheTask extends ErpAction<ErxPrescriptionBundle> {

  private final TaskId taskId;

  public TheTask(TaskId taskId) {
    this.taskId = taskId;
  }

  @Override
  @Step("{0} ruft den Task mit der ID #taskId ab")
  public ErpInteraction<ErxPrescriptionBundle> answeredBy(Actor actor) {
    val cmd = new TaskGetByIdCommand(taskId);
    return this.performCommandAs(cmd, actor);
  }

  public static TheTask fromBackend(ErxTask task) {
    return withId(task.getTaskId());
  }

  public static TheTask withId(TaskId id) {
    Object[] params = {id};
    return new Instrumented.InstrumentedBuilder<>(TheTask.class, params).newInstance();
  }
}
