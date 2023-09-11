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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.primsys.model.ActorContext;
import de.gematik.test.erezept.primsys.rest.data.AcceptData;
import de.gematik.test.erezept.primsys.rest.data.DispensedData;
import de.gematik.test.erezept.primsys.rest.data.PrescriptionData;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponse;
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
  public List<PrescriptionData> getPrescriptions() {
    return getPrescribed();
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public PrescriptionData getPrescription(@PathParam("id") String id) {
    return getPrescribed(id);
  }

  @GET
  @Path("prescribed")
  @Produces(MediaType.APPLICATION_JSON)
  public List<PrescriptionData> getPrescribed() {
    return ctx.getPrescriptions();
  }

  @GET
  @Path("prescribed/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public PrescriptionData getPrescribed(@PathParam("id") String id) {
    log.info("GET /prescription/" + id);
    return ctx.getPrescriptions().stream()
        .filter(p -> p.getTaskId().equals(id))
        .findFirst()
        .orElseThrow(
            () ->
                new WebApplicationException(
                    Response.status(404)
                        .entity(new ErrorResponse(format("No Prescription found with ID {0}", id)))
                        .build()));
  }

  @GET
  @Path("accepted")
  @Produces(MediaType.APPLICATION_JSON)
  public List<AcceptData> getAccepted() {
    return ctx.getAcceptedPrescriptions();
  }

  @GET
  @Path("accepted/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public AcceptData getAccepted(@PathParam("id") String id) {
    return ctx.getAcceptedPrescriptions().stream()
        .filter(p -> p.getTaskId().equals(id))
        .findFirst()
        .orElseThrow(
            () ->
                new WebApplicationException(
                    Response.status(404)
                        .entity(
                            new ErrorResponse(format("Prescription {0} has not been accepted", id)))
                        .build()));
  }

  @GET
  @Path("dispensed")
  @Produces(MediaType.APPLICATION_JSON)
  public List<DispensedData> getDispensed() {
    return ctx.getDispensedMedications();
  }

  @GET
  @Path("dispensed/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public DispensedData getDispensed(@PathParam("id") String id) {
    return ctx.getDispensedMedications().stream()
        .filter(p -> p.getTaskId().equals(id))
        .findFirst()
        .orElseThrow(
            () ->
                new WebApplicationException(
                    Response.status(404)
                        .entity(
                            new ErrorResponse(
                                format("Prescription {0} has not been dispensed", id)))
                        .build()));
  }

  @GET
  @Path("dmc/{id}")
  @Produces("image/png")
  public Response getDataMatrixCode(@PathParam("id") String id) {
    val prescription =
        ctx.getPrescriptions().stream()
            .filter(p -> p.getTaskId().equals(id))
            .findFirst()
            .orElseThrow(
                () ->
                    new WebApplicationException(
                        Response.status(404)
                            .entity(new ErrorResponse("No Prescription found with ID " + id))
                            .build()));

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
