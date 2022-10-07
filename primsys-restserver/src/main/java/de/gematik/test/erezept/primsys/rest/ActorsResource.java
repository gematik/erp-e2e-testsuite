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
import de.gematik.test.erezept.primsys.model.actor.ActorRole;
import de.gematik.test.erezept.primsys.model.actor.BaseActor;
import de.gematik.test.erezept.primsys.rest.data.ActorData;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@Path("actors")
public class ActorsResource {

  static ActorContext ctx = ActorContext.getInstance();

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<ActorData> getAll(@DefaultValue("all") @QueryParam("role") String roleValue) {
    log.info("GET /actors " + roleValue);

    List<ActorData> ret;

    if (roleValue.equals("all") || roleValue.isEmpty()) {
      ret = ctx.getActors().stream().map(BaseActor::getBaseData).collect(Collectors.toList());
    } else {
      val role =
          ActorRole.optionalFromString(roleValue)
              .orElseThrow(
                  () ->
                      new WebApplicationException(
                          Response.status(400)
                              .entity(
                                  new ErrorResponse(format("Given Role {0} is invalid", roleValue)))
                              .build()));

      ret =
          ctx.getActors().stream()
              .filter(actor -> actor.getRole().equals(role))
              .map(BaseActor::getBaseData)
              .collect(Collectors.toList());
    }

    return ret;
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public ActorData getActor(@PathParam("id") String id) {
    log.info("GET actor with ID " + id);
    return ctx.getActors().stream()
        .filter(actor -> actor.getIdentifier().equals(id))
        .map(BaseActor::getBaseData)
        .findFirst()
        .orElseThrow(
            () ->
                new WebApplicationException(
                    Response.status(404)
                        .entity(new ErrorResponse("No Actor found with ID " + id))
                        .build()));
  }
}
