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

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@Getter
public class ResponseOfAbortUnaccepted extends FhirResponseQuestion<Resource> {

  private final DequeStrategy deque;

  private ResponseOfAbortUnaccepted(DequeStrategy deque) {
    super("Task/$abort");
    this.deque = deque;
  }

  @Override
  public ErpResponse<Resource> answeredBy(Actor actor) {
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val accepted =
        SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class).getAssignedPrescriptions();
    val toDelete = deque.chooseFrom(accepted);
    val taskId = toDelete.getTaskId();
    val accessCode = toDelete.getAccessCode();
    val cmd = new TaskAbortCommand(taskId, accessCode);
    log.info(
        format(
            "Pharmacy {0} is asking for the response of {1} with AccessCode {2} and without a secret",
            actor.getName(), cmd.getRequestLocator(), accessCode));
    return erpClientAbility.request(cmd);
  }

  public static Builder asPharmacy() {
    return new Builder();
  }

  public static class Builder {

    public ResponseOfAbortUnaccepted fromStack(String order) {
      return fromStack(DequeStrategy.fromString(order));
    }

    public ResponseOfAbortUnaccepted fromStack(DequeStrategy deque) {
      return new ResponseOfAbortUnaccepted(deque);
    }
  }
}
