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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.vsdm.types.VsdmPatient;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskGetByExamEvidenceCommand;
import de.gematik.test.erezept.client.usecases.TaskGetCommand;
import de.gematik.test.erezept.exceptions.FeatureNotImplementedException;
import de.gematik.test.erezept.fhir.r4.erp.ErxTaskBundle;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.ActorRole;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.hl7.fhir.r4.model.Task;

@Slf4j
public class ResponseOfGetTask extends FhirResponseQuestion<ErxTaskBundle> {

  private final ActorRole role;
  private final TaskGetCommand cmd;

  private ResponseOfGetTask(ActorRole role, TaskGetCommand cmd) {
    this.role = role;
    this.cmd = cmd;
  }

  @Override
  public ErpResponse<ErxTaskBundle> answeredBy(Actor actor) {
    if (ActorRole.PHARMACY.equals(role)) {
      return answeredByPharmacy(actor);
    }
    throw new FeatureNotImplementedException("Get /Task as patient");
  }

  private ErpResponse<ErxTaskBundle> answeredByPharmacy(Actor pharmacy) {
    val erpClient = SafeAbility.getAbility(pharmacy, UseTheErpClient.class);
    val response = erpClient.request(cmd);
    val erxTaskBundle = response.getResourceOptional();
    erxTaskBundle.ifPresent(
        bundle -> {
          val prescriptionManager =
              SafeAbility.getAbility(pharmacy, ManagePharmacyPrescriptions.class);
          bundle.getTasks().stream()
              .sorted(Comparator.comparing(Task::getAuthoredOn))
              .map(t -> DmcPrescription.ownerDmc(t.getTaskId(), t.getAccessCode()))
              .filter(
                  dmc -> !prescriptionManager.getAssignedPrescriptions().getRawList().contains(dmc))
              .forEachOrdered(prescriptionManager::appendAssignedPrescription);
        });
    return response;
  }

  public static ResponseOfGetTask asPharmacy(
      Egk egk, String examEvidence, LocalDate insuranceStartDate, String street) {
    return asPharmacy(
        egk,
        examEvidence,
        Base64.getUrlEncoder()
            .encodeToString(VsdmPatient.generateHash(insuranceStartDate, street)));
  }

  public static ResponseOfGetTask asPharmacy(Egk egk, String examEvidence, String hcv) {
    log.info("Download all open task with exam evidence '{}' and hash '{}'", examEvidence, hcv);
    return new ResponseOfGetTask(
        ActorRole.PHARMACY,
        examEvidence != null
            ? new TaskGetByExamEvidenceCommand(examEvidence)
                .andKvnr(KVNR.from(egk.getKvnr()))
                .andHcv(hcv)
            : new TaskGetByExamEvidenceCommand());
  }
}
