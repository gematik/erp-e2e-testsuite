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

package de.gematik.test.erezept.app.questions;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

import de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.exceptions.AppStateMissmatchException;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionTechnicalInformation;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import java.util.Map;
import java.util.Optional;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MovingToPrescriptionTest {

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
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), Resource.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(erpClient.request(any(TaskAbortCommand.class))).thenReturn(mockResponse);
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldThrowOnAppStateMismatch() {
    val actor = OnStage.theActorCalled(userName);
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);

    val taskId = TaskId.from(PrescriptionId.random());
    val accessCode = AccessCode.random();
    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, accessCode));

    val kbvBundle = mock(KbvErpBundle.class);
    val task = mock(ErxTask.class);
    val medication = mock(KbvErpMedication.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    val medicationRequest = mock(KbvErpMedicationRequest.class);
    when(task.getStatus()).thenReturn(Task.TaskStatus.READY);
    when(prescriptionBundle.getTask()).thenReturn(task);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));
    when(kbvBundle.getMedication()).thenReturn(medication);
    when(medication.getMedicationName()).thenReturn("Schmerzmittel");
    when(kbvBundle.getMedicationRequest()).thenReturn(medicationRequest);
    when(medicationRequest.isMultiple()).thenReturn(false);
    when(kbvBundle.getMedicationRequest().isMultiple()).thenReturn(false);

    val getTaskResponse =
        ErpResponse.forPayload(prescriptionBundle, ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(200)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);

    val movingTo = MovingToPrescription.withTaskId(taskId);

    assertThrows(AppStateMissmatchException.class, () -> actor.asksFor(movingTo));
  }

  @Test
  void shouldThrowOnMissingKbvBundle() {
    val actor = OnStage.theActorCalled(userName);
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);

    val taskId = TaskId.from(PrescriptionId.random());
    val accessCode = AccessCode.random();
    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, accessCode));

    val task = mock(ErxTask.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);

    when(task.getStatus()).thenReturn(Task.TaskStatus.READY);
    when(prescriptionBundle.getTask()).thenReturn(task);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.empty());

    val getTaskResponse =
        ErpResponse.forPayload(prescriptionBundle, ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(200)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);

    val movingTo = MovingToPrescription.withTaskId(taskId);

    assertThrows(MissingFieldException.class, () -> actor.asksFor(movingTo));
  }

  @Test
  void shouldThrowWhenFindPrescriptionWhichIsNotInBackend() {
    val actor = OnStage.theActorCalled(userName);
    val appAbility = actor.abilityTo(UseIOSApp.class);
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);

    val taskId = TaskId.from(PrescriptionId.random());
    val accessCode = AccessCode.random();
    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, accessCode));

    val getTaskResponse =
        ErpResponse.forPayload(
                FhirTestResourceUtil.createOperationOutcome(), ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(404)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);
    when(appAbility.getText(PrescriptionTechnicalInformation.TASKID)).thenReturn(taskId.getValue());

    val movingTo = MovingToPrescription.withTaskId(taskId);

    assertThrows(AppStateMissmatchException.class, () -> actor.asksFor(movingTo));
  }

  @Test
  void shouldPassWhenPrescriptionIsNotFoundAnywhere() {
    val actor = OnStage.theActorCalled(userName);
    val appAbility = actor.abilityTo(UseIOSApp.class);
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);

    val taskId = TaskId.from(PrescriptionId.random());
    val accessCode = AccessCode.random();
    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, accessCode));

    val getTaskResponse =
        ErpResponse.forPayload(
                FhirTestResourceUtil.createOperationOutcome(), ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(404)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);
    when(appAbility.getText(PrescriptionTechnicalInformation.TASKID))
        .thenReturn(PrescriptionId.random().getValue())
        .thenReturn(PrescriptionId.random().getValue());

    val movingTo = MovingToPrescription.withTaskId(taskId.getValue());

    assertDoesNotThrow(() -> actor.asksFor(movingTo));
  }
}
