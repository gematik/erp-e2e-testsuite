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

import de.gematik.test.erezept.client.usecases.CommunicationGetByIdCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import de.gematik.test.erezept.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.ExchangedCommunication;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HistSentCommunications implements Question<Boolean> {

  private final BiFunction<List<ExchangedCommunication>, List<ErxCommunication>, Boolean> checker;

  @Override
  public Boolean answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val sentCommunications =
        SafeAbility.getAbility(actor, ManageCommunications.class)
            .getSentCommunications()
            .getRawList();

    val fetchedCommunications = new LinkedList<ErxCommunication>();
    for (val sc : sentCommunications) {
      val id = sc.getCommunicationId();
      val cmd = new CommunicationGetByIdCommand(id);
      val response = erpClient.request(cmd);
      val optionalBody = response.getResourceOptional(cmd.expectedResponseBody());
      optionalBody.ifPresent(fetchedCommunications::add);
    }
    return checker.apply(sentCommunications, fetchedCommunications);
  }

  public static Builder onBackend() {
    return new Builder();
  }

  public static class Builder {

    public HistSentCommunications fromQueueStillExists(String order) {
      return fromQueueStillExists(DequeStrategy.fromString(order));
    }

    public HistSentCommunications fromQueueStillExists(DequeStrategy deque) {
      return new HistSentCommunications(
          (sent, fetched) -> {
            val dequedExp = deque.chooseFrom(sent);
            val dequeFetched = deque.chooseFrom(fetched);
            return dequedExp.getCommunicationId().equals(dequeFetched.getUnqualifiedId());
          });
    }

    public HistSentCommunications expectingAll() {
      return new HistSentCommunications((sent, fetched) -> fetched.size() == sent.size());
    }

    public HistSentCommunications noneExistAnymore() {
      return new HistSentCommunications((sent, fetched) -> fetched.isEmpty());
    }
  }
}
