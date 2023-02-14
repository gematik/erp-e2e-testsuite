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

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.exceptions.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.strategy.*;
import de.gematik.test.erezept.screenplay.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import net.serenitybdd.screenplay.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ResponseOfGetTask extends FhirResponseQuestion<ErxTaskBundle> {

  private final ActorRole role;
  private final TaskGetCommand cmd;

  @Override
  public Class<ErxTaskBundle> expectedResponseBody() {
    return ErxTaskBundle.class;
  }

  @Override
  public String getOperationName() {
    return "Task";
  }

  @Override
  public ErpResponse answeredBy(Actor actor) {
    if (ActorRole.PHARMACY.equals(role)) {
      return answeredByPharmacy(actor);
    }
    throw new FeatureNotImplementedException("Get /Task as patient");
  }

  private ErpResponse answeredByPharmacy(Actor pharmacy) {
    val erpClient = SafeAbility.getAbility(pharmacy, UseTheErpClient.class);
    val response = erpClient.request(cmd);
    val erxTaskBundle = erpClient.request(cmd).getResourceOptional(cmd.expectedResponseBody());
    if (erxTaskBundle.isPresent()) {
      val prescriptionManager = SafeAbility.getAbility(pharmacy, ManagePharmacyPrescriptions.class);
      erxTaskBundle.get().getTasks().stream()
          .map(t -> DmcPrescription.ownerDmc(t.getUnqualifiedId(), t.getAccessCode()))
          .filter(dmc -> !prescriptionManager.getAssignedPrescriptions().getRawList().contains(dmc))
          .forEachOrdered(prescriptionManager::appendAssignedPrescription);
    }

    return response;
  }

  public static ResponseOfGetTask asPharmacy(String kvnr, String examEvidence) {
    log.info(
        format("Download all open task with kvnr {0} and exam evidence {1}", kvnr, examEvidence));
    return new ResponseOfGetTask(
        ActorRole.PHARMACY, new TaskGetByExamEvidenceCommand(kvnr, examEvidence));
  }
}
