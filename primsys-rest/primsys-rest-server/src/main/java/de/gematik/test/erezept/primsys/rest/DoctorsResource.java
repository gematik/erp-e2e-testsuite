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

import de.gematik.test.erezept.primsys.data.PrescribeRequestDto;
import de.gematik.test.erezept.primsys.model.AbortUseCase;
import de.gematik.test.erezept.primsys.model.ActorContext;
import de.gematik.test.erezept.primsys.model.PrescribeDiGa;
import de.gematik.test.erezept.primsys.model.PrescribePharmaceuticals;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@Path("doc")
public class DoctorsResource {

  static ActorContext actors = ActorContext.getInstance();

  @POST
  @Path("{doctorId}/prescribe")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response issuePrescription(
      @PathParam("doctorId") String doctorId,
      @DefaultValue("false") @QueryParam("direct") boolean isDirectAssignment,
      PrescribeRequestDto body) {
    log.info(
        "POST/doc/{id}/prescribe with PrescribeRequest as json for Doctor with ID {}", doctorId);
    val doctor = actors.getDoctorOrThrowNotFound(doctorId);
    return PrescribePharmaceuticals.as(doctor).assignDirectly(isDirectAssignment).withDto(body);
  }

  @POST
  @Path("{doctorId}/xml/prescribe")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_XML)
  public Response issuePrescription(
      @PathParam("doctorId") String doctorId,
      @DefaultValue("false") @QueryParam("direct") boolean isDirectAssignment,
      String kbvBundle) {
    val doctor = actors.getDoctorOrThrowNotFound(doctorId);
    log.info(
        "Doctor {} will issue a prescription from KBV-Bundle as direct assignment={}",
        doctor.getName(),
        isDirectAssignment);
    return PrescribePharmaceuticals.as(doctor)
        .assignDirectly(isDirectAssignment)
        .withKbvBundle(kbvBundle);
  }

  @POST
  @Path("{doctorId}/xml/evdga")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_XML)
  public Response issueDiGAPrescription(
      @PathParam("doctorId") String doctorId, String evdgaBundle) {
    val doctor = actors.getDoctorOrThrowNotFound(doctorId);
    log.info("Doctor {} will issue a DiGA prescription", doctor.getName());
    return PrescribeDiGa.as(doctor).withEvdga(evdgaBundle);
  }

  @DELETE
  @Path("{doctorId}/abort")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response abortPrescriptionAsDoc(
      @PathParam("doctorId") String doctorId,
      @QueryParam("taskId") String taskId,
      @QueryParam("ac") String accessCode,
      @QueryParam("secret") String secret) {
    val doctor = actors.getDoctorOrThrowNotFound(doctorId);
    log.info("Doctor {} will abort Task {}", doctor.getName(), taskId);
    return AbortUseCase.abortPrescription(doctor, taskId, accessCode, secret);
  }
}
