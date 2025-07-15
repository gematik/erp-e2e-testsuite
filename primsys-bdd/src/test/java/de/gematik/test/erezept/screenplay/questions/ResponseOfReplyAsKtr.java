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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.CommunicationPostCommand;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.erezept.fhir.valuesets.AvailabilityStatus;
import de.gematik.test.erezept.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.ExchangedCommunication;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseOfReplyAsKtr extends FhirResponseQuestion<ErxCommunication> {

  private final DequeStrategy order;
  private final Actor receiver;
  private final String message;

  public static Builder replyDiGARequest(DequeStrategy dequeStrategy) {
    return new Builder(dequeStrategy);
  }

  public static Builder replyDiGARequest(String dequeStrategy) {
    return replyDiGARequest(DequeStrategy.fromString(dequeStrategy));
  }

  @Override
  public ErpResponse<ErxCommunication> answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val receiverId = SafeAbility.getAbility(receiver, ProvidePatientBaseData.class).getKvnr();
    val prescriptionPharmacyManager =
        SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val communicationManager = SafeAbility.getAbility(receiver, ManageCommunications.class);
    val acceptBundle = order.chooseFrom(prescriptionPharmacyManager.getAcceptedPrescriptions());
    val replyMessage = new CommunicationReplyMessage(SupplyOptionsType.DELIVERY, message);

    val communication =
        ErxCommunicationBuilder.asReply(replyMessage)
            .receiver(receiverId.getValue())
            .basedOn(acceptBundle.getTaskId())
            .availabilityStatus(AvailabilityStatus.AS_30)
            .supplyOptions(SupplyOptionsType.DELIVERY)
            .build();

    val cmd = new CommunicationPostCommand(communication);
    val resp = erpClient.request(cmd);
    if (resp.isOfExpectedType())
      communicationManager
          .getExpectedCommunications()
          .append(
              ExchangedCommunication.from(resp.getExpectedResource())
                  .withActorNames(actor, receiver));
    return resp;
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  @AllArgsConstructor()
  public static class Builder {
    private final DequeStrategy dequeStrategy;
    private Actor receiver;

    public Builder sentTo(Actor receiver) {
      this.receiver = receiver;
      return this;
    }

    public ResponseOfReplyAsKtr withMessage(String message) {
      return new ResponseOfReplyAsKtr(dequeStrategy, receiver, message);
    }
  }
}
