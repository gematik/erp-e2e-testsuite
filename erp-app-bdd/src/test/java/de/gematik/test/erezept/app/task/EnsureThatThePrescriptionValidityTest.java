/*
 * Copyright 2023 gematik GmbH
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

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionTechnicalInformation;
import de.gematik.test.erezept.app.mocker.KbvBundleDummyFactory;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import java.time.LocalDate;
import java.util.Date;
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

class EnsureThatThePrescriptionValidityTest {

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
            prescriptionId, MultiplePrescriptionExtension.asNonMultiple());
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
    when(app.getText(PrescriptionDetails.PRESCRIPTION_VALIDITY_TEXT))
        .thenReturn("Noch 2 Tage einlösbar");

    val ensureTask =
        EnsureThatThePrescriptionValidity.fromStack("letzte").isStillValidForRemainingDays(2);
    assertDoesNotThrow(() -> actor.attemptsTo(ensureTask));
  }
}
