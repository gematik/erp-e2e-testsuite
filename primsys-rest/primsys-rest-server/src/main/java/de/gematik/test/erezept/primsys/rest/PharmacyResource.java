/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.primsys.rest;

import de.gematik.test.erezept.primsys.data.PznDispensedMedicationDto;
import de.gematik.test.erezept.primsys.model.AbortUseCase;
import de.gematik.test.erezept.primsys.model.AcceptUseCase;
import de.gematik.test.erezept.primsys.model.ActorContext;
import de.gematik.test.erezept.primsys.model.ChargeItemUseCase;
import de.gematik.test.erezept.primsys.model.CloseUseCase;
import de.gematik.test.erezept.primsys.model.DeleteCommunicationUseCase;
import de.gematik.test.erezept.primsys.model.DispenseUseCase;
import de.gematik.test.erezept.primsys.model.GetCommunicationsUseCase;
import de.gematik.test.erezept.primsys.model.GetPrescriptionsWithPNUseCase;
import de.gematik.test.erezept.primsys.model.RejectUseCase;
import de.gematik.test.erezept.primsys.model.ReplyUseCase;
import de.gematik.test.erezept.primsys.rest.data.InvoiceData;
import de.gematik.test.erezept.primsys.rest.params.CommunicationFilterParams;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@Path("pharm")
public class PharmacyResource {

  static ActorContext actors = ActorContext.getInstance();

  @POST
  @Path("{pharmacyId}/accept")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response acceptPrescription(
      @PathParam("pharmacyId") String pharmacyId,
      @QueryParam("taskId") String taskId,
      @QueryParam("ac") String accessCode) {
    val pharmacy = actors.getPharmacyOrThrowNotFound(pharmacyId);
    log.info(
        "Pharmacy {} will accept Task {} with accessCode {}",
        pharmacy.getName(),
        taskId,
        accessCode);
    return AcceptUseCase.acceptPrescription(pharmacy, taskId, accessCode);
  }

  @POST
  @Path("{pharmacyId}/reject")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response rejectPrescription(
      @PathParam("pharmacyId") String pharmacyId,
      @QueryParam("taskId") String taskId,
      @QueryParam("ac") String accessCode,
      @QueryParam("secret") String secret) {
    val pharmacy = actors.getPharmacyOrThrowNotFound(pharmacyId);
    log.info(
        "Pharmacy {} will reject Task {} with accessCode {}",
        pharmacy.getName(),
        taskId,
        accessCode);
    return RejectUseCase.rejectPrescription(pharmacy, taskId, accessCode, secret);
  }

  @DELETE
  @Path("{pharmacyId}/abort")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response abortPrescription(
      @PathParam("pharmacyId") String pharmacyId,
      @QueryParam("taskId") String taskId,
      @QueryParam("ac") String accessCode,
      @QueryParam("secret") String secret) {
    val pharmacy = actors.getPharmacyOrThrowNotFound(pharmacyId);
    log.info("Pharmacy {} will abort Task {}", pharmacy.getName(), taskId);
    return AbortUseCase.abortPrescription(pharmacy, taskId, accessCode, secret);
  }

  @POST
  @Path("{pharmacyId}/close")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response closePrescription(
      @PathParam("pharmacyId") String pharmacyId,
      @QueryParam("taskId") String taskId,
      @QueryParam("secret") String secret,
      List<PznDispensedMedicationDto> body) {
    val pharmacy = actors.getPharmacyOrThrowNotFound(pharmacyId);
    log.info(
        "Pharmacy {} will close Task {} with dispense data {}", pharmacy.getName(), taskId, body);
    val usecase = new CloseUseCase(pharmacy);

    if (body == null || body.isEmpty()) {
      return usecase.closePrescription(taskId, secret);
    } else {
      return usecase.closePrescription(taskId, secret, body);
    }
  }

  @POST
  @Path("{pharmacyId}/dispense")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response dispensePrescription(
      @PathParam("pharmacyId") String pharmacyId,
      @QueryParam("taskId") String taskId,
      @QueryParam("secret") String secret,
      List<PznDispensedMedicationDto> body) {
    val pharmacy = actors.getPharmacyOrThrowNotFound(pharmacyId);
    log.info(
        "Pharmacy {} will dispense Task {} with dispense data {}",
        pharmacy.getName(),
        taskId,
        body);
    val usecase = new DispenseUseCase(pharmacy);

    if (body == null || body.isEmpty()) {
      return usecase.dispensePrescription(taskId, secret);
    } else {
      return usecase.dispensePrescription(taskId, secret, body);
    }
  }

