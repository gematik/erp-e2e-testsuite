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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskRejectCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@Slf4j
public class ResponseOfRejectOperation extends FhirResponseQuestion<EmptyResource> {

  private final DequeStrategy deque;
  private final Map<String, String> replacementMap;

  private ResponseOfRejectOperation(DequeStrategy deque, Map<String, String> replacementMap) {
    this.deque = deque;
    this.replacementMap = replacementMap;
  }

  @Override
  public ErpResponse<EmptyResource> answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val pharmacyStacks = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val toReject = deque.chooseFrom(pharmacyStacks.getAcceptedPrescriptions());
    val taskId = toReject.getTaskId();
    val accessCode = toReject.getTask().getAccessCode();
    val secret = this.getSecret(toReject);
    val cmd = new TaskRejectCommand(taskId, accessCode, secret);
    return erpClient.request(cmd);
  }

  private Secret getSecret(ErxAcceptBundle acceptBundle) {
    var secret = acceptBundle.getSecret();
    val replacementSecret = this.replacementMap.get("secret");
    if (replacementSecret != null) {
      log.info("Found a replacement secret for $abort: {}", replacementSecret);
      secret = Secret.from(replacementSecret);
    }
    return secret;
  }

  public static Builder withInvalidSecret(String secret) {
    return new Builder(secret);
  }

  public static ResponseOfRejectOperation fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static ResponseOfRejectOperation fromStack(DequeStrategy deque) {
    return new ResponseOfRejectOperation(deque, new HashMap<>());
  }

  public static class Builder {
    private final Map<String, String> replacementMap;

    private Builder(String secret) {
      this.replacementMap = new HashMap<>();
      this.replacementMap.put("secret", secret);
    }

    public ResponseOfRejectOperation fromStack(String order) {
      return fromStack(DequeStrategy.fromString(order));
    }

    public ResponseOfRejectOperation fromStack(DequeStrategy deque) {
      return new ResponseOfRejectOperation(deque, replacementMap);
    }
  }
}
