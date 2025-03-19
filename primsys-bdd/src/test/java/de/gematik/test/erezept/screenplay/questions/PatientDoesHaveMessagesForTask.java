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
 */

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.client.usecases.search.CommunicationSearch;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Optional;
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
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val stack = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = deque.chooseFrom(stack.getDmcList());
    val taskId = dmc.getTaskId();

    val cmd = CommunicationSearch.getLatestCommunications();
    val response = erpClient.request(cmd);
    val searchBundle = response.getExpectedResource();
    return searchBundle.getCommunications().stream()
        .map(ErxCommunication::getAboutReference)
        .filter(Optional::isPresent)
        .map(Optional::orElseThrow)
        .anyMatch(ref -> ref.contains(taskId.getValue()));
  }

  public static PatientDoesHaveMessagesForTask fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static PatientDoesHaveMessagesForTask fromStack(DequeStrategy deque) {
    return new PatientDoesHaveMessagesForTask(deque);
  }
}
