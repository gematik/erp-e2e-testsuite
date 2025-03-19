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

package de.gematik.test.erezept.primsys.model;

import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.primsys.actors.BaseActor;
import jakarta.ws.rs.core.Response;
import lombok.val;

public class AbortUseCase {

  private AbortUseCase() {
    throw new AssertionError();
  }

  public static Response abortPrescription(
      BaseActor actor, String taskId, String accessCode, String secret) {
    return abortUseCase(actor, TaskId.from(taskId), new AccessCode(accessCode), new Secret(secret));
  }

  private static Response abortUseCase(
      BaseActor actor, TaskId taskId, AccessCode accessCode, Secret secret) {
    val abortCommand = new TaskAbortCommand(taskId, accessCode, secret);
    val abortResponse = actor.erpRequest(abortCommand);

    ActorContext.getInstance().removeAcceptedPrescription(taskId);
    return Response.status(abortResponse.getStatusCode()).build();
  }
}
