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

import de.gematik.test.erezept.primsys.model.ActorContext;
import de.gematik.test.erezept.primsys.model.PrescribeUseCase;
import de.gematik.test.erezept.primsys.rest.request.PrescribeRequest;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@Path("doc")
public class DoctorsResource {

  static ActorContext actors = ActorContext.getInstance();

  @POST
  @Path("{id}/prescribe")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response issuePrescription(@PathParam("id") String id, PrescribeRequest body) {
    log.info("POST /doc for Doctor with ID " + id);
    val doc =
        actors
            .getDoctor(id)
            .orElseThrow(
                () ->
                    new WebApplicationException(
                        Response.status(404)
                            .entity(new ErrorResponse(format("No Doctor found with ID {0}", id)))
                            .build()));

    return PrescribeUseCase.issuePrescription(doc, body);
  }

  @POST
  @Path("{id}/xml/prescribe")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_XML)
  public Response issuePrescription(@PathParam("id") String id, String kbvBundle) {
    val doc =
        actors
            .getDoctor(id)
            .orElseThrow(
                () ->
                    new WebApplicationException(
                        Response.status(404)
                            .entity(new ErrorResponse(format("No Doctor found with ID {0}", id)))
                            .build()));

    return PrescribeUseCase.issuePrescription(doc, kbvBundle);
  }
}
