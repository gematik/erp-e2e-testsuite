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

import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.client.usecases.CommunicationDeleteCommand;
import lombok.RequiredArgsConstructor;
import net.serenitybdd.screenplay.Actor;
import org.hl7.fhir.r4.model.Resource;

@RequiredArgsConstructor
public class DeleteMessages extends ErpAction<Resource> {

  private final CommunicationDeleteCommand communicationDeleteCommand;

  public static DeleteMessages fromServerWith(
      CommunicationDeleteCommand communicationDeleteCommand) {
    return new DeleteMessages(communicationDeleteCommand);
  }

  @Override
  public ErpInteraction<Resource> answeredBy(Actor actor) {
    return performCommandAs(communicationDeleteCommand, actor);
  }
}
