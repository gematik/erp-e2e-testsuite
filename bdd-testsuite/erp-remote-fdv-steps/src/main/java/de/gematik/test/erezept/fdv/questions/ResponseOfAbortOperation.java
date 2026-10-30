/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.fdv.questions;

import de.gematik.test.erezept.common.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.common.screenplay.strategy.ActorRole;
import de.gematik.test.erezept.common.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.common.screenplay.util.SafeAbility;
import de.gematik.test.erezept.fdv.abilities.UseTheRemoteFdVClient;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.remotefdv.client.PatientRequests;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@Getter
public class ResponseOfAbortOperation implements Question<String> {

  private final ActorRole role;
  private final DequeStrategy deque;
  private final Map<String, String> replacementMap;

  private ResponseOfAbortOperation(
      ActorRole role, DequeStrategy deque, Map<String, String> replacementMap) {
    this.role = role;
    this.deque = deque;
    this.replacementMap = replacementMap;
  }

  @Override
  public String answeredBy(Actor actor) {
    val client = SafeAbility.getAbility(actor, UseTheRemoteFdVClient.class);
    val dmcAbility = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = deque.chooseFrom(dmcAbility.chooseStack(DmcStack.ACTIVE));
    val prescriptionId = dmc.getTaskId().getValue();

    val response = client.sendRequest(PatientRequests.deletePrescriptionById(prescriptionId));
    return response.getOperationOutcome().getStatusCode().toPlainString();
  }

  public static Builder asPatient() {
    return new Builder(ActorRole.PATIENT);
  }

  public static class Builder {
    private final ActorRole role;
    private final Map<String, String> replacementMap;

    private Builder(ActorRole role) {
      this.role = role;
      this.replacementMap = new HashMap<>();
    }

    public Builder withInvalidSecret() {
      return withInvalidSecret(GemFaker.fakerSecret());
    }

    public Builder withInvalidSecret(String secret) {
      this.replacementMap.put("secret", secret);
      return this;
    }

    public ResponseOfAbortOperation fromStack(String order) {
      return fromStack(DequeStrategy.fromString(order));
    }

    public ResponseOfAbortOperation fromStack(DequeStrategy deque) {
      return new ResponseOfAbortOperation(role, deque, replacementMap);
    }
  }
}
