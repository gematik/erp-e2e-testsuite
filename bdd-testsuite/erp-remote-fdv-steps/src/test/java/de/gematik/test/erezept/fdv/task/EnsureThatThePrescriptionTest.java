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

package de.gematik.test.erezept.fdv.task;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceGoneException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import de.gematik.erezept.remotefdv.api.model.Error;
import de.gematik.erezept.remotefdv.api.model.Prescription;
import de.gematik.erezept.remotefdv.api.model.WorkFlow;
import de.gematik.test.erezept.fdv.abilities.UseTheRemoteFdVClient;
import de.gematik.test.erezept.fdv.exceptions.GatewayTimeOutException;
import de.gematik.test.erezept.fdv.exceptions.UnexpectedErrorException;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.remotefdv.client.FdVResponse;
import de.gematik.test.erezept.remotefdv.client.requests.GetPrescriptionById;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnsureThatThePrescriptionTest {
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
  void shouldEnsureThatThePrescriptionIsThere() {
    val workFlows = Arrays.asList(WorkFlow._160, WorkFlow._169, WorkFlow._200, WorkFlow._209);
    for (WorkFlow workFlow : workFlows) {
      val actor = OnStage.theActorCalled(userName);
      val remoteFdV = actor.abilityTo(UseTheRemoteFdVClient.class);

      val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);

      val prescriptionId =
          PrescriptionId.random(PrescriptionFlowType.fromCode(workFlow.getValue()));
      val taskId = TaskId.from(prescriptionId);
      dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, AccessCode.random()));
      val response = new FdVResponse<Prescription>();
      val prescription = new Prescription();
      prescription.setWorkFlow(workFlow);
      prescription.setPrescriptionId(prescriptionId.getValue());
      response.setResourcesList(List.of(prescription));
      when(remoteFdV.sendRequest(any(GetPrescriptionById.class))).thenReturn(response);
      val action = EnsureThatThePrescription.fromStack("letzte").isShownCorrectly();
      assertDoesNotThrow(() -> action.performAs(actor));
    }
  }

  @Test
  void shouldThrowGatewayTimeOutException() {
    val actor = OnStage.theActorCalled(userName);
    val remoteFdV = actor.abilityTo(UseTheRemoteFdVClient.class);
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);

    val taskId = TaskId.from(PrescriptionId.random());
    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, AccessCode.random()));
    val response = new FdVResponse<Prescription>();
    val error = new Error();
    error.setStatusCode(BigDecimal.valueOf(504));
    response.setOperationOutcome(error);
    when(remoteFdV.sendRequest(any(GetPrescriptionById.class))).thenReturn(response);
    val action = EnsureThatThePrescription.fromStack("letzte").isShownCorrectly();
    assertThrows(GatewayTimeOutException.class, () -> action.performAs(actor));
  }

  @Test
  void shouldThrowResourceGoneException() {
    val actor = OnStage.theActorCalled(userName);
    val remoteFdV = actor.abilityTo(UseTheRemoteFdVClient.class);
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);

    val taskId = TaskId.from(PrescriptionId.random());
    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, AccessCode.random()));
    val response = new FdVResponse<Prescription>();
    val error = new Error();
    error.setStatusCode(BigDecimal.valueOf(410));
    response.setOperationOutcome(error);
    when(remoteFdV.sendRequest(any(GetPrescriptionById.class))).thenReturn(response);
    val action = EnsureThatThePrescription.fromStack("letzte").isShownCorrectly();
    assertThrows(ResourceGoneException.class, () -> action.performAs(actor));
  }

  @Test
  void shouldThrowResourceNotFoundException() {
    val actor = OnStage.theActorCalled(userName);
    val remoteFdV = actor.abilityTo(UseTheRemoteFdVClient.class);
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);

    val taskId = TaskId.from(PrescriptionId.random());
    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, AccessCode.random()));
    val response = new FdVResponse<Prescription>();
    val error = new Error();
    error.setStatusCode(BigDecimal.valueOf(404));
    response.setOperationOutcome(error);
    when(remoteFdV.sendRequest(any(GetPrescriptionById.class))).thenReturn(response);
    val action = EnsureThatThePrescription.fromStack("letzte").isShownCorrectly();
    assertThrows(ResourceNotFoundException.class, () -> action.performAs(actor));
  }

  @Test
  void shouldThrowUnexpectedErrorException() {
    val actor = OnStage.theActorCalled(userName);
    val remoteFdV = actor.abilityTo(UseTheRemoteFdVClient.class);
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);

    val taskId = TaskId.from(PrescriptionId.random());
    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, AccessCode.random()));
    val response = new FdVResponse<Prescription>();
    val error = new Error();
    error.setStatusCode(BigDecimal.valueOf(405));
    response.setOperationOutcome(error);
    when(remoteFdV.sendRequest(any(GetPrescriptionById.class))).thenReturn(response);
    val action = EnsureThatThePrescription.fromStack("letzte").isShownCorrectly();
    assertThrows(UnexpectedErrorException.class, () -> action.performAs(actor));
  }
}
