/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.usecases.CommunicationGetByIdCommand;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.resources.erp.CommunicationType;
import de.gematik.test.erezept.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategyEnum;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HandoverDispenseRequestAsRepresentative implements Task {

  private Actor owner;
  private final DequeStrategyEnum deque;
  private Actor pharmacy;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val expectedCommunications =
        SafeAbility.getAbility(actor, ManageCommunications.class).getExpectedCommunications();
    val expectedRepresentatives =
        expectedCommunications.getRawList().stream()
            .filter(exp -> exp.getReceiverName().equals(actor.getName()))
            .filter(exp -> exp.getSenderName().equals(owner.getName()))
            .filter(exp -> exp.getType().equals(CommunicationType.REPRESENTATIVE))
            .collect(Collectors.toList());

    if (expectedCommunications.isEmpty()) {
      throw new MissingPreconditionError(
          format(
              "The Representative {0} has not yet received a Message from {1}",
              actor.getName(), owner.getName()));
    }
    val expected = deque.chooseFrom(expectedRepresentatives);
    val getByIdCmd = new CommunicationGetByIdCommand(expected.getCommunicationId());
    val response = erpClient.request(getByIdCmd);
    val com = response.getResource(getByIdCmd.expectedResponseBody());

    val taskId = com.getBasedOnReferenceId();
    val accessCode =
        com.getBasedOnAccessCode()
            .orElseThrow(
                () ->
                    new MissingPreconditionError(
                        format(
                            "The received Message with ID {0} from {1} does not contain an AccessCode",
                            com.getUnqualifiedId(), owner.getName())));

    val pharmacyPrescriptions = SafeAbility.getAbility(pharmacy, ManagePharmacyPrescriptions.class);
    val dmc = DmcPrescription.representativeDmc(taskId, accessCode);
    pharmacyPrescriptions.appendAssignedPrescription(dmc);
  }

  public static Builder fromStack(String order) {
    return fromStack(DequeStrategyEnum.fromString(order));
  }

  public static Builder fromStack(DequeStrategyEnum deque) {
    return new Builder(deque);
  }

  public static class Builder {
    private final DequeStrategyEnum deque;
    private Actor owner;

    private Builder(DequeStrategyEnum deque) {
      this.deque = deque;
    }

    public Builder ofTheOwner(Actor owner) {
      this.owner = owner;
      return this;
    }

    public HandoverDispenseRequestAsRepresentative to(Actor pharmacy) {
      return new HandoverDispenseRequestAsRepresentative(owner, deque, pharmacy);
    }
  }
}
