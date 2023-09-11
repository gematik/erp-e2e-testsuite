/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.actions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.client.usecases.TaskGetByExamEvidenceCommand;
import de.gematik.test.erezept.client.usecases.TaskGetCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxTaskBundle;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class DownloadOpenTask extends ErpAction<ErxTaskBundle> {

  private final TaskGetCommand cmd;

  public static DownloadOpenTask withExamEvidence(String examEvidence) {
    log.info(format("Request Get /Task as pharmacy with exam evidence {0} ", examEvidence));
    val cmd = new TaskGetByExamEvidenceCommand(examEvidence);
    return new DownloadOpenTask(cmd);
  }

  public static DownloadOpenTask withoutExamEvidence() {
    log.info(format("Request Get /Task as pharmacy without exam evidence"));
    val cmd = new TaskGetByExamEvidenceCommand(null);
    return new DownloadOpenTask(cmd);
  }

  @Override
  public ErpInteraction<ErxTaskBundle> answeredBy(Actor actor) {
    return this.performCommandAs(cmd, actor);
  }
}
