/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.screenplay.task;

import de.gematik.test.erezept.client.usecases.TaskGetByExamEvidenceCommand;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.smartcard.Egk;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.ensure.Ensure;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class DownloadAllOpenPrescriptions implements Task {

  private final Egk egk;

  private final String examEvidence;

  private final int expectedStatusCode;

  @Override
  public <T extends Actor> void performAs(T pharmacy) {
    val kvnr = egk.getKvnr();

    val erpClient = SafeAbility.getAbility(pharmacy, UseTheErpClient.class);
    val cmd = new TaskGetByExamEvidenceCommand(kvnr, examEvidence);
    val response = erpClient.request(cmd);

    pharmacy.attemptsTo(Ensure.that(response.getStatusCode()).isEqualTo(expectedStatusCode));

    if (expectedStatusCode == 200) {
      val erxTaskBundle = response.getResource(cmd.expectedResponseBody());
      // ToDo: A_22431: ensure that erxTaskBundle do not included signatures
      val prescriptionManager = SafeAbility.getAbility(pharmacy, ManagePharmacyPrescriptions.class);
      erxTaskBundle.getTasks().stream()
          .map(t -> DmcPrescription.ownerDmc(t.getUnqualifiedId(), t.getAccessCode()))
          .filter(dmc -> !prescriptionManager.getAssignedPrescriptions().getRawList().contains(dmc))
          .forEachOrdered(prescriptionManager::appendAssignedPrescription);
    }
  }

  public static class Builder {
    private final Egk egk;
    private final String examEvidence;
    private Integer expectedStatusCode;

    public Builder(Egk egk, String examEvidence) {
      this.egk = egk;
      this.examEvidence = examEvidence;
    }

    public Builder setExpectedStatusCode(int expectedStatusCode) {
      this.expectedStatusCode = expectedStatusCode;
      return this;
    }

    public DownloadAllOpenPrescriptions build() {
      return new DownloadAllOpenPrescriptions(
          egk, examEvidence, Optional.ofNullable(expectedStatusCode).orElse(200));
    }
  }
}
