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
import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createOperationOutcome;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actors.*;
import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import java.util.*;
import lombok.*;
import org.junit.jupiter.api.*;
import org.mockito.stubbing.*;

class ResponseOfClosePrescriptionAsBundleTest {

  @Test
  void shouldDispenseWithManipulatedValues() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val useErpClient = mock(UseTheErpClient.class);
    val useSmcb = mock(UseSMCB.class);

    pharmacy.can(useErpClient);
    pharmacy.can(useSmcb);

    val manipulatedPerformerId = "I don't care";
    val manipulatedPrescriptionId = PrescriptionId.random();
    val manipulatedKvnr = KVNR.from("Z123123123");

    doAnswer(
            (Answer<ErpResponse<ErxReceipt>>)
                invovation -> {
                  val args = invovation.getArguments();
                  val cmd = (CloseTaskCommand) args[0];
                  assertTrue(cmd.getRequestBody().isPresent());
                  val medDisp = (ErxMedicationDispense) cmd.getRequestBody().orElseThrow();
                  assertEquals(manipulatedPerformerId, medDisp.getPerformerIdFirstRep());
                  assertEquals(manipulatedKvnr, medDisp.getSubjectId());
                  assertEquals(manipulatedPrescriptionId, medDisp.getPrescriptionId());

                  return ErpResponse.forPayload(createOperationOutcome(), ErxReceipt.class)
                      .withStatusCode(404)
                      .withHeaders(Map.of())
                      .andValidationResult(createEmptyValidationResult());
                })
        .when(useErpClient)
        .request(any(CloseTaskCommand.class));

    val mockAcceptBundle = mock(ErxAcceptBundle.class);
    val mockResponse = (ErpResponse<ErxAcceptBundle>) mock(ErpResponse.class);
    val mockAcceptInteraction = new ErpInteraction<>(mockResponse);
    val mockTask = mock(ErxTask.class);

    when(mockResponse.getExpectedResource()).thenReturn(mockAcceptBundle);
    when(mockAcceptBundle.getTaskId()).thenReturn(TaskId.from("1234567890"));
    when(mockAcceptBundle.getSecret()).thenReturn(new Secret("secret"));
    when(mockAcceptBundle.getKbvBundleAsString()).thenReturn("EMPTY");
    when(mockAcceptBundle.getTask()).thenReturn(mockTask);
    when(mockTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
    when(mockTask.getForKvnr()).thenReturn(Optional.of(KVNR.from("X123456789")));
    when(useErpClient.decode(eq(KbvErpBundle.class), any()))
        .thenReturn(KbvErpBundleFaker.builder().withKvnr(KVNR.from("X123456789")).fake());
    when(useSmcb.getTelematikID()).thenReturn("Telematik-ID");

    pharmacy.performs(
        ClosePrescription.alternative()
            .performer(manipulatedPerformerId)
            .kvnr(manipulatedKvnr)
            .prescriptionId(manipulatedPrescriptionId)
            .acceptedWith(mockAcceptInteraction));
  }

  @Test
  void shouldDispenseWithPreparedDateAndHandedOver() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val useErpClient = mock(UseTheErpClient.class);
    val useSmcb = mock(UseSMCB.class);

    pharmacy.can(useErpClient);
    pharmacy.can(useSmcb);

    val mockAcceptBundle = mock(ErxAcceptBundle.class);
    val mockResponse = (ErpResponse<ErxAcceptBundle>) mock(ErpResponse.class);
    val mockAcceptInteraction = new ErpInteraction<>(mockResponse);
    val mockTask = mock(ErxTask.class);

