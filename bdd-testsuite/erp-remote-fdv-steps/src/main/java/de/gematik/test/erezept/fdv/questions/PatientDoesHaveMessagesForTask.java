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

package de.gematik.test.erezept.fdv.questions;

import de.gematik.erezept.remotefdv.api.model.Communication;
import de.gematik.test.erezept.fdv.abilities.UseTheRemoteFdVClient;
import de.gematik.test.erezept.remotefdv.client.PatientRequests;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PatientDoesHaveMessagesForTask implements Question<Boolean> {

  private final DequeStrategy deque;

  @Override
  public Boolean answeredBy(Actor actor) {
    val remoteFdV = SafeAbility.getAbility(actor, UseTheRemoteFdVClient.class);
    val stack = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = deque.chooseFrom(stack.getDmcList());
    val taskId = dmc.getTaskId();

    val response = remoteFdV.sendRequest(PatientRequests.getCommunication());
    val searchBundle = response.getExpectedResourcesList();
    return searchBundle.stream()
        .map(Communication::getReference)
        .anyMatch(ref -> ref.contains(taskId.getValue()));
  }

  public static PatientDoesHaveMessagesForTask fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static PatientDoesHaveMessagesForTask fromStack(DequeStrategy deque) {
    return new PatientDoesHaveMessagesForTask(deque);
  }
}
