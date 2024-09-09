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

package de.gematik.test.erezept.actions;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.client.usecases.TaskRejectCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import org.hl7.fhir.r4.model.Resource;

@RequiredArgsConstructor
public class TaskReject extends ErpAction<Resource> {

  private final TaskId taskId;
  private final AccessCode accessCode;
  private final Secret secret;

  @Override
  @Step("{0} weist den akzeptierten Task #taskId mit #accessCode und #secret zurück")
  public ErpInteraction<Resource> answeredBy(Actor actor) {
    val cmd = new TaskRejectCommand(taskId, accessCode, secret);
    return this.performCommandAs(cmd, actor);
  }

  public static TaskReject acceptedTask(ErxAcceptBundle acceptBundle) {
    val taskId = acceptBundle.getTaskId();
    val accessCode = acceptBundle.getTask().getAccessCode();
    val secret = acceptBundle.getSecret();
    return new Instrumented.InstrumentedBuilder<>(TaskReject.class)
        .withProperties(taskId, accessCode, secret);
  }
}
