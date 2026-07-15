/*
 * Copyright (Change Date see Readme), gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.actions;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static de.gematik.test.erezept.fhir.testutil.ErxFhirTestResourceUtil.createErxAuditEventBundle;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.rest.fd.FhirBResponse;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.usecases.AuditEventGetByIdCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEventBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import lombok.val;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

class DownloadAuditEventByIdTest extends ErpFhirBuildingTest {

  @Test
  void shouldHandleNonBundleResponse() {
    val useErpClient = mock(UseTheErpClient.class);

    val patient = new PatientActor("sina");
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.from("X123456789"), patient.getName()));
    patient.can(useErpClient);

    val resource = new OperationOutcome();

    val response =
        FhirBResponse.forPayload(ErxAuditEventBundle.class, resource)
            .withStatusCode(200)
            .andValidationResult(createEmptyValidationResult());

    when(useErpClient.request(ArgumentMatchers.<AuditEventGetByIdCommand>any()))
        .thenReturn(response);

    val prescriptionId = PrescriptionId.from("160.000.000.004.715.59");

    assertDoesNotThrow(
        () -> patient.performs(DownloadAuditEventById.forPrescriptionId(prescriptionId)));
  }

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
        FhirBResponse.forPayload(ErxAuditEventBundle.class, resource)
            .withStatusCode(200)
            .andValidationResult(createEmptyValidationResult());

    when(useErpClient.request(ArgumentMatchers.<AuditEventGetByIdCommand>any()))
        .thenReturn(response);

    val prescriptionId = PrescriptionId.from("160.000.000.004.715.59");

    assertDoesNotThrow(
        () -> patient.performs(DownloadAuditEventById.forPrescriptionId(prescriptionId)));
  }
}
