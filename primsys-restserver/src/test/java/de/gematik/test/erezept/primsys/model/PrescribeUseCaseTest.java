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

import static java.text.MessageFormat.format;
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
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.primsys.model.actor.Doctor;
import de.gematik.test.erezept.primsys.rest.data.DoctorData;
import de.gematik.test.erezept.primsys.rest.data.MvoData;
import de.gematik.test.erezept.primsys.rest.data.TelematikData;
import de.gematik.test.erezept.primsys.rest.request.PrescribeRequest;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class PrescribeUseCaseTest {

  static DoctorData doctorData;

  @BeforeAll
  static void setUp() {
    val ti = new TelematikData();
    String fakeUrl = "https://local...";
    ti.setFachdienst(fakeUrl);
    ti.setTsl(fakeUrl);
    ti.setDiscoveryDocument(fakeUrl);
    doctorData = new DoctorData();
    doctorData.setType("Doctor");
    doctorData.setId("2134");
    doctorData.setTi(ti);
  }

  @SuppressWarnings({"unchecked"})
  @SneakyThrows
  @Test
  void testIssuePrescriptionDoctorAndPrescribeRequest() {
    val mockCreateResponse = mock(ErpResponse.class);
    val mockActivateResponse = mock(ErpResponse.class);
    val mockErxTask = mock(ErxTask.class);
    val mockERPClient = mock(ErpClient.class);
    val accessCode = AccessCode.random();
    val taskId = "test Id as String";
    try (MockedStatic<ActorContext> mockedStaticActor = mockStatic(ActorContext.class)) {
      val mock = mock(ActorContext.class);
      mockedStaticActor.when(ActorContext::getInstance).thenReturn(mock);
      when(mockERPClient.encode(any(), any())).thenReturn("HelloMockClient");
      when(mockErxTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
      when(mockErxTask.getUnqualifiedId()).thenReturn(taskId);
      when(mockErxTask.getOptionalAccessCode()).thenReturn(Optional.of(accessCode));
      when(mockErxTask.getAuthoredOn()).thenReturn(new Date());
      when(mockErxTask.getAccessCode()).thenReturn(accessCode);
      when(mockCreateResponse.getResourceOptional(any())).thenReturn(Optional.of(mockErxTask));
      when(mockCreateResponse.getResource(any())).thenReturn(mockErxTask);
      when(mockActivateResponse.getResourceOptional(any())).thenReturn(Optional.of(mockErxTask));
      val mockDoctor = mock(Doctor.class);
      when(mockDoctor.signDocument(anyString())).thenReturn("signedDocument".getBytes());
      when(mockDoctor.getName()).thenReturn("Doc Doolittle");
      when(mockDoctor.getBaseData()).thenReturn(doctorData);
      when(mockDoctor.erpRequest(any()))
          .thenReturn(mockCreateResponse)
          .thenReturn(mockActivateResponse);
      when(mockDoctor.getClient()).thenReturn(mockERPClient);
      var prescribeRequest = new PrescribeRequest();
      final Map<String, String> resMap;
      try (val response = PrescribeUseCase.issuePrescription(mockDoctor, prescribeRequest)) {
        resMap = (Map<String, String>) response.getEntity();
        assertTrue(response.hasEntity());
      }
      assertEquals(taskId, resMap.get("task-id"));
      assertEquals(accessCode.getValue(), resMap.get("access-code"));
    }
  }

  @SneakyThrows
  @Test
  void issuePrescriptionShouldThrowWebAppException() {
    val mockDoctor = mock(Doctor.class);
    val mockPrescribeRequest = mock(PrescribeRequest.class);
    when(mockPrescribeRequest.getPatient()).thenReturn(null);

    try (val response = PrescribeUseCase.issuePrescription(mockDoctor, mockPrescribeRequest)) {
      fail(
          format(
              "PrescribeUseCase did not throw the expected Exception and answered with ",
              response.getStatus()));
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
  void getMedicationRequestIfMvoIsNotNull() {
    val mockCreateResponse = mock(ErpResponse.class);
    val mockActivateResponse = mock(ErpResponse.class);
    val mockErxTask = mock(ErxTask.class);
    val mockERPClient = mock(ErpClient.class);
    val accessCode = AccessCode.random();
    val taskId = "test Id as String";
    try (MockedStatic<ActorContext> mockedStaticActor = mockStatic(ActorContext.class)) {
      val mock = mock(ActorContext.class);
      mockedStaticActor.when(ActorContext::getInstance).thenReturn(mock);
      when(mockERPClient.encode(any(), any())).thenReturn("HelloMockClient");
      when(mockErxTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
      when(mockErxTask.getUnqualifiedId()).thenReturn(taskId);
      when(mockErxTask.getOptionalAccessCode()).thenReturn(Optional.of(accessCode));
      when(mockErxTask.getAuthoredOn()).thenReturn(new Date());
      when(mockErxTask.getAccessCode()).thenReturn(accessCode);
      when(mockCreateResponse.getResourceOptional(any())).thenReturn(Optional.of(mockErxTask));
      when(mockCreateResponse.getResource(any())).thenReturn(mockErxTask);
      when(mockActivateResponse.getResourceOptional(any())).thenReturn(Optional.of(mockErxTask));
      val mockDoctor = mock(Doctor.class);
      when(mockDoctor.signDocument(anyString())).thenReturn("signedDocument".getBytes());
      when(mockDoctor.getName()).thenReturn("Doc Doolittle");
      when(mockDoctor.getBaseData()).thenReturn(doctorData);
      when(mockDoctor.erpRequest(any()))
          .thenReturn(mockCreateResponse)
          .thenReturn(mockActivateResponse);
      when(mockDoctor.getClient()).thenReturn(mockERPClient);
      var prescribeRequest = new PrescribeRequest();
      MvoData mvoData = new MvoData();
      prescribeRequest.getMedication().setMvoData(mvoData.fakeMvoNumeAndDenomi());
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
      when(mockErxTask.getUnqualifiedId()).thenReturn(taskId);
      when(mockErxTask.getOptionalAccessCode()).thenReturn(Optional.of(accessCode));
      when(mockErxTask.getAuthoredOn()).thenReturn(new Date());
      when(mockErxTask.getAccessCode()).thenReturn(accessCode);
      when(mockCreateResponse.getResourceOptional(any())).thenReturn(Optional.of(mockErxTask));
      when(mockCreateResponse.getResource(any())).thenReturn(mockErxTask);
      when(mockActivateResponse.getResourceOptional(any())).thenReturn(Optional.of(mockErxTask));
      mockDoctor = mock(Doctor.class);
      when(mockDoctor.signDocument(anyString())).thenReturn("signedDocument".getBytes());
      when(mockDoctor.getName()).thenReturn("Doc Doolittle");
      when(mockDoctor.getBaseData()).thenReturn(doctorData);
      when(mockDoctor.erpRequest(any()))
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
          format(
              "PrescribeUseCase did not throw the expected Exception and answered with ",
              response.getStatus()));
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
}
