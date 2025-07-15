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

package de.gematik.test.erezept.primsys.model;

import de.gematik.test.erezept.client.usecases.CommunicationPostCommand;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.valuesets.AvailabilityStatus;
import de.gematik.test.erezept.primsys.actors.Pharmacy;
import jakarta.ws.rs.core.Response;
import lombok.val;

public class ReplyUseCase {

  private ReplyUseCase() {
    throw new AssertionError();
  }

  public static Response replyPrescription(
      Pharmacy actor, String taskId, String kvnr, String supplyOption, String message) {
    var supplyOptionType = SupplyOptionsType.getSupplyOptionType(supplyOption);
    val sender = actor.getSmcb().getTelematikId();
    return replyPrescription(actor, taskId, kvnr, supplyOptionType, message, sender);
  }

  public static Response replyPrescriptionWithSender(
      Pharmacy actor,
      String taskId,
      String kvnr,
      String supplyOption,
      String message,
      String sender) {
    var supplyOptionType = SupplyOptionsType.getSupplyOptionType(supplyOption);
    return replyPrescription(actor, taskId, kvnr, supplyOptionType, message, sender);
  }

  private static Response replyPrescription(
      Pharmacy actor,
      String taskId,
      String kvnr,
      SupplyOptionsType type,
      String message,
      String sender) {
    val erxCommunication =
        ErxCommunicationBuilder.asReply(message)
            .basedOn(taskId)
            .receiver(kvnr)
            .availabilityStatus(AvailabilityStatus.AS_30)
            .supplyOptions(type)
            .sender(sender)
            .build();

    val communicationPostCommand = new CommunicationPostCommand(erxCommunication);
    val replyResponse = actor.erpRequest(communicationPostCommand);

    return Response.status(replyResponse.getStatusCode()).build();
  }
}