  @POST
  @Path("{pharmacyId}/chargeitem")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createChargeItem(
      @PathParam("pharmacyId") String pharmacyId,
      @QueryParam("taskId") String taskId,
      @Nullable InvoiceData body) {
    val pharmacy = actors.getPharmacyOrThrowNotFound(pharmacyId);
    log.info(
        "Pharmacy {} will create a ChargeItem for Task {} with invoice data {}",
        pharmacy.getName(),
        taskId,
        body);
    return new ChargeItemUseCase(pharmacy).postChargeItem(taskId, body);
  }

  @POST // Note: DEVOPS-1400
  @Path("{pharmacyId}/put_chargeitem")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response changeChargeItem(
      @PathParam("pharmacyId") String pharmacyId,
      @QueryParam("taskId") String taskId,
      @QueryParam("ac") String accessCode,
      @Nullable InvoiceData body) {
    val pharmacy = actors.getPharmacyOrThrowNotFound(pharmacyId);
    log.info("Pharmacy {} will change the ChargeItem for Task {}", pharmacy.getName(), taskId);
    return new ChargeItemUseCase(pharmacy).putChargeItem(taskId, accessCode, body);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("{pharmacyId}/withEvidence")
  public Response getPrescriptionsByEvidence(
      @PathParam("pharmacyId") String pharmacyId, @QueryParam("evidence") String examEvidence) {
    val pharmacy = actors.getPharmacyOrThrowNotFound(pharmacyId);
    log.info(
        "Pharmacy {} will read all prescriptions with ExamEvidence '{}'",
        pharmacy.getName(),
        examEvidence);
    return new GetPrescriptionsWithPNUseCase(pharmacy).getPrescriptionsByEvidence(examEvidence);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("{pharmacyId}/withKvnr")
  public Response getPrescriptionsByKvnr(
      @PathParam("pharmacyId") String pharmacyId, @QueryParam("kvnr") String kvnr) {
    val pharmacy = actors.getPharmacyOrThrowNotFound(pharmacyId);
    log.info("Pharmacy {} will read all prescriptions for KVNR: {}", pharmacy.getName(), kvnr);
    return new GetPrescriptionsWithPNUseCase(pharmacy).getPrescriptionByKvnr(kvnr);
  }

  @POST
  @Path("{pharmacyId}/replyWithSender")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response replyPrescription(
      @PathParam("pharmacyId") String pharmacyId,
      @QueryParam("taskId") String taskId,
      @QueryParam("kvnr") String kvnr,
      @DefaultValue("onPremise") @QueryParam("supplyOption") String supplyOption,
      @QueryParam("sender") String sender,
      String messageBody) {
    val pharmacy = actors.getPharmacyOrThrowNotFound(pharmacyId);
    log.info(
        "POST/pharm/pharmaId/reply -- Pharmacy {} will reply Task {} on KVNR {} with Sender {}",
        pharmacyId,
        taskId,
        kvnr,
        sender);
    return ReplyUseCase.replyPrescriptionWithSender(
        pharmacy, taskId, kvnr, supplyOption, messageBody, sender);
  }

  @POST
  @Path("{pharmacyId}/reply")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response replyPrescriptionWithoutSender(
      @PathParam("pharmacyId") String pharmacyId,
      @QueryParam("taskId") String taskId,
      @QueryParam("kvnr") String kvnr,
      @DefaultValue("onPremise") @QueryParam("supplyOption") String supplyOption,
      String messageBody) {
    val pharmacy = actors.getPharmacyOrThrowNotFound(pharmacyId);
    log.info(
        "POST/pharm/pharmaId/reply -- Pharmacy {} will reply Task {} on KVNR {}",
        pharmacyId,
        taskId,
        kvnr);
    return ReplyUseCase.replyPrescription(pharmacy, taskId, kvnr, supplyOption, messageBody);
  }

  @GET
  @Path("{pharmacyId}/communications")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response getCommunications(
      @PathParam("pharmacyId") String pharmacyId, @BeanParam CommunicationFilterParams params) {
    val pharmacy = actors.getPharmacyOrThrowNotFound(pharmacyId);
    return new GetCommunicationsUseCase(pharmacy).getCommunications(params);
  }

  @DELETE
  @Path("{pharmacyId}/communication/{communicationId}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response deleteCommunication(
      @PathParam("pharmacyId") String pharmacyId,
      @PathParam("communicationId") String communicationId) {
    val pharmacy = actors.getPharmacyOrThrowNotFound(pharmacyId);
    return new DeleteCommunicationUseCase(pharmacy).forId(communicationId);
  }
}
