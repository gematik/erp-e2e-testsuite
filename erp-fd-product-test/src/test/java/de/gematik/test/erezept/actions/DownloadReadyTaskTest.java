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
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidenceResult;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

class DownloadReadyTaskTest {

  @Test
  void shouldPerformCorrectCommand() {
    val useErpClient = mock(UseTheErpClient.class);
    val pharmacist = new PharmacyActor("PhaMoc");
    pharmacist.can(useErpClient);

    val sina = new PatientActor("sina");
    val providePatientBaseData = ProvidePatientBaseData.forGkvPatient(KVNR.random(), "sina");
    sina.can(providePatientBaseData);

    val examEvidence =
        VsdmExamEvidence.builder(VsdmExamEvidenceResult.NO_UPDATES).build().encodeAsBase64();

    val mockResponse =
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), ErxTaskBundle.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(useErpClient.request(any(TaskGetByExamEvidenceCommand.class))).thenReturn(mockResponse);

    assertDoesNotThrow(() -> pharmacist.performs(DownloadReadyTask.withExamEvidence(examEvidence)));
    assertDoesNotThrow(() -> pharmacist.performs(DownloadReadyTask.withoutExamEvidence()));



  }
}
