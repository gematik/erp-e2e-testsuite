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

package de.gematik.test.erezept.screenplay.questions;

import static org.junit.jupiter.api.Assertions.assertFalse;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.CommunicationGetByIdCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import de.gematik.test.erezept.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategyEnum;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseOfGetCommunicationFrom extends FhirResponseQuestion<ErxCommunication> {

  private final DequeStrategyEnum deque;
  private final Actor sender;

  @Override
  public Class<ErxCommunication> expectedResponseBody() {
    return ErxCommunication.class;
  }

  @Override
  public String getOperationName() {
    return "Communication";
  }

  @Override
  public ErpResponse answeredBy(Actor actor) {
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
    val cmd = new CommunicationGetByIdCommand(com.getCommunicationId());
    return erpClient.request(cmd);
  }

  public static Builder sender(Actor sender) {
    return new Builder(sender);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final Actor sender;

    public ResponseOfGetCommunicationFrom onStack(String order) {
      return onStack(DequeStrategyEnum.fromString(order));
    }

    public ResponseOfGetCommunicationFrom onStack(DequeStrategyEnum deque) {
      return new ResponseOfGetCommunicationFrom(deque, sender);
    }
  }
}
