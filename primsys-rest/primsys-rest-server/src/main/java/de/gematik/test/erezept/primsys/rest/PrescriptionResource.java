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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.primsys.data.AcceptedPrescriptionDto;
import de.gematik.test.erezept.primsys.data.DispensedMedicationDto;
import de.gematik.test.erezept.primsys.data.PrescriptionDto;
import de.gematik.test.erezept.primsys.model.ActorContext;
import de.gematik.test.erezept.primsys.rest.params.PrescriptionFilterParams;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import de.gematik.test.erezept.screenplay.util.DataMatrixCodeGenerator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@Path("prescription")
public class PrescriptionResource {

  private static final ActorContext ctx = ActorContext.getInstance();

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<PrescriptionDto> getPrescriptions(@BeanParam PrescriptionFilterParams params) {
    return getPrescribed(params);
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public PrescriptionDto getPrescription(@PathParam("id") String id) {
    return getPrescribed(id);
  }

  @GET
  @Path("prescribed")
  @Produces(MediaType.APPLICATION_JSON)
  public List<PrescriptionDto> getPrescribed(@BeanParam PrescriptionFilterParams params) {
    log.info("GET all open prescriptions with filter {}", params);
    return ctx.getPrescriptions(params);
  }

  @GET
  @Path("prescribed/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public PrescriptionDto getPrescribed(@PathParam("id") String id) {
    log.info("GET open prescription with ID {}", id);
    return ctx.getPrescription(id)
        .orElseThrow(
            () ->
                ErrorResponseBuilder.createInternalErrorException(
                    404, format("No Prescription found with ID {0}", id)));
  }

  @GET
  @Path("accepted")
  @Produces(MediaType.APPLICATION_JSON)
  public List<AcceptedPrescriptionDto> getAccepted(@BeanParam PrescriptionFilterParams params) {
    log.info("GET all accepted prescriptions with filter {}", params);
    return ctx.getAcceptedPrescriptions(params);
  }

  @GET
  @Path("accepted/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public AcceptedPrescriptionDto getAccepted(@PathParam("id") String id) {
    log.info("GET accepted prescription with ID {}", id);
    return ctx.getAcceptedPrescription(id)
        .orElseThrow(
            () ->
                ErrorResponseBuilder.createInternalErrorException(
                    404, format("Prescription {0} has not been accepted", id)));
  }

  @GET
  @Path("dispensed")
  @Produces(MediaType.APPLICATION_JSON)
  public List<DispensedMedicationDto> getDispensed(@BeanParam PrescriptionFilterParams params) {
    log.info("GET all dispensed prescriptions with filter {}", params);
    return ctx.getDispensedMedications(params);
  }

  @GET
  @Path("dispensed/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public DispensedMedicationDto getDispensed(@PathParam("id") String id) {
    log.info("GET dispensed prescription with ID {}", id);
    return ctx.getDispensedMedication(id)
        .orElseThrow(
            () ->
                ErrorResponseBuilder.createInternalErrorException(
                    404, format("Prescription {0} has not been dispensed", id)));
  }

  @GET
  @Path("dmc/{id}")
  @Produces("image/png")
  public Response getDataMatrixCode(@PathParam("id") String id) {
    val prescription = this.getPrescribed(id);

    val baoDmc =
        DataMatrixCodeGenerator.writeToStream(
            prescription.getTaskId(), new AccessCode(prescription.getAccessCode()));
    return Response.ok(baoDmc.toByteArray()).build();
  }

  @GET
  @Path("dmc/render")
  @Produces("image/png")
  public Response renderDataMatrixCode(
      @QueryParam("taskId") String taskId, @QueryParam("ac") String accessCode) {
    val baoDmc = DataMatrixCodeGenerator.writeToStream(taskId, new AccessCode(accessCode));
    return Response.ok(baoDmc.toByteArray()).build();
  }
}
