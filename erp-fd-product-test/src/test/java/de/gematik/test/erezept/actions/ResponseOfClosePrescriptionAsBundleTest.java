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

package de.gematik.test.erezept.actions;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createOperationOutcome;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.CloseTaskCommand;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.erp.GemCloseOperationParameters;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.konnektor.soap.mock.LocalSigner;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

class ResponseOfClosePrescriptionAsBundleTest extends ErpFhirBuildingTest {

  private static byte[] exampleQes;

  @BeforeAll
  public static void setUp() {
    val hba = SmartcardArchive.fromResources().getHbaByICCSN("80276001011699901501");
    exampleQes = LocalSigner.signQES(hba, CryptoSystem.ECC_256).signDocument(false, "Empty");
  }

  @Test
  @SuppressWarnings("unchecked")
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

                  if (ErpWorkflowVersion.getDefaultVersion().compareTo(ErpWorkflowVersion.V1_3_0)
                      <= 0) {
                    val medDisp = (ErxMedicationDispense) cmd.getRequestBody().orElseThrow();
                    assertEquals(manipulatedPerformerId, medDisp.getPerformerIdFirstRep());
                    assertEquals(manipulatedKvnr.getValue(), medDisp.getSubjectId().getValue());
                    assertEquals(manipulatedPrescriptionId, medDisp.getPrescriptionId());
                  } else {
                    assertInstanceOf(
                        GemCloseOperationParameters.class, cmd.getRequestBody().orElseThrow());
                  }

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
    val hba = SmartcardArchive.fromResources().getHbaByICCSN("80276001011699901501");

    when(mockResponse.getExpectedResource()).thenReturn(mockAcceptBundle);
    when(mockAcceptBundle.getTaskId()).thenReturn(TaskId.from("1234567890"));
    when(mockAcceptBundle.getSecret()).thenReturn(new Secret("secret"));
    when(mockAcceptBundle.getTask()).thenReturn(mockTask);
    when(mockAcceptBundle.getSignedKbvBundle()).thenReturn(exampleQes);
    when(mockAcceptBundle.getSignedKbvBundle())
        .thenReturn(LocalSigner.signQES(hba, CryptoSystem.ECC_256).signDocument(false, "Empty"));
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
  @SuppressWarnings("unchecked")
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
    when(mockAcceptBundle.getTask()).thenReturn(mockTask);
    when(mockAcceptBundle.getSignedKbvBundle()).thenReturn(exampleQes);
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
  @SuppressWarnings("unchecked")
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
    when(mockAcceptBundle.getTask()).thenReturn(mockTask);
    when(mockAcceptBundle.getSignedKbvBundle()).thenReturn(exampleQes);
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
  @SuppressWarnings("unchecked")
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
    when(mockAcceptBundle.getTask()).thenReturn(mockTask);
    when(mockAcceptBundle.getSignedKbvBundle()).thenReturn(exampleQes);
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