    when(mockResponse.getExpectedResource()).thenReturn(mockAcceptBundle);
    when(mockAcceptBundle.getTaskId()).thenReturn(TaskId.from("1234567890"));
    when(mockAcceptBundle.getSecret()).thenReturn(new Secret("secret"));
    when(mockAcceptBundle.getKbvBundleAsString()).thenReturn("EMPTY");
    when(mockAcceptBundle.getTask()).thenReturn(mockTask);
    when(mockTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
    when(mockTask.getForKvnr()).thenReturn(Optional.of(KVNR.from("X123456789")));
    when(useErpClient.decode(eq(KbvErpBundle.class), any()))
        .thenReturn(KbvErpBundleFaker.builder().withKvnr(KVNR.from("X123456789")).fake());
    when(useSmcb.getTelematikID()).thenReturn("Telematik-ID");
    assertDoesNotThrow(
        () -> {
          pharmacy.performs(
              ClosePrescription.alternative()
                  .acceptedWith(mockAcceptInteraction, new Date(), new Date()));
        });
  }

  @Test
  void shouldDispenseWithPreparedDate() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val useErpClient = mock(UseTheErpClient.class);
    val useSmcb = mock(UseSMCB.class);

    pharmacy.can(useErpClient);
    pharmacy.can(useSmcb);

    val mockAcceptBundle = mock(ErxAcceptBundle.class);
    val mockResponse = (ErpResponse<ErxAcceptBundle>) mock(ErpResponse.class);
    val mockAcceptInteraction = new ErpInteraction<>(mockResponse);
    val mockTask = mock(ErxTask.class);

    when(mockResponse.getExpectedResource()).thenReturn(mockAcceptBundle);
    when(mockAcceptBundle.getTaskId()).thenReturn(TaskId.from("1234567890"));
    when(mockAcceptBundle.getSecret()).thenReturn(new Secret("secret"));
    when(mockAcceptBundle.getKbvBundleAsString()).thenReturn("EMPTY");
    when(mockAcceptBundle.getTask()).thenReturn(mockTask);
    when(mockTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
    when(mockTask.getForKvnr()).thenReturn(Optional.of(KVNR.from("X123456789")));
    when(useErpClient.decode(eq(KbvErpBundle.class), any()))
        .thenReturn(KbvErpBundleFaker.builder().withKvnr(KVNR.from("X123456789")).fake());
    when(useSmcb.getTelematikID()).thenReturn("Telematik-ID");
    assertDoesNotThrow(
        () -> {
          pharmacy.performs(
              ClosePrescription.alternative().acceptedWith(mockAcceptInteraction, new Date()));
        });
  }

  @Test
  void shouldDispenseWithoutPreparedDateOrHandedOver() {
    val pharmacy = new PharmacyActor("Am Flughafen");
    val useErpClient = mock(UseTheErpClient.class);
    val useSmcb = mock(UseSMCB.class);

    pharmacy.can(useErpClient);
    pharmacy.can(useSmcb);

    val mockAcceptBundle = mock(ErxAcceptBundle.class);
    val mockResponse = (ErpResponse<ErxAcceptBundle>) mock(ErpResponse.class);
    val mockAcceptInteraction = new ErpInteraction<>(mockResponse);
    val mockTask = mock(ErxTask.class);

    when(mockResponse.getExpectedResource()).thenReturn(mockAcceptBundle);
    when(mockAcceptBundle.getTaskId()).thenReturn(TaskId.from("1234567890"));
    when(mockAcceptBundle.getSecret()).thenReturn(new Secret("secret"));
    when(mockAcceptBundle.getKbvBundleAsString()).thenReturn("EMPTY");
    when(mockAcceptBundle.getTask()).thenReturn(mockTask);
    when(mockTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
    when(mockTask.getForKvnr()).thenReturn(Optional.of(KVNR.from("X123456789")));
    when(useErpClient.decode(eq(KbvErpBundle.class), any()))
        .thenReturn(KbvErpBundleFaker.builder().withKvnr(KVNR.from("X123456789")).fake());
    when(useSmcb.getTelematikID()).thenReturn("Telematik-ID");
    assertDoesNotThrow(
        () -> {
          pharmacy.performs(ClosePrescription.alternative().acceptedWith(mockAcceptInteraction));
        });
  }
}
