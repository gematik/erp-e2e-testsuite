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

package de.gematik.test.erezept.primsys.controller;

import de.gematik.test.erezept.primsys.model.AbortUseCase;
import de.gematik.test.erezept.primsys.model.AcceptDiGA;
import de.gematik.test.erezept.primsys.model.ActorContext;
import de.gematik.test.erezept.primsys.model.CloseDiGA;
import de.gematik.test.erezept.primsys.model.RejectUseCase;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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
@Path("ktr")
public class KtrController {

  private static final ActorContext actors = ActorContext.getInstance();

  @POST
  @Path("{ktrId}/accept")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response acceptPrescription(
      @PathParam("ktrId") String ktrId,
      @QueryParam("taskId") String taskId,
      @QueryParam("ac") String accessCode) {
    val ktr = actors.getHealthInsuranceOrThrowNotFound(ktrId);
    log.info("KTR {} will accept Task {} with accessCode {}", ktr.getName(), taskId, accessCode);
    return new AcceptDiGA(ktr).acceptPrescription(taskId, accessCode);
  }

  @POST
  @Path("{ktrId}/close")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response closePrescription(
      @PathParam("ktrId") String ktrId,
      @QueryParam("taskId") String taskId,
      @QueryParam("secret") String secret) {
    val ktr = actors.getHealthInsuranceOrThrowNotFound(ktrId);
    log.info("KTR {} will close DiGA-Task {}", ktr.getName(), taskId);
    return new CloseDiGA(ktr).closePrescription(taskId, secret);
  }

  @POST
  @Path("{ktrId}/decline")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response declinePrescription(
      @PathParam("ktrId") String ktrId,
      @QueryParam("taskId") String taskId,
      @QueryParam("secret") String secret) {
    val ktr = actors.getHealthInsuranceOrThrowNotFound(ktrId);
    log.info("KTR {} will decline DiGA-Task {}", ktr.getName(), taskId);
    return new CloseDiGA(ktr).declinePrescription(taskId, secret);
  }

  @POST
  @Path("{ktrId}/reject")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response rejectPrescription(
      @PathParam("ktrId") String ktrId,
      @QueryParam("taskId") String taskId,
      @QueryParam("ac") String accessCode,
      @QueryParam("secret") String secret) {
    val ktr = actors.getHealthInsuranceOrThrowNotFound(ktrId);
    log.info("KTR {} will reject Task {} with accessCode {}", ktr.getName(), taskId, accessCode);
    return new RejectUseCase(ktr).rejectPrescription(taskId, accessCode, secret);
  }

  @DELETE
  @Path("{ktrId}/abort")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response abortPrescription(
      @PathParam("ktrId") String ktrId,
      @QueryParam("taskId") String taskId,
      @QueryParam("ac") String accessCode,
      @QueryParam("secret") String secret) {
    val ktr = actors.getHealthInsuranceOrThrowNotFound(ktrId);
    log.info("KTR {} will abort Task {}", ktr.getName(), taskId);
    return new AbortUseCase(ktr).abortPrescription(taskId, accessCode, secret);
  }
}
