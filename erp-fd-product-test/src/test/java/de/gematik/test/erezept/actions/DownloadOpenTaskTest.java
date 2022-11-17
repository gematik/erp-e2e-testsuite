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

package de.gematik.test.erezept.actions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.actors.*;
import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.konnektor.commands.options.*;
import java.util.*;
import lombok.*;
import org.junit.jupiter.api.*;

class DownloadOpenTaskTest {

  @Test
  void shouldPerformCorrectCommand() {
    val useErpClient = mock(UseTheErpClient.class);
    val pharmacist = new PharmacyActor("PhaMoc");
    pharmacist.can(useErpClient);

    val sina = new PatientActor("sina");
    val providePatientBaseData = ProvidePatientBaseData.forGkvPatient("X12345678", "sina");
    sina.can(providePatientBaseData);

    val examEvidence = ExamEvidence.NO_UPDATES.encodeAsBase64();

    val mockResponse =
        new ErpResponse(404, Map.of(), FhirTestResourceUtil.createOperationOutcome());
    when(useErpClient.request(any(TaskGetByExamEvidenceCommand.class))).thenReturn(mockResponse);

    assertDoesNotThrow(
        () ->
            pharmacist.performs(
                DownloadOpenTask.builder()
                    .kvnr(sina.getKvnr())
                    .examEvidence(examEvidence)
                    .build()));
  }
}
