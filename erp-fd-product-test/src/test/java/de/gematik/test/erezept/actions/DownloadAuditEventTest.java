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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.actors.*;
import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.konnektor.soap.mock.vsdm.*;
import java.util.*;
import lombok.*;
import org.junit.jupiter.api.*;

class DownloadAuditEventTest {

  @Test
  void shouldPerformCorrectCommand() {
    val useErpClient = mock(UseTheErpClient.class);

    val patient = new PatientActor("sina");
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.from("X123456789"), patient.getName()));
    patient.can(useErpClient);

    val agentName = "Am Flughafen";
    val agentId = TelematikID.from("3-SMC-B-Testkarte-883110000116873");
    val checksum =
        VsdmExamEvidence.builder(VsdmExamEvidenceResult.NO_UPDATES)
            .checksum(VsdmChecksum.builder("X12345678").build())
            .build()
            .getChecksum()
            .orElseThrow();

    val resource = FhirTestResourceUtil.createErxAuditEventBundle(agentId, agentName, checksum);
    val response =
        ErpResponse.forPayload(resource, ErxAuditEventBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(useErpClient.request(any(AuditEventGetCommand.class))).thenReturn(response);

    assertDoesNotThrow(() -> patient.performs(DownloadAuditEvent.orderByDateDesc()));
  }
}
