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
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunicationBundle;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DownloadNewMessages implements Question<ErxCommunicationBundle> {

  @Override
  public ErxCommunicationBundle answeredBy(Actor actor) {
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val cmd = CommunicationSearch.getLatestNewCommunications();
    return erpClient.request(cmd).getExpectedResource();
  }

  public static DownloadNewMessages fromServer() {
    return new DownloadNewMessages();
  }
}
