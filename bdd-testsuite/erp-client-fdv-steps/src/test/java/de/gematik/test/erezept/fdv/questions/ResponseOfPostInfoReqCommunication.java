/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.fdv.questions;

import de.gematik.bbriccs.rest.fd.FhirBResponse;
import de.gematik.test.erezept.client.usecases.CommunicationPostCommand;
import de.gematik.test.erezept.common.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.common.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.common.screenplay.questions.DownloadAllTasks;
import de.gematik.test.erezept.common.screenplay.questions.FullPrescriptionBundle;
import de.gematik.test.erezept.common.screenplay.questions.ResponseOfPostCommunication;
import de.gematik.test.erezept.common.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.common.screenplay.util.SafeAbility;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.r4.erp.CommunicationType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor
@Slf4j
public class ResponseOfPostInfoReqCommunication extends ResponseOfPostCommunication {
  private final Builder builder;

  public static Builder build() {
    return new Builder();
  }

  @Override
  public Actor getReceiver() {
    return builder.receiver;
  }

  @Override
  public FhirBResponse<ErxCommunication> answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val communication = createInfoRequestAs(actor);

    log.info(
        "{} is sending an INFO_REQ Communication to {} basedOn {} (AccessCode {})",
        actor.getName(),
        builder.receiver.getName(),
        communication.getBasedOnReferenceId(),
        communication.getBasedOnAccessCodeString().orElse("n/a"));

    val cmd = new CommunicationPostCommand(communication);
    return erpClient.request(cmd);
  }

  private ErxCommunication createInfoRequestAs(Actor actor) {
    val baseData = SafeAbility.getAbility(actor, ProvidePatientBaseData.class);
    val receiverId = SafeAbility.getAbility(builder.receiver, UseSMCB.class).getTelematikID();

    // first make sure we download all tasks and fetch the corresponding prescription
    val taskBundle = actor.asksFor(DownloadAllTasks.sortedWith(builder.basedOnDequeStrategy));
    val task = taskBundle.getTasks().get(0);
    val prescription = actor.asksFor(FullPrescriptionBundle.forTask(task));

    val erxBuilder =
        ErxCommunicationBuilder.forInfoRequest(builder.message)
            .receiver(receiverId)
            .basedOn(prescription.getTask().getTaskId())
            .supplyOptions(SupplyOptionsType.ON_PREMISE)
            .insurance(baseData.getInsuranceIknr())
            .flowType(task.getFlowType());

    prescription
        .getKbvBundle()
        .ifPresent(kbvErpBundle -> erxBuilder.medication(kbvErpBundle.getMedication()));
    return erxBuilder.build();
  }

  public static class Builder {
    private DequeStrategy basedOnDequeStrategy;
    private Actor receiver;
    private String message;

    public Builder forPrescriptionFromBackend(String order) {
      return forPrescriptionFromBackend(DequeStrategy.fromString(order));
    }

    public Builder forPrescriptionFromBackend(DequeStrategy dequeStrategy) {
      this.basedOnDequeStrategy = dequeStrategy;
      return this;
    }

    public Builder sentTo(Actor receiver) {
      this.receiver = receiver;
      return this;
    }

    public ResponseOfPostInfoReqCommunication withoutMessage() {
      return new ResponseOfPostInfoReqCommunication(this);
    }

    public ResponseOfPostInfoReqCommunication withRandomMessage() {
      return withMessage(GemFaker.fakerCommunicationMessage(CommunicationType.INFO_REQ));
    }

    public ResponseOfPostInfoReqCommunication withMessage(String message) {
      this.message = message;
      return new ResponseOfPostInfoReqCommunication(this);
    }
  }
}
