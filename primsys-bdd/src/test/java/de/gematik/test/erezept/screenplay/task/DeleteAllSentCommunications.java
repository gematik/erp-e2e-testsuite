/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.client.usecases.CommunicationDeleteCommand;
import de.gematik.test.erezept.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

public class DeleteAllSentCommunications implements Task {

  @Override
  public <T extends Actor> void performAs(T actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val communicationsOracle = SafeAbility.getAbility(actor, ManageCommunications.class);

    communicationsOracle
        .getSentCommunications()
        .getRawList()
        .forEach(
            com -> {
              com.getCommunicationId()
                  .ifPresent(
                      id -> {
                        val cmd = new CommunicationDeleteCommand(id);
                        val response = erpClient.request(cmd);

                        // make sure the FD signaled correct deletion: A_19514-03
                        assertEquals(
                            204,
                            response.getStatusCode(),
                            format(
                                "Communication with ID {0} was not deleted as the backend answered with return code {1}",
                                id, response.getStatusCode()));
                      });
            });
  }

  public static DeleteAllSentCommunications fromBackend() {
    return new DeleteAllSentCommunications();
  }
}
