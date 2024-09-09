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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.usecases.TaskGetByExamEvidenceCommand;
import de.gematik.test.erezept.client.usecases.TaskGetCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxTaskBundle;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class DownloadReadyTask extends ErpAction<ErxTaskBundle> {

  private final TaskGetCommand cmd;

  /**
   * This method is mainly intended for PN3 with kvnr
   *
   * @param examEvidence
   * @param kvnr
   * @return
   */
  public static DownloadReadyTask withExamEvidence(VsdmExamEvidence examEvidence, KVNR kvnr) {
    log.info(
        format(
            "Request Get /Task as pharmacy with exam evidence {0} and kvnr {1} ",
            examEvidence, kvnr));
    val cmd = new TaskGetByExamEvidenceCommand(examEvidence.encodeAsBase64()).andKvnr(kvnr);
    return new DownloadReadyTask(cmd);
  }

  public static DownloadReadyTask asPatient(List<IQueryParameter> queryParameter) {
    val cmd = new DownloadReadyTask(new TaskGetCommand(queryParameter));
    log.info(
        format(
            "Request Get /Task with Query {0} as as Patient with Sort- or PagingParams ",
            queryParameter));
    return cmd;
  }

  public static DownloadReadyTask withExamEvidenceAnOptionalQueryParams(
      VsdmExamEvidence examEvidence, KVNR kvnr, List<IQueryParameter> queryParameter) {
    log.info(
        format(
            "Request Get /Task as pharmacy with exam evidence {0} and kvnr {1} and QueryParam {2} ",
            examEvidence, kvnr, queryParameter));
    val cmd =
        new TaskGetByExamEvidenceCommand(examEvidence.encodeAsBase64())
            .andKvnr(kvnr)
            .andAdditionalQuery(queryParameter);
    return new DownloadReadyTask(cmd);
  }

  public static DownloadReadyTask withExamEvidence(VsdmExamEvidence examEvidence) {
    log.info(format("Request Get /Task as pharmacy with exam evidence {0}", examEvidence));
    val cmd = new TaskGetByExamEvidenceCommand(examEvidence.encodeAsBase64());
    return new DownloadReadyTask(cmd);
  }

  public static DownloadReadyTask withoutExamEvidence(KVNR kvnr) {
    log.info(format("Request Get /Task as pharmacy without exam evidence but with kvnr {0}", kvnr));
    val cmd = new TaskGetByExamEvidenceCommand().andKvnr(kvnr);
    return new DownloadReadyTask(cmd);
  }

  public static DownloadReadyTask withInvalidExamEvidence() {
    log.info(format("Request Get /Task as pharmacy with invalid exam evidence"));
    val cmd = new TaskGetByExamEvidenceCommand("abc");
    return new DownloadReadyTask(cmd);
  }

  @Override
  public ErpInteraction<ErxTaskBundle> answeredBy(Actor actor) {
    return this.performCommandAs(cmd, actor);
  }
}
