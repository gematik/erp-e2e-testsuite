/*
 *
 *  * Copyright (c) 2022 gematik GmbH
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the License);
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * 
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an 'AS IS' BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package de.gematik.test.erezept.primsys.model;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import de.gematik.test.erezept.fhir.values.TelematikID;
import de.gematik.test.erezept.primsys.model.actor.Pharmacy;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import lombok.val;
import org.hl7.fhir.r4.model.AuditEvent;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class RejectUseCaseTest {

  @Test
  void constructorShouldNotBeCallable() throws NoSuchMethodException {
    val constructor = RejectUseCase.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    assertThrows(InvocationTargetException.class, constructor::newInstance);
  }

  @Test
  void shouldRejectPrescription() {
    try (MockedStatic<ActorContext> mockedStaticActor = mockStatic(ActorContext.class)) {
      val mockActorContext = mock(ActorContext.class);
      mockedStaticActor.when(ActorContext::getInstance).thenReturn(mockActorContext);
      val mockPharmacyActor = mock(Pharmacy.class);
      val resource =
          FhirTestResourceUtil.createErxAuditEvent(
              "testString", TelematikID.from("123"), "testName", AuditEvent.AuditEventAction.R);
      when(mockPharmacyActor.erpRequest(any()))
          .thenReturn(new ErpResponse(204, Map.of(), resource));
      try (var response =
          RejectUseCase.rejectPrescription(
              mockPharmacyActor, "taskId", "accessCode", "verySecret")) {
        assertEquals(204, response.getStatus());
      }
    }
  }

  @Test
  void shouldThrowIsOperationOutcome() {
    try (MockedStatic<ActorContext> mockedStaticActor = mockStatic(ActorContext.class)) {
      val mockActorContext = mock(ActorContext.class);
      mockedStaticActor.when(ActorContext::getInstance).thenReturn(mockActorContext);
      val operationOutcome = FhirTestResourceUtil.createOperationOutcome();
      val mockPharmacyActor = mock(Pharmacy.class);
      when(mockPharmacyActor.erpRequest(any()))
          .thenReturn(new ErpResponse(500, Map.of(), operationOutcome));

      try (val response =
          RejectUseCase.rejectPrescription(
              mockPharmacyActor, "taskId", "accessCode", "verySecret")) {
        fail(
            format(
                "RejectUseCase did not throw the expected Exception and answered with ",
                response.getStatus()));
      } catch (WebApplicationException wae) {
        assertEquals(WebApplicationException.class, wae.getClass());
        assertEquals(500, wae.getResponse().getStatus());
        assertEquals(ErrorResponse.class, wae.getResponse().getEntity().getClass());
      }
    }
  }

  @Test
  void shouldThrowWebApplicationWith500andNull() {
    try (MockedStatic<ActorContext> mockedStaticActor = mockStatic(ActorContext.class)) {
      val mockActorContext = mock(ActorContext.class);
      mockedStaticActor.when(ActorContext::getInstance).thenReturn(mockActorContext);
      val mockPharmacyActor = mock(Pharmacy.class);
      when(mockPharmacyActor.erpRequest(any())).thenReturn(new ErpResponse(500, Map.of(), null));

      try (val response =
          RejectUseCase.rejectPrescription(
              mockPharmacyActor, "taskId", "accessCode", "verySecret")) {
        fail(
            format(
                "RejectUseCase did not throw the expected Exception and answered with ",
                response.getStatus()));
      } catch (WebApplicationException wae) {
        assertEquals(WebApplicationException.class, wae.getClass());
        assertEquals(500, wae.getResponse().getStatus());
        assertEquals(ErrorResponse.class, wae.getResponse().getEntity().getClass());
      }
    }
  }

  @Test
  void shouldThrowGetStatusCodeIsBigger299() {
    try (MockedStatic<ActorContext> mockedStaticActor = mockStatic(ActorContext.class)) {
      val mockActorContext = mock(ActorContext.class);
      mockedStaticActor.when(ActorContext::getInstance).thenReturn(mockActorContext);
      val operationOutcome = FhirTestResourceUtil.createOperationOutcome();
      val mockPharmacyActor = mock(Pharmacy.class);
      when(mockPharmacyActor.erpRequest(any()))
          .thenReturn(new ErpResponse(400, Map.of(), operationOutcome));

      try (val response =
          RejectUseCase.rejectPrescription(
              mockPharmacyActor, "taskId", "accessCode", "verySecret")) {
        fail(
            format(
                "RejectUseCase did not throw the expected Exception and answered with ",
                response.getStatus()));
      } catch (WebApplicationException wae) {
        assertEquals(WebApplicationException.class, wae.getClass());
        assertEquals(400, wae.getResponse().getStatus());
        assertEquals(ErrorResponse.class, wae.getResponse().getEntity().getClass());
      }
    }
  }
}
