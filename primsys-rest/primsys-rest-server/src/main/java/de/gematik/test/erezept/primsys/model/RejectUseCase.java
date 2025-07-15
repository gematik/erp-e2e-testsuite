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

package de.gematik.test.erezept.primsys.model;

import de.gematik.test.erezept.client.usecases.TaskRejectCommand;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.primsys.actors.BaseActor;
import de.gematik.test.erezept.primsys.actors.HealthInsurance;
import de.gematik.test.erezept.primsys.actors.Pharmacy;
import jakarta.ws.rs.core.Response;
import lombok.val;

public class RejectUseCase {

  private final BaseActor actor;

  public RejectUseCase(Pharmacy pharmacy) {
    this.actor = pharmacy;
  }

  public RejectUseCase(HealthInsurance ktr) {
    this.actor = ktr;
  }

  public Response rejectPrescription(String taskId, String accessCode, String secret) {
    return rejectPrescription(
        TaskId.from(taskId), AccessCode.from(accessCode), Secret.from(secret));
  }

  public Response rejectPrescription(TaskId taskId, AccessCode accessCode, Secret secret) {
    val rejectCommand = new TaskRejectCommand(taskId, accessCode, secret);
    val rejectResponse = this.actor.erpRequest(rejectCommand);

    ActorContext.getInstance().removeAcceptedPrescription(taskId);
    return Response.status(rejectResponse.getStatusCode()).build();
  }
}
