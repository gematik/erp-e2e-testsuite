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

package de.gematik.test.erezept.actions.communication;

import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.actors.ErpActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.usecases.CommunicationPostCommand;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.util.LinkedList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor
public class SendMessages extends ErpAction<ErxCommunication> {

  private final ErxCommunication communication;

  private final List<NamedEnvelope<FuzzingMutator<ErxCommunication>>> fuzzingMutators;

  public static Builder to(Actor receiver) {
    return new Builder(receiver);
  }

  public static SendMessages withCommunication(ErxCommunication communication) {
    return withCommunication(communication, List.of());
  }

  public static SendMessages withCommunication(
      ErxCommunication communication,
      List<NamedEnvelope<FuzzingMutator<ErxCommunication>>> fuzzingMutators) {
    return new SendMessages(communication, fuzzingMutators);
  }

  @Override
  @Step("{0} sendet eine Communication ")
  public ErpInteraction<ErxCommunication> answeredBy(Actor actor) {
    fuzzingMutators.forEach(m -> m.getParameter().accept(communication));
    val cmd = new CommunicationPostCommand(communication);
    return this.performCommandAs(cmd, actor);
  }

  @RequiredArgsConstructor
  public static class Builder {
    private final Actor receiver;
    private final List<NamedEnvelope<FuzzingMutator<ErxCommunication>>> fuzzingMutators =
        new LinkedList<>();

    private AccessCode accessCode;

    private TaskId taskId;

    public Builder forTask(ErxTask erxTask) {
      this.taskId = erxTask.getTaskId();
      this.accessCode = erxTask.getAccessCode();
      return this;
    }

    public Builder with(TaskId taskId, AccessCode accessCode) {
      this.taskId = taskId;
      this.accessCode = accessCode;
      return this;
    }

    public Builder addManipulator(
        List<NamedEnvelope<FuzzingMutator<ErxCommunication>>> fuzzingMutator) {
      this.fuzzingMutators.addAll(fuzzingMutator);
      return this;
    }

    public SendMessages asReply(CommunicationReplyMessage message, ErpActor sender) {
      val patientBaseData = SafeAbility.getAbility(receiver, ProvidePatientBaseData.class);
      val communication =
          ErxCommunicationBuilder.asReply(message)
              .sender(((PharmacyActor) sender).getTelematikId().getValue())
              .receiver(patientBaseData.getKvnr().getValue())
              .basedOn(taskId, accessCode)
              .supplyOptions(SupplyOptionsType.getSupplyOptionType(message.supplyOptionsType()))
              .build();
      return withCommunication(communication, fuzzingMutators);
    }

    public SendMessages asDispenseRequest(CommunicationDisReqMessage message) {
      val useSmcb = SafeAbility.getAbility(receiver, UseSMCB.class);
      val telematikId = TelematikID.from(useSmcb.getTelematikID());
      val communication =
          ErxCommunicationBuilder.forDispenseRequest(message)
              .receiver(telematikId.getValue())
              .basedOn(taskId, accessCode)
              .flowType(taskId.getFlowType())
              .build();
      return withCommunication(communication, fuzzingMutators);
    }

    public SendMessages asDispenseRequest(
        CommunicationDisReqMessage message, ErpWorkflowVersion version) {
      val useSmcb = SafeAbility.getAbility(receiver, UseSMCB.class);
      val telematikId = TelematikID.from(useSmcb.getTelematikID());
      val communication =
          ErxCommunicationBuilder.forDispenseRequest(message)
              .receiver(telematikId.getValue())
              .basedOn(taskId, accessCode)
              .flowType(taskId.getFlowType())
              .version(version)
              .build();
      return withCommunication(communication, fuzzingMutators);
    }
  }
}
