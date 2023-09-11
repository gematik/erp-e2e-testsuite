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

package de.gematik.test.erezept.app.questions;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.exceptions.AppStateMissmatchException;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import java.util.Map;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
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
    givenThat(theAppUser).can(mock(UseTheErpClient.class));
    givenThat(theAppUser).can(ManageDataMatrixCodes.sheGetsPrescribed());
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldThrowOnAppStateMissmatch() {
    val actor = OnStage.theActorCalled(userName);
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);
    
    val taskId = TaskId.from(PrescriptionId.random());
    val accessCode = AccessCode.random();
    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, accessCode));

    val kbvBundle = mock(KbvErpBundle.class);
    val task = mock(ErxTask.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    val medicationRequest = mock(KbvErpMedicationRequest.class);
    when(task.getStatus()).thenReturn(Task.TaskStatus.READY);
    when(prescriptionBundle.getTask()).thenReturn(task);
    when(prescriptionBundle.getKbvBundle()).thenReturn(kbvBundle);
    when(kbvBundle.getMedicationName()).thenReturn("Schmerzmittel");
    when(kbvBundle.getMedicationRequest()).thenReturn(medicationRequest);
    when(medicationRequest.isMultiple()).thenReturn(false);
    when(kbvBundle.isMultiple()).thenReturn(false);
    
    val getTaskResponse =
            ErpResponse.forPayload(prescriptionBundle, ErxPrescriptionBundle.class)
                    .withHeaders(Map.of())
                    .withStatusCode(200)
                    .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    
    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);

    val movingTo =
        MovingToPrescription.fromStack("ausgestellt")
            .withDeque("erstes");
    
    assertThrows(AppStateMissmatchException.class, () -> actor.asksFor(movingTo));
  }
}
