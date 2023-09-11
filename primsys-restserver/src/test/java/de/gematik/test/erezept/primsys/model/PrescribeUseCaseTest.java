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

package de.gematik.test.erezept.primsys.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ICommand;
import de.gematik.test.erezept.client.usecases.TaskActivateCommand;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import de.gematik.test.erezept.fhir.testutil.PrivateConstructorsUtil;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.primsys.model.actor.Doctor;
import de.gematik.test.erezept.primsys.rest.data.DoctorData;
import de.gematik.test.erezept.primsys.rest.data.MvoData;
import de.gematik.test.erezept.primsys.rest.data.PatientData;
import de.gematik.test.erezept.primsys.rest.request.PrescribeRequest;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

class PrescribeUseCaseTest {

  static DoctorData doctorData;

  @SuppressWarnings({"unchecked"})
  @SneakyThrows
  @Test
  void testIssuePrescriptionDoctorAndPrescribeRequest() {
    ErpResponse<ErxTask> mockCreateResponse = mock(ErpResponse.class);
    ErpResponse<ErxTask> mockActivateResponse = mock(ErpResponse.class);

    val accessCode = AccessCode.random();
    val taskId = TaskId.from("123");
    val activatedTask = new ErxTask();
    activatedTask.setId(taskId.getValue());
    activatedTask.setAuthoredOn(new Date());
    activatedTask.addIdentifier(PrescriptionId.random().asIdentifier());
    activatedTask.addIdentifier(accessCode.asIdentifier());
    when(mockActivateResponse.getResourceOptional()).thenReturn(Optional.of(activatedTask));

    val mockDraftErxTask = mock(ErxTask.class);
    val mockERPClient = mock(ErpClient.class);
    try (MockedStatic<ActorContext> mockedStaticActor = mockStatic(ActorContext.class)) {
      val mock = mock(ActorContext.class);
      mockedStaticActor.when(ActorContext::getInstance).thenReturn(mock);
      when(mockERPClient.encode(any(), any())).thenReturn("HelloMockClient");
      when(mockDraftErxTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
      when(mockDraftErxTask.getTaskId()).thenReturn(taskId);
      when(mockDraftErxTask.getOptionalAccessCode()).thenReturn(Optional.of(accessCode));
      when(mockDraftErxTask.getAuthoredOn()).thenReturn(new Date());
      when(mockDraftErxTask.getAccessCode()).thenReturn(accessCode);
      when(mockCreateResponse.getResourceOptional()).thenReturn(Optional.of(mockDraftErxTask));

      val mockDoctor = mock(Doctor.class);
      when(mockDoctor.signDocument(anyString())).thenReturn("signedDocument".getBytes());
      when(mockDoctor.getName()).thenReturn("Doc Doolittle");
      when(mockDoctor.getBaseData()).thenReturn(doctorData);
      when(mockDoctor.erpRequest(any(TaskCreateCommand.class))).thenReturn(mockCreateResponse);
      when(mockDoctor.erpRequest(any(TaskActivateCommand.class))).thenReturn(mockActivateResponse);
      when(mockDoctor.getClient()).thenReturn(mockERPClient);
      var prescribeRequest = new PrescribeRequest();
      final Map<String, String> resMap;
      try (val response = PrescribeUseCase.issuePrescription(mockDoctor, prescribeRequest)) {
        resMap = (Map<String, String>) response.getEntity();
        assertTrue(response.hasEntity());
      }
      assertEquals(taskId.getValue(), resMap.get("task-id"));
      assertEquals(accessCode.getValue(), resMap.get("access-code"));
    }
  }

  @SneakyThrows
  @Test
  void issuePrescriptionShouldThrowWebAppExceptionOnMissingPatient() {
    val mockDoctor = mock(Doctor.class);
    val prescribeBody = mock(PrescribeRequest.class);
    when(prescribeBody.getPatient()).thenReturn(null);

    try (val response = PrescribeUseCase.issuePrescription(mockDoctor, prescribeBody)) {
      fail(
          "PrescribeUseCase did not throw the expected Exception and answered with "
              + response.getStatus());
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(400, wae.getResponse().getStatus());
      assertEquals(ErrorResponse.class, wae.getResponse().getEntity().getClass());
      assertTrue(
          ((ErrorResponse) wae.getResponse().getEntity())
              .getMessage()
              .contains("KVNR is required"));
    }
  }

  @SneakyThrows
  @Test
  void issuePrescriptionShouldThrowWebAppExceptionOnMissingKvnr() {
    val mockDoctor = mock(Doctor.class);
    val prescribeBody = new PrescribeRequest();
    val mockPatient = mock(PatientData.class);
    prescribeBody.setPatient(mockPatient);
    when(mockPatient.getKvnr()).thenReturn(null);

    try (val response = PrescribeUseCase.issuePrescription(mockDoctor, prescribeBody)) {
      fail(
          "PrescribeUseCase did not throw the expected Exception and answered with "
              + response.getStatus());
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(400, wae.getResponse().getStatus());
      assertEquals(ErrorResponse.class, wae.getResponse().getEntity().getClass());
      assertTrue(
          ((ErrorResponse) wae.getResponse().getEntity())
              .getMessage()
              .contains("KVNR is required"));
    }
  }

  @SneakyThrows
  @Test
  void shouldRethrowOnFailedTaskCreation() {
    val prescribeRequest = new PrescribeRequest();
    val mockDoctor = mock(Doctor.class);

    val createResponse =
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), ErxTask.class)
            .withHeaders(Map.of())
            .withStatusCode(401)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    when(mockDoctor.getName()).thenReturn("Doc Doolittle");
    when(mockDoctor.erpRequest(any(TaskCreateCommand.class))).thenReturn(createResponse);

