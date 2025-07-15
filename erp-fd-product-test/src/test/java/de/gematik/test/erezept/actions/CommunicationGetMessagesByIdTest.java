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

package de.gematik.test.erezept.actions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.actions.communication.GetMessage;
import de.gematik.test.erezept.actors.ActorStage;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.CommunicationGetByIdCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommunicationGetMessagesByIdTest extends ErpFhirBuildingTest {

  private ErpClient erpClientMock;
  private MockActorsUtils mockUtil;
  private PatientActor patientActor;

  @BeforeEach
  void init() {
    StopwatchProvider.init();
    this.mockUtil = new MockActorsUtils();
    ActorStage actorStage = mockUtil.actorStage;
    this.erpClientMock = mockUtil.erpClientMock;
    patientActor = actorStage.getPatientNamed("Leonie Hütter");
  }

  @Test
  void shouldGetCommunicationById() {
    val mockResponse = mockUtil.createErpResponse(null, ErxCommunication.class, 200);
    when(erpClientMock.request(any(CommunicationGetByIdCommand.class))).thenReturn(mockResponse);
    assertDoesNotThrow(
        () -> patientActor.performs(GetMessage.byId(new CommunicationGetByIdCommand("asd"))));
  }
}
