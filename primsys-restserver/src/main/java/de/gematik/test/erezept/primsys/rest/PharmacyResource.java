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

package de.gematik.test.erezept.primsys.rest;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.primsys.model.AcceptUseCase;
import de.gematik.test.erezept.primsys.model.ActorContext;
import de.gematik.test.erezept.primsys.model.RejectUseCase;
import de.gematik.test.erezept.primsys.model.actor.Pharmacy;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@Path("pharm")
public class PharmacyResource {

  static ActorContext actors = ActorContext.getInstance();

  @POST
  @Path("{id}/accept")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response acceptPrescription(
      @PathParam("id") String id,
      @QueryParam("taskId") String taskId,
      @QueryParam("ac") String accessCode) {
    val pharmacy = getPharmacy(id);
    log.info(
        format("Pharmacy {0} will accept Task {1} with accessCode {2}", id, taskId, accessCode));
    return AcceptUseCase.acceptPrescription(pharmacy, taskId, accessCode);
  }

  @PUT
  @Path("{id}/reject")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response rejectPrescription(
      @PathParam("id") String id,
      @QueryParam("taskId") String taskId,
      @QueryParam("ac") String accessCode,
      @QueryParam("secret") String secret) {
    val pharmacy = getPharmacy(id);
    log.info(
        format("Pharmacy {0} will reject Task {1} with accessCode {2}", id, taskId, accessCode));
    return RejectUseCase.rejectPrescription(pharmacy, taskId, accessCode, secret);
  }

  private Pharmacy getPharmacy(String id) {
    return actors
        .getPharmacy(id)
        .orElseThrow(
            () ->
                new WebApplicationException(
                    Response.status(404)
                        .entity(new ErrorResponse(format("No Pharmacy found with ID {0}", id)))
                        .build()));
  }
}