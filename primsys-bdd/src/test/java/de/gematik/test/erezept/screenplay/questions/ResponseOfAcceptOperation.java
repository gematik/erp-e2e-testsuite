/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskAcceptCommand;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.resources.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.strategy.pharmacy.AcceptStrategy;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@Slf4j
public class ResponseOfAcceptOperation extends FhirResponseQuestion<ErxAcceptBundle> {

  private DmcPrescription prescription;
  private AcceptStrategy strategy;
  private ErxCommunication communication;

  private ResponseOfAcceptOperation(DmcPrescription prescription) {
    super("Task/$accept via DMC");
    this.prescription = prescription;
  }

  private ResponseOfAcceptOperation(AcceptStrategy strategy) {
    super("Task/$accept");
    this.strategy = strategy;
  }

  private ResponseOfAcceptOperation(ErxCommunication communication) {
    super("Task/$accept via Communication");
    this.communication = communication;
  }

  @Override
  public ErpResponse<ErxAcceptBundle> answeredBy(Actor actor) {
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val executedCommand = createCommand(actor);
    log.info(
        format(
            "Actor {0} is asking for the response of {1}",
            actor.getName(), executedCommand.getRequestLocator()));
    return erpClientAbility.request(executedCommand);
  }

  private TaskAcceptCommand createCommand(Actor actor) {
    TaskId taskId;
    AccessCode accessCode;

    if (strategy != null) {
      // if a strategy was given the prescription is null and choose one from stack!
      val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
      strategy.initialize(prescriptionManager);
      val toAccept = strategy.getDmcPrescription();
      taskId = toAccept.getTaskId();
      accessCode = toAccept.getAccessCode();
    } else if (communication != null) {
      taskId = communication.getBasedOnReferenceId();
      accessCode =
          communication
              .getBasedOnAccessCode()
              .orElseThrow(
                  () ->
                      new MissingPreconditionError(
                          format(
                              "{0} Communication with ID {1} is missing an AccessCode",
                              communication.getType(), communication.getUnqualifiedId())));
    } else {
      taskId = prescription.getTaskId();
      accessCode = prescription.getAccessCode();
    }

    return new TaskAcceptCommand(taskId, accessCode);
  }

  public static ResponseOfAcceptOperation forPrescription(DmcPrescription prescription) {
    return new ResponseOfAcceptOperation(prescription);
  }

  public static ResponseOfAcceptOperation forDispenseRequest(ErxCommunication communication) {
    return new ResponseOfAcceptOperation(communication);
  }

  public static ResponseOfAcceptOperation fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static ResponseOfAcceptOperation fromStack(DequeStrategy dequeue) {
    return new ResponseOfAcceptOperation(AcceptStrategy.fromStack(dequeue));
  }
}
