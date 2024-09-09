/*
 * Copyright 2024 gematik GmbH
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

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.client.usecases.CommunicationGetByIdCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import lombok.RequiredArgsConstructor;
import net.serenitybdd.screenplay.Actor;

@RequiredArgsConstructor
public class GetMessage extends ErpAction<ErxCommunication> {

  private final CommunicationGetByIdCommand communicationGetByIdCommand;

  public static GetMessage byId(ErxCommunication com) {
    return byId(com.getIdPart());
  }

  public static GetMessage byId(String id) {
    return byId(new CommunicationGetByIdCommand(id));
  }

  public static GetMessage byId(CommunicationGetByIdCommand communicationGetCommand) {
    return new GetMessage(communicationGetCommand);
  }

  @Override
  public ErpInteraction<ErxCommunication> answeredBy(Actor actor) {
    return performCommandAs(communicationGetByIdCommand, actor);
  }
}
