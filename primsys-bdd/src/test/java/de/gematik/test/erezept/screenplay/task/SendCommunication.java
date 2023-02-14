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

package de.gematik.test.erezept.screenplay.task;

import de.gematik.test.erezept.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.screenplay.questions.ResponseOfPostCommunication;
import de.gematik.test.erezept.screenplay.util.ExchangedCommunication;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SendCommunication implements Task {

  private final ResponseOfPostCommunication question;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val response = actor.asksFor(question);
    val communication = response.getResource(question.expectedResponseBody());
    val exchanged =
        ExchangedCommunication.from(actor.getName())
            .to(question.getReceiver().getName())
            .sent(communication);

    val receiverStack = SafeAbility.getAbility(question.getReceiver(), ManageCommunications.class);
    val senderStack = SafeAbility.getAbility(actor, ManageCommunications.class);

    receiverStack.getExpectedCommunications().append(exchanged);
    senderStack.getSentCommunications().append(exchanged);
  }

  public static SendCommunication with(ResponseOfPostCommunication communicationQuestion) {
    return new SendCommunication(communicationQuestion);
  }
}
