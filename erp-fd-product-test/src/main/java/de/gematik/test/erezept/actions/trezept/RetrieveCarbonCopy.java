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

package de.gematik.test.erezept.actions.trezept;

import de.gematik.test.erezept.abilities.UseTheTRegisterMockClient;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.trezept.TRegisterLog;
import de.gematik.test.erezept.trezept.TRegisterMockDownloadRequest;
import java.util.List;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class RetrieveCarbonCopy implements Question<List<TRegisterLog>> {

  private final String taskId;

  private RetrieveCarbonCopy(String taskId) {
    this.taskId = taskId;
  }

  public static RetrieveCarbonCopy forTask(ErxTask task) {
    return new RetrieveCarbonCopy(task.getTaskId().getValue());
  }

  @Override
  @Step("{0} ruft den Durchschlag das E-Rezepts mit #taskId beim BfarmMock ab")
  public List<TRegisterLog> answeredBy(Actor actor) {
    UseTheTRegisterMockClient ability = actor.abilityTo(UseTheTRegisterMockClient.class);
    return ability.pollRequest(new TRegisterMockDownloadRequest(taskId));
  }
}
