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

package de.gematik.test.erezept.actions.communication;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.client.usecases.CommunicationGetCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunicationBundle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor
@Slf4j
public class GetMessages extends ErpAction<ErxCommunicationBundle> {

  private final CommunicationGetCommand communicationGetCommand;

  public static GetMessages fromServerWith(CommunicationGetCommand communicationGetCommand) {
    return new GetMessages(communicationGetCommand);
  }

  @Override
  @Step("{0} ruft Communications als Bundle ab")
  public ErpInteraction<ErxCommunicationBundle> answeredBy(Actor actor) {
    log.info(
        format(
            "Tried to call communications from Backend as Bundle for: {0} with: {1}",
            actor.getName(), communicationGetCommand.getRequestLocator()));
    return performCommandAs(communicationGetCommand, actor);
  }
}
