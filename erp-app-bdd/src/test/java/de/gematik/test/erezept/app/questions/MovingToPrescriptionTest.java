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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.app.questions;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.*;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.exceptions.AppStateMissmatchException;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.app.mocker.KbvBundleDummyFactory;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.kbv.*;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import java.util.Map;
import java.util.Optional;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MovingToPrescriptionTest extends ErpFhirParsingTest {

  private String userName;

  @BeforeEach
  void init() {
    OnStage.setTheStage(new Cast() {});

    val appAbility = mock(UseIOSApp.class);
    when(appAbility.getPlatformType()).thenReturn(PlatformType.IOS);

    // assemble the screenplay
    userName = GemFaker.fakerName();
    val theAppUser = OnStage.theActorCalled(userName);
    givenThat(theAppUser).can(appAbility);
    val erpClient = mock(ErpClient.class);
    val erpClientAbility = mock(UseTheErpClient.class);
    when(erpClientAbility.getClient()).thenReturn(erpClient);
    givenThat(theAppUser).can(erpClientAbility);
    givenThat(theAppUser).can(ManageDataMatrixCodes.sheGetsPrescribed());

    // make sure the teardown does not run into an NPE
    val mockResponse =
        ErpResponse.forPayload(createOperationOutcome(), EmptyResource.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(erpClient.request(any(TaskAbortCommand.class))).thenReturn(mockResponse);
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldThrowOnAppStateMismatch(boolean testsEVDGA) {
    val actor = OnStage.theActorCalled(userName);
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);

    val flowType =
        testsEVDGA ? PrescriptionFlowType.FLOW_TYPE_162 : PrescriptionFlowType.FLOW_TYPE_160;
    val prescriptionId = PrescriptionId.random(flowType);
    val taskId = TaskId.from(prescriptionId);

    val accessCode = AccessCode.random();
    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, accessCode));

    val kbvBundle =
        KbvBundleDummyFactory.createSimpleKbvBundle(
            prescriptionId,
            StatusCoPayment.STATUS_0,
            MultiplePrescriptionExtension.asNonMultiple());

    val evdgaBundle = mock(KbvEvdgaBundle.class);
    val task = mock(ErxTask.class);
    val medication = mock(KbvErpMedication.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    val medicationRequest = mock(KbvErpMedicationRequest.class);
    val healthAppRequest = mock(KbvHealthAppRequest.class);

    // EVDGA-Bundle
    when(prescriptionBundle.getEvdgaBundle()).thenReturn(Optional.of(evdgaBundle));
    when(evdgaBundle.getHealthAppRequest()).thenReturn(healthAppRequest);
    when(healthAppRequest.getName()).thenReturn("EVIDA Gesund-App");
    // KBV-Bundle
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));
    when(medication.getMedicationName()).thenReturn("Schmerzmittel");
    when(medicationRequest.isMultiple()).thenReturn(false);
    when(prescriptionBundle.getTask()).thenReturn(task);

    val getTaskResponse =
        ErpResponse.forPayload(prescriptionBundle, ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(200)
            .andValidationResult(createEmptyValidationResult());

    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);

    val movingTo = MovingToPrescription.withTaskId(taskId);

    assertThrows(AppStateMissmatchException.class, () -> actor.asksFor(movingTo));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldThrowOnMissingBundle(boolean testsEVDGABundle) {
    val actor = OnStage.theActorCalled(userName);
    val erpClient = actor.abilityTo(UseTheErpClient.class);

    val flowType =
        testsEVDGABundle ? PrescriptionFlowType.FLOW_TYPE_162 : PrescriptionFlowType.FLOW_TYPE_160;
    val taskId = TaskId.from(PrescriptionId.random(flowType));

    val task = mock(ErxTask.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);

    when(task.getStatus()).thenReturn(Task.TaskStatus.READY);
    when(prescriptionBundle.getTask()).thenReturn(task);
    when(prescriptionBundle.getEvdgaBundle()).thenReturn(Optional.empty());
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.empty());

    val getTaskResponse =
        ErpResponse.forPayload(prescriptionBundle, ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(200)
            .andValidationResult(createEmptyValidationResult());

    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);

    val movingTo = MovingToPrescription.withTaskId(taskId);

    assertThrows(MissingFieldException.class, () -> actor.asksFor(movingTo));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldThrowWhenFindPrescriptionWhichIsNotInBackend(boolean testsEVDGA) {
    val actor = OnStage.theActorCalled(userName);
    val appAbility = actor.abilityTo(UseIOSApp.class);
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);

    val flowType =
        testsEVDGA ? PrescriptionFlowType.FLOW_TYPE_162 : PrescriptionFlowType.FLOW_TYPE_160;
    val taskId = TaskId.from(PrescriptionId.random(flowType));
    val accessCode = AccessCode.random();
    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, accessCode));

    val getTaskResponse =
        ErpResponse.forPayload(createOperationOutcome(), ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(404)
            .andValidationResult(createEmptyValidationResult());

    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);

    when(appAbility.isDisplayed(EVDGADetails.DIGA_TITLE)).thenReturn(testsEVDGA);
    when(appAbility.getText(PrescriptionTechnicalInformation.TASKID)).thenReturn(taskId.getValue());
    when(appAbility.getText(EVDGATechnicalInformation.TASKID)).thenReturn(taskId.getValue());
    when(appAbility.getWebElementListLen(any(PrescriptionsViewElement.class))).thenReturn(1);

    when(appAbility.isDisplayed(EVDGADetails.DISMISS_FHIR_VZD_DIALOG)).thenReturn(testsEVDGA);

    val movingTo = MovingToPrescription.withTaskId(taskId);

    assertThrows(AppStateMissmatchException.class, () -> actor.asksFor(movingTo));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldPassWhenPrescriptionIsNotFoundAnywhere(boolean testsEVDGA) {
    val actor = OnStage.theActorCalled(userName);
    val appAbility = actor.abilityTo(UseIOSApp.class);
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);

    val flowType =
        testsEVDGA ? PrescriptionFlowType.FLOW_TYPE_162 : PrescriptionFlowType.FLOW_TYPE_160;
    val taskId = TaskId.from(PrescriptionId.random(flowType));
    val accessCode = AccessCode.random();
    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, accessCode));

    val getTaskResponse =
        ErpResponse.forPayload(createOperationOutcome(), ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(404)
            .andValidationResult(createEmptyValidationResult());

    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);
    when(appAbility.isDisplayed(EVDGADetails.DIGA_TITLE)).thenReturn(testsEVDGA);
    when(appAbility.getText(PrescriptionTechnicalInformation.TASKID))
        .thenReturn(PrescriptionId.random().getValue())
        .thenReturn(PrescriptionId.random().getValue());
    when(appAbility.getText(EVDGATechnicalInformation.TASKID))
        .thenReturn(PrescriptionId.random().getValue())
        .thenReturn(PrescriptionId.random().getValue());

    when(appAbility.isDisplayed(EVDGADetails.DISMISS_FHIR_VZD_DIALOG)).thenReturn(testsEVDGA);

    val movingTo = MovingToPrescription.withTaskId(taskId.getValue());

    assertDoesNotThrow(() -> actor.asksFor(movingTo));
  }
}
