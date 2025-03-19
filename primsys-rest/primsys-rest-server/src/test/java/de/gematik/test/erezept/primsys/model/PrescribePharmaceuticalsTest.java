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

package de.gematik.test.erezept.primsys.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskActivateCommand;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.data.MedicationRequestDto;
import de.gematik.test.erezept.primsys.data.MvoDto;
import de.gematik.test.erezept.primsys.data.PatientDto;
import de.gematik.test.erezept.primsys.data.PrescribeRequestDto;
import de.gematik.test.erezept.primsys.data.PrescriptionDto;
import de.gematik.test.erezept.primsys.data.error.ErrorDto;
import de.gematik.test.erezept.primsys.mapping.MvoDataMapper;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class PrescribePharmaceuticalsTest extends TestWithActorContext {

  @Test
  void shouldPrescribeWithDto() {
    val ctx = ActorContext.getInstance();
    val doctor = ctx.getDoctors().get(0);
    val mockClient = doctor.getClient();

    val accessCode = AccessCode.random();
    val taskId = TaskId.from(PrescriptionId.random());
    val activatedErxTask = new ErxTask();
    activatedErxTask.setId(taskId.getValue());
    activatedErxTask.setAuthoredOn(new Date());
    activatedErxTask.addIdentifier(PrescriptionId.random().asIdentifier());
    activatedErxTask.addIdentifier(accessCode.asIdentifier());

    val draftErxTask = mock(ErxTask.class);
    when(draftErxTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
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

    val prescribeRequest = new PrescribeRequestDto();
    val patient = new PatientDto();
    patient.setKvnr("X123123123");
    prescribeRequest.setPatient(patient);
    try (val response =
        PrescribePharmaceuticals.as(doctor).asNormalAssignment().withDto(prescribeRequest)) {
      val resMap = (PrescriptionDto) response.getEntity();
      assertTrue(response.hasEntity());
      assertEquals(taskId.getValue(), resMap.getTaskId());
      assertEquals(accessCode.getValue(), resMap.getAccessCode());
    }
  }

  @Test
  void shouldPrescribeWithXml() {
    val ctx = ActorContext.getInstance();
    val doctor = ctx.getDoctors().get(0);
    val mockClient = doctor.getClient();

    val accessCode = AccessCode.random();
    val taskId = TaskId.from("123");
    val activatedErxTask = new ErxTask();
    activatedErxTask.setId(taskId.getValue());
    activatedErxTask.setAuthoredOn(new Date());
    activatedErxTask.addIdentifier(PrescriptionId.random().asIdentifier());
    activatedErxTask.addIdentifier(accessCode.asIdentifier());

    val draftErxTask = mock(ErxTask.class);
    when(draftErxTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
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

    val kbvBundle = parser.encode(KbvErpBundleFaker.builder().fake(), EncodingType.XML);
    try (val response =
        PrescribePharmaceuticals.as(doctor).asDirectAssignment().withKbvBundle(kbvBundle)) {
      val resMap = (PrescriptionDto) response.getEntity();
      assertTrue(response.hasEntity());
      assertEquals(taskId.getValue(), resMap.getTaskId());
      assertEquals(accessCode.getValue(), resMap.getAccessCode());
    }
    try (val response1 =
        PrescribePharmaceuticals.as(doctor).asDirectAssignment().withKbvBundle(kbvBundle)) {
      val resMap = (PrescriptionDto) response1.getEntity();
      assertTrue(response1.hasEntity());
      assertEquals(taskId.getValue(), resMap.getTaskId());
      assertEquals(accessCode.getValue(), resMap.getAccessCode());
    }
  }

  @Test
  void shouldThrowWebAppExceptionOnMissingPatient() {
    val ctx = ActorContext.getInstance();
    val doctor = ctx.getDoctors().get(0);

    val prescribeBody = new PrescribeRequestDto();

    try (val response =
        PrescribePharmaceuticals.as(doctor).asNormalAssignment().withDto(prescribeBody)) {
      fail(
          "PrescribeUseCase did not throw the expected Exception and answered with "
              + response.getStatus());
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(400, wae.getResponse().getStatus());
      assertEquals(ErrorDto.class, wae.getResponse().getEntity().getClass());
      assertTrue(
          ((ErrorDto) wae.getResponse().getEntity()).getMessage().contains("KVNR is required"));
    }
  }

  @Test
  void shouldThrowWebAppExceptionOnMissingKvnr() {
    val ctx = ActorContext.getInstance();
    val doctor = ctx.getDoctors().get(0);

    val prescribeBody = new PrescribeRequestDto();
    val patientDto = new PatientDto();
    patientDto.setKvnr(null);
    prescribeBody.setPatient(patientDto);

    try (val response =
        PrescribePharmaceuticals.as(doctor).asNormalAssignment().withDto(prescribeBody)) {
      fail(
          "PrescribeUseCase did not throw the expected Exception and answered with "
              + response.getStatus());
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(400, wae.getResponse().getStatus());
      assertEquals(ErrorDto.class, wae.getResponse().getEntity().getClass());
      assertTrue(
          ((ErrorDto) wae.getResponse().getEntity()).getMessage().contains("KVNR is required"));
    }
  }

  @Test
  void shouldRethrowOnFailedTaskCreation() {
    val ctx = ActorContext.getInstance();
    val doctor = ctx.getDoctors().get(0);
    val mockClient = doctor.getClient();

    val prescribeRequest = new PrescribeRequestDto();
    val patientDto = new PatientDto();
    patientDto.setKvnr("X123123123");
    prescribeRequest.setPatient(patientDto);

    val createResponse =
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), ErxTask.class)
            .withHeaders(Map.of())
            .withStatusCode(401)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    doThrow(ErrorResponseBuilder.createFachdienstErrorException(createResponse))
        .when(mockClient)
        .request(any(TaskCreateCommand.class));

    try (val response =
        PrescribePharmaceuticals.as(doctor).asNormalAssignment().withDto(prescribeRequest)) {
      fail(
          "PrescribeUseCase did not throw the expected Exception and answered with "
              + response.getStatus());
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(401, wae.getResponse().getStatus());
      assertEquals(ErrorDto.class, wae.getResponse().getEntity().getClass());
    }
  }

  @Test
  void shouldIssueMvoPrescriptionWithDto() {
    val ctx = ActorContext.getInstance();
    val doctor = ctx.getDoctors().get(0);
    val mockClient = doctor.getClient();

    val activatedErxTask = new ErxTask();
    activatedErxTask.setId("123");
    activatedErxTask.setAuthoredOn(new Date());
    activatedErxTask.addIdentifier(PrescriptionId.random().asIdentifier());
    activatedErxTask.addIdentifier(AccessCode.random().asIdentifier());

    val draftErxTask = mock(ErxTask.class);
    val accessCode = AccessCode.random();
    val taskId = "test Id as String";

    when(draftErxTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
    when(draftErxTask.getTaskId()).thenReturn(TaskId.from(taskId));
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

    val prescribeRequest = new PrescribeRequestDto();
    val patient = new PatientDto();
    patient.setKvnr("X123123123");
    prescribeRequest.setPatient(patient);
    prescribeRequest.setMedicationRequest(MedicationRequestDto.medicationRequest().build());
    val mvoData = MvoDataMapper.randomDto();
    prescribeRequest.getMedicationRequest().setMvo(mvoData);
    try (val response =
        PrescribePharmaceuticals.as(doctor).asNormalAssignment().withDto(prescribeRequest)) {
      assertTrue(response.hasEntity());
      assertEquals(202, response.getStatus());
      val prescriptionDto = (PrescriptionDto) response.getEntity();
      assertNotNull(prescriptionDto.getMedicationRequest().getMvo());
    }
  }

  @Test
  void shouldThrowOnInvalidMvoDto() {
    val ctx = ActorContext.getInstance();
    val doctor = ctx.getDoctors().get(0);
    val mockClient = doctor.getClient();

    val erxTask = mock(ErxTask.class);
    val accessCode = AccessCode.random();
    val taskId = "test Id as String";

    when(erxTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
    when(erxTask.getTaskId()).thenReturn(TaskId.from(taskId));
    when(erxTask.getOptionalAccessCode()).thenReturn(Optional.of(accessCode));
    when(erxTask.getAuthoredOn()).thenReturn(new Date());
    when(erxTask.getAccessCode()).thenReturn(accessCode);

    val createResponse =
        ErpResponse.forPayload(erxTask, ErxTask.class)
            .withStatusCode(204)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    val activateResponse =
        ErpResponse.forPayload(erxTask, ErxTask.class)
            .withStatusCode(204)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    when(mockClient.request(any(TaskCreateCommand.class))).thenReturn(createResponse);
    when(mockClient.request(any(TaskActivateCommand.class))).thenReturn(activateResponse);

    val prescribeRequest = new PrescribeRequestDto();
    val patient = new PatientDto();
    patient.setKvnr("X123123123");
    prescribeRequest.setPatient(patient);
    prescribeRequest.setMedicationRequest(MedicationRequestDto.medicationRequest().build());
    val mvoData = new MvoDto();
    mvoData.setNumerator(12);
    mvoData.setDenominator(11);
    prescribeRequest.getMedicationRequest().setMvo(mvoData);

    try (val response =
        PrescribePharmaceuticals.as(doctor).asNormalAssignment().withDto(prescribeRequest)) {
      fail(
          "PrescribeUseCase did not throw the expected Exception and answered with "
              + response.getStatus());
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(400, wae.getResponse().getStatus());
      assertEquals(ErrorDto.class, wae.getResponse().getEntity().getClass());
      assertTrue(
          ((ErrorDto) wae.getResponse().getEntity()).getMessage().contains("MVO Data is invalid"));
    }
  }

  @ParameterizedTest(
      name = "#{index} - Detect Error while parsing FHIR-Bundle with directAssignment={0}")
  @NullSource // pass a null value
  @ValueSource(booleans = {true, false})
  void shouldReturn400OnInvalidFhirBundle(Boolean directAssignment) {
    val ctx = ActorContext.getInstance();
    val doctor = ctx.getDoctors().get(0);

    val invalidFhir = "hello world";
    Supplier<Response> responseSupplier;
    if (directAssignment != null) {
      responseSupplier =
          () ->
              PrescribePharmaceuticals.as(doctor)
                  .assignDirectly(directAssignment)
                  .withKbvBundle(invalidFhir);
    } else {
      responseSupplier =
          () -> PrescribePharmaceuticals.as(doctor).asNormalAssignment().withKbvBundle(invalidFhir);
    }
    try (val response = responseSupplier.get()) {
      fail(
          "PrescribeUseCase did not throw the expected Exception and answered with "
              + response.getStatus());
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(400, wae.getResponse().getStatus());
      assertEquals(ErrorDto.class, wae.getResponse().getEntity().getClass());
      assertTrue(
          ((ErrorDto) wae.getResponse().getEntity())
              .getMessage()
              .contains("Unable to decode the given FHIR-Content"));
    }
  }
}
