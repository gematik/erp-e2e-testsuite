/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.primsys.model;

import de.gematik.test.erezept.client.usecases.TaskAcceptCommand;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.primsys.model.actor.Pharmacy;
import de.gematik.test.erezept.primsys.rest.data.AcceptData;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.val;

public class AcceptUseCase {

  private AcceptUseCase() {
    throw new AssertionError();
  }

  public static Response acceptPrescription(Pharmacy actor, String taskId, String accessCode) {
    return acceptPrescription(actor, taskId, new AccessCode(accessCode));
  }

  public static Response acceptPrescription(Pharmacy actor, String taskId, AccessCode accessCode) {
    val acceptCommand = new TaskAcceptCommand(taskId, accessCode);
    val acceptResponse = actor.erpRequest(acceptCommand);
    val acceptedTask =
        acceptResponse
            .getResourceOptional(acceptCommand.expectedResponseBody())
            .orElseThrow(
                () ->
                    new WebApplicationException(
                        Response.status(acceptResponse.getStatusCode())
                            .entity(new ErrorResponse(acceptResponse))
                            .build()));

    val acceptData = new AcceptData();
    acceptData.setTaskId(acceptedTask.getTask().getUnqualifiedId());
    acceptData.setAccessCode(acceptedTask.getTask().getAccessCode().getValue());
    acceptData.setSecret(acceptedTask.getSecret().getValue());
    ActorContext.getInstance().addAcceptedPrescription(acceptData);

    return Response.accepted(acceptData).build();
  }
}
