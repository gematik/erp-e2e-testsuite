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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.actions.communication.SendMessages;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.CommunicationPostCommand;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.fuzzing.erx.ErxCommunicationPayloadManipulatorFactory;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SendMessagesTest {

  private ErpClient erpClientMock;
  private MockActorsUtils mockUtil;
  private PharmacyActor pharmacyActor;
  private PatientActor patientActor;

  @BeforeEach
  void init() {
    StopwatchProvider.init();
    this.mockUtil = new MockActorsUtils();

    val actorStage = mockUtil.actorStage;
    this.erpClientMock = mockUtil.erpClientMock;
    pharmacyActor = actorStage.getPharmacyNamed("Stadtapotheke");
    patientActor = actorStage.getPatientNamed("Leonie Hütter");
  }

  @Test
  void shouldBuildDispenseRequest() {
    val communicationDisReqMessage =
        new CommunicationDisReqMessage(SupplyOptionsType.ON_PREMISE, "testMessage");
    val task = mock(ErxTask.class);
    val flowType = PrescriptionFlowType.FLOW_TYPE_160;
    val prescriptionId = PrescriptionId.random(flowType);

    when(task.getTaskId()).thenReturn(prescriptionId.toTaskId());
    when(task.getAccessCode()).thenReturn(AccessCode.random());
    when(task.getFlowType()).thenReturn(flowType);

    val payload =
        ErxCommunicationBuilder.forDispenseRequest(communicationDisReqMessage)
            .basedOn(prescriptionId.toTaskId(), AccessCode.random())
            .flowType(flowType)
            .receiver(patientActor.getKvnr().getValue())
            .build();

    val res = mockUtil.createErpResponse(payload, ErxCommunication.class);

    when(erpClientMock.request(any(CommunicationPostCommand.class))).thenReturn(res);
    val com =
        SendMessages.to(pharmacyActor)
            .forTask(task)
            .addManipulator(
                ErxCommunicationPayloadManipulatorFactory.getCommunicationPayloadManipulators())
            .asDispenseRequest(communicationDisReqMessage);
    val erpIntCom = com.answeredBy(pharmacyActor);
    assertNotNull(erpIntCom.getExpectedType());
    assertNotNull(erpIntCom.getExpectedResponse());
  }

  @Test
  void shouldBuildCommunicationReply() {
    val communicationReplyMessage =
        new CommunicationReplyMessage(SupplyOptionsType.ON_PREMISE, "testMessage");
    val tsk = mock(ErxTask.class);
    when(tsk.getTaskId()).thenReturn(TaskId.from(UUID.randomUUID().toString()));
    when(tsk.getAccessCode()).thenReturn(AccessCode.random());
    val payload =
        ErxCommunicationBuilder.asReply(communicationReplyMessage)
            .basedOn(TaskId.from(UUID.randomUUID().toString()), AccessCode.random())
            .receiver(pharmacyActor.getTelematikId().getValue())
            .build();
    val res = mockUtil.createErpResponse(payload, ErxCommunication.class);

    when(erpClientMock.request(any(CommunicationPostCommand.class))).thenReturn(res);
    val com =
        SendMessages.to(patientActor)
            .forTask(tsk)
            .addManipulator(
                ErxCommunicationPayloadManipulatorFactory.getCommunicationPayloadManipulators())
            .asReply(communicationReplyMessage, pharmacyActor);
    val erpIntCom = com.answeredBy(patientActor);
    assertNotNull(erpIntCom.getExpectedType());
    assertDoesNotThrow(erpIntCom::getExpectedResponse);
  }

  @Test
  void shouldBuildCommunicationReplyWithSupplyOption() {
    val testReply = new CommunicationReplyMessage(SupplyOptionsType.ON_PREMISE, "testReply");
    val tsk = mock(ErxTask.class);
    when(tsk.getTaskId()).thenReturn(TaskId.random());
    when(tsk.getAccessCode()).thenReturn(AccessCode.random());
    val payload =
        ErxCommunicationBuilder.asReply(testReply)
            .basedOn(TaskId.from(UUID.randomUUID().toString()), AccessCode.random())
            .receiver(pharmacyActor.getTelematikId().getValue())
            .supplyOptions(SupplyOptionsType.ON_PREMISE)
            .build();
    val res = mockUtil.createErpResponse(payload, ErxCommunication.class);
    when(erpClientMock.request(any(CommunicationPostCommand.class))).thenReturn(res);
    val sentReplyComm =
        SendMessages.to(patientActor).forTask(tsk).asReply(testReply, pharmacyActor);
    val interActionComm = sentReplyComm.answeredBy(patientActor);
    assertNotNull(interActionComm.getExpectedType());
  }

  @Test
  void shouldSendWithCustomCommunication() {
    val payload =
        ErxCommunicationBuilder.asReply(
                new CommunicationReplyMessage(SupplyOptionsType.ON_PREMISE, "testReply"))
            .basedOn(TaskId.from(PrescriptionId.random()), AccessCode.random())
            .receiver(pharmacyActor.getTelematikId().getValue())
            .supplyOptions(SupplyOptionsType.ON_PREMISE)
            .build();
    val res = mockUtil.createErpResponse(payload, ErxCommunication.class);
    when(erpClientMock.request(any(CommunicationPostCommand.class))).thenReturn(res);

    val sentReplyComm = SendMessages.withCommunication(payload);
    val interActionComm = sentReplyComm.answeredBy(patientActor);
    assertNotNull(interActionComm.getExpectedType());
  }

  @Test
  void shouldSendWithCustomTaskValues() {
    val testReply = new CommunicationReplyMessage(SupplyOptionsType.ON_PREMISE, "testReply");
    val task = mock(ErxTask.class);
    when(task.getTaskId()).thenReturn(PrescriptionId.random().toTaskId());
    when(task.getAccessCode()).thenReturn(AccessCode.random());
    val payload =
        ErxCommunicationBuilder.asReply(testReply)
            .basedOn(TaskId.from(UUID.randomUUID().toString()), AccessCode.random())
            .receiver(pharmacyActor.getTelematikId().getValue())
            .supplyOptions(SupplyOptionsType.ON_PREMISE)
            .build();
    val res = mockUtil.createErpResponse(payload, ErxCommunication.class);
    when(erpClientMock.request(any(CommunicationPostCommand.class))).thenReturn(res);
    val sentReplyComm =
        SendMessages.to(patientActor)
            .with(TaskId.from("123.456.789"), AccessCode.random())
            .asReply(testReply, pharmacyActor);
    val interActionComm = sentReplyComm.answeredBy(patientActor);
    assertNotNull(interActionComm.getExpectedType());
  }

  @Test
  void shouldBuildSendMessageWithCustomCommunicationAndWFVersion() {
    val communicationDisReqMessage =
        new CommunicationDisReqMessage(SupplyOptionsType.ON_PREMISE, "testMessage");
    val task = mock(ErxTask.class);
    val flowType = PrescriptionFlowType.FLOW_TYPE_160;
    val prescriptionId = PrescriptionId.random(flowType);

    when(task.getTaskId()).thenReturn(prescriptionId.toTaskId());
    when(task.getAccessCode()).thenReturn(AccessCode.random());
    when(task.getFlowType()).thenReturn(flowType);
    val com =
        SendMessages.to(pharmacyActor).forTask(task).asDispenseRequest(communicationDisReqMessage);
    assertNotNull(com);
  }
}
