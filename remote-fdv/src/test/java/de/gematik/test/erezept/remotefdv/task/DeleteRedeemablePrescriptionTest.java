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

package de.gematik.test.erezept.remotefdv.task;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.remotefdv.abilities.UseTheRemoteFdVClient;
import de.gematik.test.erezept.remotefdv.client.FdVResponse;
import de.gematik.test.erezept.remotefdv.client.PatientRequests;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import java.util.List;
import java.util.Optional;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.Prescription;

class DeleteRedeemablePrescriptionTest {
  private String userName;

  @BeforeEach
  void setup() {
    OnStage.setTheStage(new Cast() {});
    userName = GemFaker.fakerName();
    val theAppUser = OnStage.theActorCalled(userName);
    val useTheMockRemoteFdVClient = mock(UseTheRemoteFdVClient.class);

    givenThat(theAppUser).can(useTheMockRemoteFdVClient);
    givenThat(theAppUser).can(ManageDataMatrixCodes.sheGetsPrescribed());
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldDeleteRedeemablePrescription() {
    val deletePrescriptionTask = DeleteRedeemablePrescription.fromStack("letzte");

    val actor = OnStage.theActorCalled(userName);
    val taskId = TaskId.from(PrescriptionId.random());
    val dmcAbility = actor.abilityTo(ManageDataMatrixCodes.class);
    val dmc = DmcPrescription.ownerDmc(taskId, AccessCode.random());
    dmcAbility.appendDmc(dmc);

    val remoteFdV = actor.abilityTo(UseTheRemoteFdVClient.class);
    val prescription = new Prescription();
    prescription.setStatus(Prescription.StatusEnum.READY);
    val response = new FdVResponse<Prescription>();
    response.setExpectedResource(List.of(prescription));
    val emptyResponse = mock(FdVResponse.class);

    when(emptyResponse.getResourceOptional()).thenReturn(Optional.empty());
    when(remoteFdV.sendRequest(PatientRequests.getPrescriptionById(any())))
        .thenReturn(response)
        .thenReturn(emptyResponse);
    assertDoesNotThrow(() -> deletePrescriptionTask.performAs(actor));
  }
}
