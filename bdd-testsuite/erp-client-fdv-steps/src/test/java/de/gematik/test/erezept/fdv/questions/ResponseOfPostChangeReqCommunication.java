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
import de.gematik.test.erezept.common.screenplay.questions.ResponseOfGetChargeItemBundleAsPatient;
import de.gematik.test.erezept.common.screenplay.questions.ResponseOfPostCommunication;
import de.gematik.test.erezept.common.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.common.screenplay.util.SafeAbility;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.r4.erp.ChargeItemCommunicationType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor
@Slf4j
public class ResponseOfPostChangeReqCommunication extends ResponseOfPostCommunication {
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

    val communication = this.createChangeReqAs(actor);

    log.info(
        "{} is sending a CHANGE_REQ Communication to {} basedOn {} (AccessCode {})",
        actor.getName(),
        builder.receiver.getName(),
        communication.getBasedOnReferenceId(),
        communication.getBasedOnAccessCodeString().orElse("n/a"));

    val cmd = new CommunicationPostCommand(communication);
    return erpClient.request(cmd);
  }

  private ErxCommunication createChangeReqAs(Actor actor) {
    val receiverId = SafeAbility.getAbility(builder.receiver, UseSMCB.class).getTelematikID();
    val sender = SafeAbility.getAbility(actor, ProvideEGK.class).getKvnr();

    val chargeItemResponse =
        actor.asksFor(
            ResponseOfGetChargeItemBundleAsPatient.forPrescription(builder.basedOnDequeStrategy));
    val chargeItem = chargeItemResponse.getExpectedResource();
    return ErxCommunicationBuilder.forChargeItemChangeRequest(builder.message)
        .basedOn(chargeItem.getChargeItem())
        .receiver(receiverId)
        .sender(sender.getValue())
        .build();
  }

  public static class Builder {
    private DequeStrategy basedOnDequeStrategy;
    private Actor receiver;
    private String message;

    public Builder forChargeItemFromBackend(DequeStrategy dequeStrategy) {
      this.basedOnDequeStrategy = dequeStrategy;
      return this;
    }

    public Builder sentTo(Actor receiver) {
      this.receiver = receiver;
      return this;
    }

    public ResponseOfPostChangeReqCommunication withoutMessage() {
      return new ResponseOfPostChangeReqCommunication(this);
    }

    public ResponseOfPostChangeReqCommunication withRandomMessage() {
      return withMessage(
          GemFaker.fakerChargeItemCommunicationMessage(ChargeItemCommunicationType.CHANGE_REQ));
    }

    public ResponseOfPostChangeReqCommunication withMessage(String message) {
      this.message = message;
      return new ResponseOfPostChangeReqCommunication(this);
    }
  }
}