    try (val response = PrescribeUseCase.issuePrescription(mockDoctor, prescribeRequest)) {
      fail(
          "PrescribeUseCase did not throw the expected Exception and answered with "
              + response.getStatus());
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(401, wae.getResponse().getStatus());
      assertEquals(ErrorResponse.class, wae.getResponse().getEntity().getClass());
    }
  }

  @SneakyThrows
  @Test
  void getMedicationRequestIfMvoIsNotNull() {
    ErpResponse<ErxTask> mockCreateResponse = mock(ErpResponse.class);
    ErpResponse<ErxTask> mockActivateResponse = mock(ErpResponse.class);

    val activatedTask = new ErxTask();
    activatedTask.setId("123");
    activatedTask.setAuthoredOn(new Date());
    activatedTask.addIdentifier(PrescriptionId.random().asIdentifier());
    activatedTask.addIdentifier(AccessCode.random().asIdentifier());
    when(mockActivateResponse.getResourceOptional()).thenReturn(Optional.of(activatedTask));

    val mockErxTask = mock(ErxTask.class);
    val mockERPClient = mock(ErpClient.class);
    val accessCode = AccessCode.random();
    val taskId = "test Id as String";
    try (MockedStatic<ActorContext> mockedStaticActor = mockStatic(ActorContext.class)) {
      val mock = mock(ActorContext.class);
      mockedStaticActor.when(ActorContext::getInstance).thenReturn(mock);
      when(mockERPClient.encode(any(), any())).thenReturn("HelloMockClient");
      when(mockErxTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
      when(mockErxTask.getTaskId()).thenReturn(TaskId.from(taskId));
      when(mockErxTask.getOptionalAccessCode()).thenReturn(Optional.of(accessCode));
      when(mockErxTask.getAuthoredOn()).thenReturn(new Date());
      when(mockErxTask.getAccessCode()).thenReturn(accessCode);
      when(mockCreateResponse.getResourceOptional()).thenReturn(Optional.of(mockErxTask));
      when(mockCreateResponse.getExpectedResource()).thenReturn(mockErxTask);
      val mockDoctor = mock(Doctor.class);
      when(mockDoctor.signDocument(anyString())).thenReturn("signedDocument".getBytes());
      when(mockDoctor.getName()).thenReturn("Doc Doolittle");
      when(mockDoctor.getBaseData()).thenReturn(doctorData);
      when(mockDoctor.erpRequest(any(TaskCreateCommand.class))).thenReturn(mockCreateResponse);
      when(mockDoctor.erpRequest(any(TaskActivateCommand.class))).thenReturn(mockActivateResponse);
      when(mockDoctor.getClient()).thenReturn(mockERPClient);
      var prescribeRequest = new PrescribeRequest();
      MvoData mvoData = new MvoData();
      prescribeRequest.getMedication().setMvoData(mvoData.fakeMvoNumeDenomAndDates());
      try (val response = PrescribeUseCase.issuePrescription(mockDoctor, prescribeRequest)) {
        assertTrue(response.hasEntity());
        assertEquals(202, response.getStatus());
      }
    }
  }

  @SneakyThrows
  @Test
  void getMedicationRequestIfMvoIsInvalid() {
    val mockCreateResponse = mock(ErpResponse.class);
    val mockActivateResponse = mock(ErpResponse.class);
    val mockErxTask = mock(ErxTask.class);
    val mockERPClient = mock(ErpClient.class);
    val accessCode = AccessCode.random();
    val taskId = "test Id as String";
    Doctor mockDoctor = null;
    try (MockedStatic<ActorContext> mockedStaticActor = mockStatic(ActorContext.class)) {
      val mock = mock(ActorContext.class);
      mockedStaticActor.when(ActorContext::getInstance).thenReturn(mock);
      when(mockERPClient.encode(any(), any())).thenReturn("HelloMockClient");
      when(mockErxTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
      when(mockErxTask.getTaskId()).thenReturn(TaskId.from(taskId));
      when(mockErxTask.getOptionalAccessCode()).thenReturn(Optional.of(accessCode));
      when(mockErxTask.getAuthoredOn()).thenReturn(new Date());
      when(mockErxTask.getAccessCode()).thenReturn(accessCode);
      when(mockCreateResponse.getResourceOptional()).thenReturn(Optional.of(mockErxTask));
      when(mockCreateResponse.getExpectedResource()).thenReturn(mockErxTask);
      mockDoctor = mock(Doctor.class);
      when(mockDoctor.signDocument(anyString())).thenReturn("signedDocument".getBytes());
      when(mockDoctor.getName()).thenReturn("Doc Doolittle");
      when(mockDoctor.getBaseData()).thenReturn(doctorData);
      when(mockDoctor.erpRequest(any(ICommand.class)))
          .thenReturn(mockCreateResponse)
          .thenReturn(mockActivateResponse);
      when(mockDoctor.getClient()).thenReturn(mockERPClient);
    }
    var prescribeRequest = new PrescribeRequest();
    MvoData mvoData = new MvoData();
    mvoData.setNumerator(12);
    mvoData.setDenominator(11);
    prescribeRequest.getMedication().setMvoData(mvoData);

    try (val response = PrescribeUseCase.issuePrescription(mockDoctor, prescribeRequest)) {
      fail(
          "PrescribeUseCase did not throw the expected Exception and answered with "
              + response.getStatus());
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(400, wae.getResponse().getStatus());
      assertEquals(ErrorResponse.class, wae.getResponse().getEntity().getClass());
      assertTrue(
          ((ErrorResponse) wae.getResponse().getEntity())
              .getMessage()
              .contains("MVO Data is invalid"));
    }
  }

  @ParameterizedTest(
      name = "#{index} - Detect Error while parsing FHIR-Bundle with directAssignment={0}")
  @NullSource // pass a null value
  @ValueSource(booleans = {true, false})
  void shouldReturn400OnInvalidFhirBundle(Boolean directAssignment) {
    val fhir = new FhirParser();
    val mockDoctor = mock(Doctor.class);
    val mockClient = mock(ErpClient.class);
    when(mockDoctor.getClient()).thenReturn(mockClient);
    when(mockClient.getFhir()).thenReturn(fhir);

    val invalidFhir = "hello world";
    Supplier<Response> responseSupplier;
    if (directAssignment != null) {
      responseSupplier =
          () -> PrescribeUseCase.issuePrescription(mockDoctor, invalidFhir, directAssignment);
    } else {
      responseSupplier = () -> PrescribeUseCase.issuePrescription(mockDoctor, invalidFhir);
    }
    try (val response = responseSupplier.get()) {
      fail(
          "PrescribeUseCase did not throw the expected Exception and answered with "
              + response.getStatus());
    } catch (WebApplicationException wae) {
      assertEquals(WebApplicationException.class, wae.getClass());
      assertEquals(400, wae.getResponse().getStatus());
      assertEquals(ErrorResponse.class, wae.getResponse().getEntity().getClass());
      assertTrue(
          ((ErrorResponse) wae.getResponse().getEntity())
              .getMessage()
              .contains("Given KBV Bundle cannot be parsed"));
    }
  }

  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.throwsInvocationTargetException(PrescribeUseCase.class));
  }

  @BeforeAll
  static void setUp() {
    val fakeUrl = "https://local...";
    doctorData = new DoctorData();
    doctorData.setType("Doctor");
    doctorData.setId("2134");
  }
}
