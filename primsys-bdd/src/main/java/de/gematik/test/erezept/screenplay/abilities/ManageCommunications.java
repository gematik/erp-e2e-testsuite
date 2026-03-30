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

package de.gematik.test.erezept.screenplay.abilities;

import de.gematik.bbriccs.fhir.codec.OperationOutcomeExtractor;
import de.gematik.test.erezept.client.usecases.CommunicationDeleteCommand;
import de.gematik.test.erezept.screenplay.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import net.serenitybdd.screenplay.*;
import org.hl7.fhir.r4.model.OperationOutcome;

@Slf4j
@Getter
public class ManageCommunications implements Ability, HasTeardown, RefersToActor {

  private final ManagedList<ExchangedCommunication> sentCommunications;
  private final ManagedList<ExchangedCommunication> expectedCommunications;
  private Actor actor;

  private ManageCommunications() {
    this.sentCommunications = new ManagedList<>(() -> "No Communications were sent so far");
    this.expectedCommunications = new ManagedList<>(() -> "No Communications were expected so far");
  }

  public static ManageCommunications sheExchanges() {
    return heExchanges();
  }

  public static ManageCommunications heExchanges() {
    return new ManageCommunications();
  }

  @Override
  public void tearDown() {
    log.info("TearDown ManageCommunications Ability for {}", this.actor.getName());
    val erpClientAbility = this.actor.abilityTo(UseTheErpClient.class);

    // null-check required because having this ability does not necessarily mean that ErpClient is
    // also available
    if (erpClientAbility != null) {
      // use the erpClient directly to avoid reporting of the teardowns
      val erpClient = erpClientAbility.getClient();
      this.sentCommunications.getRawList().stream()
          .filter(com -> com.getCommunicationId().isPresent())
          .forEach(
              com -> {
                // will never throw because of the filter above
                val comId = com.getCommunicationId().orElseThrow();
                val type = com.getType().name();
                val pid = com.getBasedOn().getValue();

                val response = erpClient.request(new CommunicationDeleteCommand(comId));
                response
                    .getResourceOptional(OperationOutcome.class)
                    .map(OperationOutcomeExtractor::extractFrom)
                    .ifPresentOrElse(
                        errorMessage ->
                            log.warn(
                                "Received OperationOutcome with HTTP Statuscode {} while trying to"
                                    + " delete {} communication for {}: \n"
                                    + "{}",
                                response.getStatusCode(),
                                type,
                                pid,
                                errorMessage),
                        () -> log.info("Successfully deleted {} communication for {}", type, pid));
              });
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Ability> T asActor(Actor actor) {
    this.actor = actor;
    return (T) this;
  }
}
