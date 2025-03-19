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

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.vsdm.types.VsdmPatient;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.usecases.TaskGetByExamEvidenceCommand;
import de.gematik.test.erezept.client.usecases.TaskGetCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxTaskBundle;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence;
import java.time.LocalDate;
import java.util.Base64;
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

  public static DownloadReadyTask with(VsdmExamEvidence examEvidence, Egk egk) {
    return with(examEvidence, egk, List.of());
  }

  public static DownloadReadyTask with(
      VsdmExamEvidence examEvidence, Egk egk, List<IQueryParameter> queryParameter) {
    val street = egk.getOwnerData().getStreet();
    return with(
        examEvidence,
        KVNR.from(egk.getKvnr()),
        egk.getInsuranceStartDate(),
        street == null ? "" : street,
        queryParameter);
  }

  public static DownloadReadyTask with(
      VsdmExamEvidence examEvidence, KVNR kvnr, LocalDate insuranceStartDate, String street) {
    return with(examEvidence, kvnr, insuranceStartDate, street, List.of());
  }

  public static DownloadReadyTask with(
      VsdmExamEvidence examEvidence,
      KVNR kvnr,
      LocalDate insuranceStartDate,
      String street,
      List<IQueryParameter> queryParameter) {
    return with(
        examEvidence.encode(), kvnr, generateHash(insuranceStartDate, street), queryParameter);
  }

  public static DownloadReadyTask withoutPnwParameter(
      KVNR kvnr, LocalDate insuranceStartDate, String street) {
    return with(null, kvnr, generateHash(insuranceStartDate, street), List.of());
  }

  public static DownloadReadyTask with(
      String examEvidence, KVNR kvnr, LocalDate insuranceStartDate, String street) {
    return with(examEvidence, kvnr, generateHash(insuranceStartDate, street), List.of());
  }

  public static DownloadReadyTask withoutHcvParameter(VsdmExamEvidence examEvidence, KVNR kvnr) {
    return with(examEvidence.encode(), kvnr, null, List.of());
  }

  public static DownloadReadyTask withoutKvnrParameter(
      VsdmExamEvidence examEvidence, LocalDate insuranceStartDate, String street) {
    return with(examEvidence, null, insuranceStartDate, street, List.of());
  }

  private static String generateHash(LocalDate insuranceStartDate, String street) {
    return Base64.getUrlEncoder()
        .encodeToString(VsdmPatient.generateHash(insuranceStartDate, street == null ? "" : street));
  }

  public static DownloadReadyTask with(
      String examEvidenceAsBase64, KVNR kvnr, String hcv, List<IQueryParameter> queryParameter) {
    log.info(
        "Request Get /Task as pharmacy with exam evidence {}, kvnr {}, hcv {} and QueryParam {} ",
        examEvidenceAsBase64,
        kvnr,
        hcv,
        queryParameter);
    var cmd =
        examEvidenceAsBase64 != null
            ? new TaskGetByExamEvidenceCommand(examEvidenceAsBase64)
            : new TaskGetByExamEvidenceCommand();
    cmd.andAdditionalQuery(queryParameter);
    if (kvnr != null) {
      cmd = cmd.andKvnr(kvnr);
    }
    if (hcv != null) {
      cmd = cmd.andHcv(hcv);
    }
    return new DownloadReadyTask(cmd);
  }

  public static DownloadReadyTask asPatient(List<IQueryParameter> queryParameter) {
    val cmd = new DownloadReadyTask(new TaskGetCommand(queryParameter));
    log.info(
        "Request Get /Task with Query {} as as Patient with Sort- or PagingParams ",
        queryParameter);
    return cmd;
  }

  @Override
  public ErpInteraction<ErxTaskBundle> answeredBy(Actor actor) {
    return this.performCommandAs(cmd, actor);
  }
}
