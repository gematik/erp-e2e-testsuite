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

package de.gematik.test.erezept.app.task;

import static java.text.MessageFormat.format;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionTechnicalInformation;
import de.gematik.test.erezept.app.mocker.KbvBundleDummyFactory;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SerenityJUnit5Extension.class)
class EnsureThatThePrescriptionTest {

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
    val erpClientAbility = mock(UseTheErpClient.class);
    givenThat(theAppUser).can(erpClientAbility);
    givenThat(theAppUser).can(ManageDataMatrixCodes.sheGetsPrescribed());

    // make sure the teardown does not run into an NPE
    val mockResponse =
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), Resource.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(erpClientAbility.request(any(TaskAbortCommand.class))).thenReturn(mockResponse);
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldEnsureSimplePrescription() {
    val actor = OnStage.theActorCalled(userName);
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val app = actor.abilityTo(UseIOSApp.class);
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);

    val prescriptionId = PrescriptionId.random();
    val taskId = TaskId.from(prescriptionId);
    val accessCode = AccessCode.random();

    val kbvBundle =
        KbvBundleDummyFactory.createSimpleKbvBundle(
            prescriptionId,
            StatusCoPayment.STATUS_0,
            MultiplePrescriptionExtension.asNonMultiple());
    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, accessCode));

    val task = mock(ErxTask.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    when(task.getStatus()).thenReturn(Task.TaskStatus.READY);
    when(task.getAcceptDate())
        .thenReturn(DateConverter.getInstance().localDateToDate(LocalDate.now().plusDays(3)));
    when(task.getAuthoredOn()).thenReturn(new Date());
    when(prescriptionBundle.getTask()).thenReturn(task);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));

    val getTaskResponse =
        ErpResponse.forPayload(prescriptionBundle, ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(200)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);

    when(app.getWebElementListLen(any())).thenReturn(2);
    when(app.getText(PrescriptionTechnicalInformation.TASKID)).thenReturn(taskId.getValue());
    when(app.getText(PrescriptionDetails.PRESCRIPTION_TITLE))
        .thenReturn(kbvBundle.getMedication().getMedicationName());
    when(app.getText(PrescriptionDetails.PRESCRIPTION_VALIDITY_TEXT))
        .thenReturn("Noch 2 Tage einlösbar");
    when(app.getText(PrescriptionDetails.PRESCRIPTION_ADDITIONAL_PAYMENT)).thenReturn("Ja");

    val ensureTask = EnsureThatThePrescription.fromStack("letzte").isShownCorrectly();
    assertDoesNotThrow(() -> actor.attemptsTo(ensureTask));
  }

  @Test
  void shouldEnsureMvoPrescription() {
    val actor = OnStage.theActorCalled(userName);
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val app = actor.abilityTo(UseIOSApp.class);
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);

    val prescriptionId = PrescriptionId.random();
    val taskId = TaskId.from(prescriptionId);
    val accessCode = AccessCode.random();

    val kbvBundle =
        KbvBundleDummyFactory.createSimpleKbvBundle(
            prescriptionId,
            StatusCoPayment.STATUS_2,
            MultiplePrescriptionExtension.asMultiple(1, 4).validForDays(90));
    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, accessCode));

    val task = mock(ErxTask.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    when(task.getStatus()).thenReturn(Task.TaskStatus.READY);
    when(task.getAcceptDate())
        .thenReturn(DateConverter.getInstance().localDateToDate(LocalDate.now().plusDays(3)));
    when(task.getAuthoredOn()).thenReturn(new Date());
    when(prescriptionBundle.getTask()).thenReturn(task);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));

    val getTaskResponse =
        ErpResponse.forPayload(prescriptionBundle, ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(200)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);

    when(app.getWebElementListLen(any())).thenReturn(2);
    when(app.getText(PrescriptionTechnicalInformation.TASKID)).thenReturn(taskId.getValue());
    when(app.getText(PrescriptionDetails.PRESCRIPTION_TITLE))
        .thenReturn(kbvBundle.getMedication().getMedicationName());
    when(app.getText(PrescriptionDetails.PRESCRIPTION_VALIDITY_TEXT))
        .thenReturn("Noch 89 Tage einlösbar");
    when(app.getText(PrescriptionDetails.PRESCRIPTION_ADDITIONAL_PAYMENT)).thenReturn("Teilweise");

    val ensureTask = EnsureThatThePrescription.fromStack("letzte").isShownCorrectly();
    assertDoesNotThrow(() -> actor.attemptsTo(ensureTask));
  }

  @Test
  void shouldEnsureDirectAssignmentPrescription() {
    val actor = OnStage.theActorCalled(userName);
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val app = actor.abilityTo(UseIOSApp.class);
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);

    val prescriptionId = PrescriptionId.random(PrescriptionFlowType.FLOW_TYPE_169);
    val taskId = TaskId.from(prescriptionId);
    val accessCode = AccessCode.random();

    val kbvBundle =
        KbvBundleDummyFactory.createSimpleKbvBundle(
            prescriptionId,
            StatusCoPayment.STATUS_1,
            MultiplePrescriptionExtension.asNonMultiple());
    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, accessCode));

    val task = mock(ErxTask.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    when(task.getStatus()).thenReturn(Task.TaskStatus.READY);
    when(task.getAcceptDate())
        .thenReturn(DateConverter.getInstance().localDateToDate(LocalDate.now().plusDays(3)));
    when(task.getAuthoredOn()).thenReturn(new Date());
    when(prescriptionBundle.getTask()).thenReturn(task);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));

    val getTaskResponse =
        ErpResponse.forPayload(prescriptionBundle, ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(200)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);

    when(app.getWebElementListLen(any())).thenReturn(2);
    when(app.getText(PrescriptionTechnicalInformation.TASKID)).thenReturn(taskId.getValue());
    when(app.getText(PrescriptionDetails.PRESCRIPTION_TITLE))
        .thenReturn(kbvBundle.getMedication().getMedicationName());
    when(app.getText(PrescriptionDetails.PRESCRIPTION_VALIDITY_TEXT))
        .thenReturn("Noch 2 Tage einlösbar");
    when(app.getText(PrescriptionDetails.PRESCRIPTION_ADDITIONAL_PAYMENT)).thenReturn("Nein");
    when(app.isPresent(PrescriptionDetails.DIRECT_ASSIGNMENT_BADGE)).thenReturn(true);

    val ensureTask = EnsureThatThePrescription.fromStack("letzte").isShownCorrectly();
    assertDoesNotThrow(() -> actor.attemptsTo(ensureTask));
  }

  @Test
  void shouldEnsureMvoPrescriptionLaterRedeemable() {
    val actor = OnStage.theActorCalled(userName);
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val app = actor.abilityTo(UseIOSApp.class);
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);

    val prescriptionId = PrescriptionId.random();
    val taskId = TaskId.from(prescriptionId);
    val accessCode = AccessCode.random();

    val kbvBundle =
        KbvBundleDummyFactory.createSimpleKbvBundle(
            prescriptionId,
            StatusCoPayment.STATUS_2,
            MultiplePrescriptionExtension.asMultiple(1, 4).starting(10).validForDays(90));
    val expectedRedemptionDate =
        DateConverter.getInstance().localDateToDate(LocalDate.now().plusDays(10));
    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, accessCode));

    val task = mock(ErxTask.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    when(task.getStatus()).thenReturn(Task.TaskStatus.READY);
    when(task.getAcceptDate())
        .thenReturn(DateConverter.getInstance().localDateToDate(LocalDate.now().plusDays(3)));
    when(task.getAuthoredOn()).thenReturn(new Date());
    when(prescriptionBundle.getTask()).thenReturn(task);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));

    val getTaskResponse =
        ErpResponse.forPayload(prescriptionBundle, ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(200)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);

    when(app.getWebElementListLen(any())).thenReturn(2);
    when(app.getText(PrescriptionTechnicalInformation.TASKID)).thenReturn(taskId.getValue());
    when(app.getText(PrescriptionDetails.PRESCRIPTION_TITLE))
        .thenReturn(kbvBundle.getMedication().getMedicationName());
    when(app.getText(PrescriptionDetails.PRESCRIPTION_VALIDITY_TEXT))
        .thenReturn(
            format(
                "Einlösbar ab {0}",
                new SimpleDateFormat("dd.MM.yyyy").format(expectedRedemptionDate)));
    when(app.getText(PrescriptionDetails.PRESCRIPTION_ADDITIONAL_PAYMENT)).thenReturn("Teilweise");

    val ensureTask = EnsureThatThePrescription.fromStack("letzte").isShownCorrectly();
    assertDoesNotThrow(() -> actor.attemptsTo(ensureTask));
  }

  @Test
  void shouldCheckForPrecondition() {
    val actor = OnStage.theActorCalled(userName);
    val ensureTask = EnsureThatThePrescription.fromStack("letzte").isShownCorrectly();
    assertThrows(MissingPreconditionError.class, () -> ensureTask.performAs(actor));
  }

  @Test
  void shouldCheckForPreconditionInBackend() {
    val actor = OnStage.theActorCalled(userName);
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val appAbility = actor.abilityTo(UseIOSApp.class);
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

    val ensureTask = EnsureThatThePrescription.fromStack("letzte").isShownCorrectly();
    assertThrows(MissingPreconditionError.class, () -> ensureTask.performAs(actor));
  }
}
