/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertFalse;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.CommunicationGetByIdCommand;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import de.gematik.test.erezept.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

public class ResponseOfGetCommunicationFrom extends FhirResponseQuestion<ErxCommunication> {

  private final DequeStrategy deque;
  private final Actor sender;

  private ResponseOfGetCommunicationFrom(DequeStrategy deque, Actor sender) {
    super("GET /Communication");
    this.deque = deque;
    this.sender = sender;
  }

  @Override
  public ErpResponse<ErxCommunication> answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val communicationsOracle = SafeAbility.getAbility(actor, ManageCommunications.class);
    val expectedCommunications =
        communicationsOracle.getExpectedCommunications().getRawList().stream()
            .filter(exp -> exp.getSenderName().equals(sender.getName()))
            .collect(Collectors.toList());

    // make sure we have at least one expected message from the sender
    assertFalse(expectedCommunications.isEmpty());

    // now try to fetch
    val com = deque.chooseFrom(expectedCommunications);
    val id =
        com.getCommunicationId()
            .orElseThrow(
                () ->
                    new MissingPreconditionError(
                        format(
                            "Expected communication from {0} with Type {1} does not have an ID",
                            com.getSenderName(), com.getType())));
    val cmd = new CommunicationGetByIdCommand(id);
    return erpClient.request(cmd);
  }

  public static Builder sender(Actor sender) {
    return new Builder(sender);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final Actor sender;

    public ResponseOfGetCommunicationFrom onStack(String order) {
      return onStack(DequeStrategy.fromString(order));
    }

    public ResponseOfGetCommunicationFrom onStack(DequeStrategy deque) {
      return new ResponseOfGetCommunicationFrom(deque, sender);
    }
  }
}
