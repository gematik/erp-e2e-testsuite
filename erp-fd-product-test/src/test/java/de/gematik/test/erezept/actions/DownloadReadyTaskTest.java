/*
 * Copyright 2023 gematik GmbH
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskGetByExamEvidenceCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxTaskBundle;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidenceResult;
import de.gematik.test.smartcard.Egk;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DownloadReadyTaskTest {

  private static VsdmExamEvidence examEvidence;
  private static PharmacyActor pharmacist;
  private static PatientActor sina;

  @BeforeAll
  static void setup() {
    val useErpClient = mock(UseTheErpClient.class);
    pharmacist = new PharmacyActor("PhaMoc");
    pharmacist.can(useErpClient);

    sina = new PatientActor("sina");
    val egk = new Egk(List.of(), "80276883110000113298", "X110498565");
    sina.can(ProvideEGK.sheOwns(egk));
    val providePatientBaseData =
        ProvidePatientBaseData.forGkvPatient(KVNR.from(egk.getKvnr()), "sina");
    sina.can(providePatientBaseData);

    examEvidence =
        VsdmExamEvidence.asOnlineTestMode(sina.getEgk())
            .generate(VsdmExamEvidenceResult.NO_UPDATES);

    val mockResponse =
        ErpResponse.forPayload(new ErxTaskBundle(), ErxTaskBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(useErpClient.request(any(TaskGetByExamEvidenceCommand.class))).thenReturn(mockResponse);
  }

  @Test
  void withExamEvidence() {
    assertDoesNotThrow(() -> pharmacist.performs(DownloadReadyTask.withExamEvidence(examEvidence)));
  }

  @Test
  void withExamEvidenceAndKVNR() {
    assertDoesNotThrow(
        () ->
            pharmacist.performs(DownloadReadyTask.withExamEvidence(examEvidence, sina.getKvnr())));
  }

  @Test
  void withoutExamEvidence() {
    assertDoesNotThrow(
        () -> pharmacist.performs(DownloadReadyTask.withoutExamEvidence(sina.getKvnr())));
  }

  @Test
  void withInvalidExamEvidence() {
    assertDoesNotThrow(() -> pharmacist.performs(DownloadReadyTask.withInvalidExamEvidence()));
  }
}
