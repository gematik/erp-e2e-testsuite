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

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor
public class TaskAbort extends ErpAction<EmptyResource> {

  private final TaskId taskId;
  private final AccessCode accessCode;
  private final Secret secret;

  @Override
  @Step("{0} weist den aktivierten Task #taskId zurück")
  public ErpInteraction<EmptyResource> answeredBy(Actor actor) {
    val cmd = new TaskAbortCommand(taskId, accessCode, secret);
    return this.performCommandAs(cmd, actor);
  }

  public static TaskAbort asPatient(ErxTask task) {
    return new Instrumented.InstrumentedBuilder<>(TaskAbort.class)
        .withProperties(task.getTaskId(), null, null);
  }

  public static TaskAbort asLeistungserbringer(ErxTask task) {
    return new Instrumented.InstrumentedBuilder<>(TaskAbort.class)
        .withProperties(task.getTaskId(), task.getAccessCode(), null);
  }

  public static TaskAbort asPharmacy(ErxAcceptBundle acceptBundle) {
    return new Instrumented.InstrumentedBuilder<>(TaskAbort.class)
        .withProperties(acceptBundle.getTaskId(), null, acceptBundle.getSecret());
  }
}
