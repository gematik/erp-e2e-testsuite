/*
 * Copyright (c) 2023 gematik GmbH
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

import de.gematik.test.erezept.primsys.model.AbortUseCase;
import de.gematik.test.erezept.primsys.model.AcceptUseCase;
import de.gematik.test.erezept.primsys.model.ActorContext;
import de.gematik.test.erezept.primsys.model.CloseUseCase;
import de.gematik.test.erezept.primsys.model.CreateChargeItemUseCase;
import de.gematik.test.erezept.primsys.model.RejectUseCase;
import de.gematik.test.erezept.primsys.model.ReplyUseCase;
import de.gematik.test.erezept.primsys.model.actor.Pharmacy;
import de.gematik.test.erezept.primsys.rest.data.InvoiceData;
import de.gematik.test.erezept.primsys.rest.request.DispenseRequestData;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.annotation.Nullable;

import static java.text.MessageFormat.format;

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

  @POST
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

  @POST
  @Path("{pharmaId}/reply")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response replyPrescription(
      @PathParam("pharmaId") String pharmaId,
      @QueryParam("taskId") String taskId,
      @QueryParam("kvnr") String kvnr,
      @DefaultValue("onPremise") @QueryParam("supplyOption") String supplyOption,
      String messageBody) {
    val pharmacy = getPharmacy(pharmaId);
    log.info(
        format(
            "POST/pharm/pharmaId/reply -- Pharmacy {0} will reply Task {1} on KVNR {2}",
            pharmaId, taskId, kvnr));
    return ReplyUseCase.replyPrescription(pharmacy, taskId, kvnr, supplyOption, messageBody);
  }

  @DELETE
  @Path("{id}/abort")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response abortPrescriptionAsPharma(
      @PathParam("id") String id,
      @QueryParam("taskId") String taskId,
      @QueryParam("ac") String accessCode,
      @QueryParam("secret") String secret) {
    log.info(
        format("DELETE/pharm/id/abort for Pharmacy with ID {0} the Task with Id {1}", id, taskId));
    val pharma = getPharmacy(id);
    return AbortUseCase.abortPrescription(pharma, taskId, accessCode, secret);
  }

  @POST
  @Path("{id}/close")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response closePrescriptionAsPharmaPKV(
      @PathParam("id") String id,
      @QueryParam("taskId") String taskId,
      @QueryParam("secret") String secret,
      DispenseRequestData body) {
    log.info(
        format(
            "POST/pharm/id/close for Pharmacy with ID {0} the Task with Id {1} and Data to dispense {2}",
            id, taskId, body));
    val pharma = getPharmacy(id);
    if (body != null) {
      return CloseUseCase.closePrescription(pharma, taskId, secret, body);
    } else {
      return CloseUseCase.closePrescription(pharma, taskId, secret);
    }
  }

  @POST
  @Path("{id}/chargeitem")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createChargeItem(
          @PathParam("id") String id, @QueryParam("taskId") String taskId, @Nullable InvoiceData body) {
    log.info(
            format(
                    "POST .../pharm/{0}/chargeitem/?taskId={1} + body: {2}",
                    id, taskId, body));
    val pharma = getPharmacy(id);
    return new CreateChargeItemUseCase().postChargeItem(pharma, taskId, body);
  }
}
