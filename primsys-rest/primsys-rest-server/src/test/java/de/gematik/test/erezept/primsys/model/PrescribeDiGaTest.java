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

package de.gematik.test.erezept.primsys.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskActivateCommand;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.builder.kbv.KbvCoverageFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvEvdgaBundleBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvHealthAppRequestFaker;
import de.gematik.test.erezept.fhir.builder.kbv.MedicalOrganizationFaker;
import de.gematik.test.erezept.fhir.builder.kbv.PatientFaker;
import de.gematik.test.erezept.fhir.builder.kbv.PractitionerFaker;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.data.PrescriptionDto;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.Test;

class PrescribeDiGaTest extends TestWithActorContext {

  @Test
  void shouldPrescribeWithXml() {
    val ctx = ActorContext.getInstance();
    val doctor = ctx.getDoctors().get(0);
    val mockClient = doctor.getClient();

    val accessCode = AccessCode.random();
    val prescriptionId = PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_162);
    val taskId = TaskId.from(prescriptionId);
    val activatedErxTask = new ErxTask();
    activatedErxTask.setId(taskId.getValue());
    activatedErxTask.setAuthoredOn(new Date());
    activatedErxTask.addIdentifier(prescriptionId.asIdentifier());
    activatedErxTask.addIdentifier(accessCode.asIdentifier());

    val draftErxTask = mock(ErxTask.class);
    when(draftErxTask.getPrescriptionId()).thenReturn(prescriptionId);
    when(draftErxTask.getTaskId()).thenReturn(taskId);
    when(draftErxTask.getOptionalAccessCode()).thenReturn(Optional.of(accessCode));
    when(draftErxTask.getAuthoredOn()).thenReturn(new Date());
    when(draftErxTask.getAccessCode()).thenReturn(accessCode);

    val createResponse =
        ErpResponse.forPayload(draftErxTask, ErxTask.class)
            .withStatusCode(204)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    val activateResponse =
        ErpResponse.forPayload(activatedErxTask, ErxTask.class)
            .withStatusCode(204)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    when(mockClient.request(any(TaskCreateCommand.class))).thenReturn(createResponse);
    when(mockClient.request(any(TaskActivateCommand.class))).thenReturn(activateResponse);

    val patient = PatientFaker.builder().fake();
    val practitioner = PractitionerFaker.builder().fake();
    val coverage = KbvCoverageFaker.builder().fake();
    val evdgaBundle =
        KbvEvdgaBundleBuilder.forPrescription(prescriptionId)
            .patient(patient)
            .insurance(coverage)
            .practitioner(practitioner)
            .healthAppRequest(
                KbvHealthAppRequestFaker.forPatient(patient)
                    .withInsurance(coverage)
                    .withRequester(practitioner)
                    .fake())
            .medicalOrganization(MedicalOrganizationFaker.builder().fake())
            .build();

    val xml = fhir.encode(evdgaBundle, EncodingType.XML);
    try (val response = PrescribeDiGa.as(doctor).withEvdga(xml)) {
      val resMap = (PrescriptionDto) response.getEntity();
      assertTrue(response.hasEntity());
      assertEquals(taskId.getValue(), resMap.getTaskId());
      assertEquals(accessCode.getValue(), resMap.getAccessCode());
    }
  }
}
