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

package de.gematik.test.erezept.actions;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.actions.communication.SendMessages;
import de.gematik.test.erezept.actors.ActorStage;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.CommunicationPostCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.fuzzing.erx.ErxCommunicationPayloadManipulatorFactory;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SendMessagesTest {

  private ActorStage actorStage;
  private ErpClient erpClientMock;
  private MockActorsUtils mockUtil;
  private PharmacyActor pharmacyActor;
  private PatientActor patientActor;

  @BeforeEach
  void init() {
    StopwatchProvider.init();
    this.mockUtil = new MockActorsUtils();
    this.actorStage = mockUtil.actorStage;
    this.erpClientMock = mockUtil.erpClientMock;
    pharmacyActor = actorStage.getPharmacyNamed("Stadtapotheke");
    patientActor = actorStage.getPatientNamed("Leonie Hütter");
  }

  @Test
  void shouldBuildDispenseRequest() {
    val communicationDisReqMessage =
        new CommunicationDisReqMessage(SupplyOptionsType.ON_PREMISE, "testMessage");
    val tsk = mock(ErxTask.class);

    when(tsk.getTaskId()).thenReturn(TaskId.from(UUID.randomUUID().toString()));
    when(tsk.getAccessCode()).thenReturn(GemFaker.fakerAccessCode());

    val payload =
        ErxCommunicationBuilder.builder()
            .basedOnTask(TaskId.from(UUID.randomUUID().toString()), AccessCode.random())
            .recipient(patientActor.getKvnr().getValue())
            .buildDispReq(communicationDisReqMessage);

    val res = mockUtil.createErpResponse(payload, ErxCommunication.class);

    when(erpClientMock.request(any(CommunicationPostCommand.class))).thenReturn(res);
    val com =
        SendMessages.to(pharmacyActor)
            .forTask(tsk)
            .addManipulator(
                ErxCommunicationPayloadManipulatorFactory.getCommunicationPayloadManipulators())
            .asDispenseRequest(communicationDisReqMessage);
    val erpIntCom = com.answeredBy(pharmacyActor);
    Assertions.assertNotNull(erpIntCom.getExpectedType());
    Assertions.assertNotNull(erpIntCom.getExpectedResponse());
  }

  @Test
  void shouldBuildCommunicationReply() {
    val communicationReplyMessage =
        new CommunicationReplyMessage(SupplyOptionsType.ON_PREMISE, "testMessage");
    val tsk = mock(ErxTask.class);
    when(tsk.getTaskId()).thenReturn(TaskId.from(UUID.randomUUID().toString()));
    when(tsk.getAccessCode()).thenReturn(GemFaker.fakerAccessCode());
    val payload =
        ErxCommunicationBuilder.builder()
            .basedOnTask(TaskId.from(UUID.randomUUID().toString()), AccessCode.random())
            .recipient(pharmacyActor.getTelematikId().getValue())
            .buildReply(communicationReplyMessage);
    val res = mockUtil.createErpResponse(payload, ErxCommunication.class);
    when(erpClientMock.request(any(CommunicationPostCommand.class))).thenReturn(res);
    val com =
        SendMessages.to(patientActor)
            .forTask(tsk)
            .addManipulator(
                ErxCommunicationPayloadManipulatorFactory.getCommunicationPayloadManipulators())
            .asReply(communicationReplyMessage);
    val erpIntCom = com.answeredBy(patientActor);
    Assertions.assertNotNull(erpIntCom.getExpectedType());
    Assertions.assertNotNull(erpIntCom.getExpectedResponse());
  }

  @Test
  void shouldBuildCommunicationReplyWithSupplyOption() {
    val testReply = new CommunicationReplyMessage(SupplyOptionsType.ON_PREMISE, "testReply");
    val tsk = mock(ErxTask.class);
    when(tsk.getTaskId()).thenReturn(TaskId.from(UUID.randomUUID().toString()));
    when(tsk.getAccessCode()).thenReturn(GemFaker.fakerAccessCode());
    val payload =
        ErxCommunicationBuilder.builder()
            .basedOnTask(TaskId.from(UUID.randomUUID().toString()), AccessCode.random())
            .recipient(pharmacyActor.getTelematikId().getValue())
            .supplyOptions(SupplyOptionsType.ON_PREMISE)
            .buildReply(testReply);
    val res = mockUtil.createErpResponse(payload, ErxCommunication.class);
    when(erpClientMock.request(any(CommunicationPostCommand.class))).thenReturn(res);
    val sentReplyComm = SendMessages.to(patientActor).forTask(tsk).asReply(testReply);
    val interActionComm = sentReplyComm.answeredBy(patientActor);
    assertNotNull(interActionComm.getExpectedType());
  }
}
