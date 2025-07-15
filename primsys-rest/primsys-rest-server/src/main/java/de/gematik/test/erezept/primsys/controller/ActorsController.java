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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.primsys.actors.BaseActor;
import de.gematik.test.erezept.primsys.data.actors.ActorDto;
import de.gematik.test.erezept.primsys.data.actors.ActorType;
import de.gematik.test.erezept.primsys.model.ActorContext;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@Path("actors")
public class ActorsController {

  static ActorContext ctx = ActorContext.getInstance();

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<ActorDto> getAll(@QueryParam("role") String actorTypeValue) {
    log.info("GET summary about all actors");

    if (actorTypeValue == null) {
      return ctx.getActorsSummary();
    } else {
      val actorType =
          ActorType.optionalFromString(actorTypeValue)
              .orElseThrow(
                  () ->
                      ErrorResponseBuilder.createInternalErrorException(
                          400, format("Given Role {0} is invalid", actorTypeValue)));
      return ctx.getActorsSummary(actorType);
    }
  }

  @GET
  @Path("{actorId}")
  @Produces(MediaType.APPLICATION_JSON)
  public ActorDto getActor(@PathParam("actorId") String actorId) {
    log.info("GET actor with ID {}", actorId);
    return ctx.getActors().stream()
        .filter(actor -> actor.getIdentifier().equals(actorId))
        .map(BaseActor::getActorSummary)
        .findFirst()
        .orElseThrow(
            () ->
                ErrorResponseBuilder.createInternalErrorException(
                    404, format("No Actor with ID {0} found", actorId)));
  }
}
