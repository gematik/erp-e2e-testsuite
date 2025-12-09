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

package de.gematik.test.erezept.fdv.task;

import de.gematik.test.erezept.config.dto.remotefdv.RemoteFdVActorConfiguration;
import de.gematik.test.erezept.fdv.abilities.UseTheRemoteFdVClient;
import de.gematik.test.erezept.remotefdv.client.PatientRequests;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor
public class SetUpRemoteFdV implements Task {
  private final RemoteFdVActorConfiguration actorConfiguration;

  @Override
  public <T extends Actor> void performAs(final T actor) {
    val useTheRemoteFdv = SafeAbility.getAbility(actor, UseTheRemoteFdVClient.class);
    useTheRemoteFdv.sendRequest(PatientRequests.startFdV()).getExpectedResourcesList();
    useTheRemoteFdv
        .sendRequest(PatientRequests.loginWithKvnr(actorConfiguration.getKvnr()))
        .getExpectedResourcesList();
  }

  public static SetUpRemoteFdV forUser(RemoteFdVActorConfiguration actorConfiguration) {
    return new SetUpRemoteFdV(actorConfiguration);
  }
}
