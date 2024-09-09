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

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static de.gematik.test.erezept.fhir.testutil.ErxFhirTestResourceUtil.createErxAuditEventBundle;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.rest.param.QueryParameter;
import de.gematik.test.erezept.client.usecases.AuditEventGetCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEventBundle;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.TelematikID;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;

class DownloadAuditEventTest {

  @Test
  void shouldPerformCorrectCommand() {
    val useErpClient = mock(UseTheErpClient.class);

    val patient = new PatientActor("sina");
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.from("X123456789"), patient.getName()));
    patient.can(useErpClient);

    val agentName = "Am Flughafen";
    val agentId = TelematikID.from("3-SMC-B-Testkarte-883110000116873");

    val resource = createErxAuditEventBundle(agentId, agentName);
    val response =
        ErpResponse.forPayload(resource, ErxAuditEventBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(AuditEventGetCommand.class))).thenReturn(response);

    assertDoesNotThrow(() -> patient.performs(DownloadAuditEvent.orderByDateDesc()));
  }

  @Test
  void shouldPerformCorrectCommandWithQueryParams() {
    val useErpClient = mock(UseTheErpClient.class);

    val patient = new PatientActor("sina");
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.from("X123456789"), patient.getName()));
    patient.can(useErpClient);

    val agentName = "Am Flughafen";
    val agentId = TelematikID.from("3-SMC-B-Testkarte-883110000116873");

    val resource = createErxAuditEventBundle(agentId, agentName);
    val response =
        ErpResponse.forPayload(resource, ErxAuditEventBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(AuditEventGetCommand.class))).thenReturn(response);

    assertDoesNotThrow(
        () ->
            patient.performs(
                DownloadAuditEvent.withQueryParams(new QueryParameter("test", "test"))));
  }
}
