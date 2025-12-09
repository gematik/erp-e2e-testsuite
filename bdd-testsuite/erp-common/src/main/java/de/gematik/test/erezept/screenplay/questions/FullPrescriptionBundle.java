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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FullPrescriptionBundle implements Question<ErxPrescriptionBundle> {

  private final TaskId taskId;

  @Override
  public ErxPrescriptionBundle answeredBy(Actor actor) {
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val cmd = new TaskGetByIdCommand(taskId);
    return erpClient.request(cmd).getExpectedResource();
  }

  public static FullPrescriptionBundle forTask(ErxTask task) {
    return forTask(task.getTaskId());
  }

  public static FullPrescriptionBundle forTask(TaskId taskId) {
    return new FullPrescriptionBundle(taskId);
  }
}
