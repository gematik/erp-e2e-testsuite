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
import de.gematik.test.erezept.common.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.common.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.common.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.common.screenplay.questions.FullPrescriptionBundle;
import de.gematik.test.erezept.common.screenplay.questions.ResponseOfPostCommunication;
import de.gematik.test.erezept.common.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.common.screenplay.util.SafeAbility;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor
@Slf4j
public class ResponseOfPostDIGADispReqCommunication extends ResponseOfPostCommunication {
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
    val communication = createDispenseRequestAs(actor);

    log.info(
        "{} is sending a DIGA_DISP_REQ Communication to {} basedOn {} (AccessCode {})",
        actor.getName(),
        builder.receiver.getName(),
        communication.getBasedOnReferenceId(),
        communication.getBasedOnAccessCodeString().orElse("n/a"));

    val cmd = new CommunicationPostCommand(communication);
    return erpClient.request(cmd);
  }

  private ErxCommunication createDispenseRequestAs(Actor actor) {
    val receiverId = SafeAbility.getAbility(builder.receiver, UseSMCB.class).getTelematikID();
    val dmcAbility = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);

    // first make sure we download all tasks and fetch the corresponding prescription
    val dmc = builder.basedOnDequeStrategy.chooseFrom(dmcAbility.getDmcs());
    val prescription = actor.asksFor(FullPrescriptionBundle.forTask(dmc.getTaskId()));

    val accessCode = prescription.getTask().getAccessCode();

    // Note: taking the flowType from the prescription ID might break the test intention
    // here we can hard-code the flowType for DiGA because of the context of this question
    val flowType = PrescriptionFlowType.FLOW_TYPE_162;
    return ErxCommunicationBuilder.forDiGADispenseRequest()
        .receiver(receiverId)
        .basedOn(prescription.getTask().getTaskId(), accessCode)
        .flowType(flowType)
        .build();
  }

  public static class Builder {
    private DequeStrategy basedOnDequeStrategy;
    private Actor receiver;

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

    public ResponseOfPostDIGADispReqCommunication withoutMessage() {
      return new ResponseOfPostDIGADispReqCommunication(this);
    }
  }
}
