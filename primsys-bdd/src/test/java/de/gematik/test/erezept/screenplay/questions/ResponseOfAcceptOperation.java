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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskAcceptCommand;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@Slf4j
public class ResponseOfAcceptOperation extends FhirResponseQuestion<ErxAcceptBundle> {

  @Nullable private final DmcPrescription prescription;
  private final DequeStrategy strategy;

  private ResponseOfAcceptOperation(
      DequeStrategy strategy, @Nullable DmcPrescription prescription) {
    this.strategy = strategy;
    this.prescription = prescription;
  }

  public static ResponseOfAcceptOperation forPrescription(DmcPrescription prescription) {
    return new ResponseOfAcceptOperation(DequeStrategy.FIFO, prescription);
  }

  public static ResponseOfAcceptOperation forDispenseRequest(ErxCommunication communication) {
    val taskId = communication.getBasedOnReferenceId();
    val accessCode =
        communication
            .getBasedOnAccessCode()
            .orElseThrow(
                () ->
                    new MissingPreconditionError(
                        format(
                            "{0} Communication with ID {1} is missing an AccessCode",
                            communication.getType(), communication.getUnqualifiedId())));
    val dmc = DmcPrescription.ownerDmc(taskId, accessCode);
    return forPrescription(dmc);
  }

  public static ResponseOfAcceptOperation fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static ResponseOfAcceptOperation fromStack(DequeStrategy dequeue) {
    return new ResponseOfAcceptOperation(dequeue, null);
  }

  @Override
  public ErpResponse<ErxAcceptBundle> answeredBy(Actor actor) {
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val executedCommand = createCommand(actor);
    log.info(
        "Actor {} is asking for the response of {}",
        actor.getName(),
        executedCommand.getRequestLocator());
    return erpClientAbility.request(executedCommand);
  }

  private TaskAcceptCommand createCommand(Actor actor) {
    val dmc =
        Optional.ofNullable(prescription)
            .orElseGet(
                () -> {
                  val mpp = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
                  val dmcp = this.strategy.chooseFrom(mpp.getAssignedList());
                  return DmcPrescription.ownerDmc(dmcp.getTaskId(), dmcp.getAccessCode());
                });

    return new TaskAcceptCommand(dmc.getTaskId(), dmc.getAccessCode());
  }
}
