/*
 * Copyright 2023 gematik GmbH
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

import de.gematik.test.erezept.fhir.anonymizer.AnonymizerFacade;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.primsys.model.ActorContext;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@Path("anonymize")
public class AnonymizerResource {

  private static ActorContext actors = ActorContext.getInstance();
  private final AnonymizerFacade anonymizer = new AnonymizerFacade();

  @POST
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ApiResponse(description = "Anonymizes the given Resource")
  public Response anonymize(String content) {
    val actor = actors.getActors().get(0);

    try {
      val encodingType = EncodingType.guessFromContent(content);
      val resource = actor.getClient().getFhir().decode(content);
      val anonymized = anonymizer.anonymize(resource);

      if (anonymized) {
        val anonymizedContent = actor.getClient().getFhir().encode(resource, encodingType);
        return Response.ok(anonymizedContent).build();
      } else {
        val profile = extractProfile(resource).orElse("no profile");
        val errorMessage =
            "Anonymization failed: the given resource does not seem to be from ERP-context: "
                + profile;
        return ErrorResponseBuilder.createInternalError(400, errorMessage);
      }
    } catch (Throwable t) {
      return ErrorResponseBuilder.createInternalError(t);
    }
  }

  private Optional<String> extractProfile(Resource resource) {
    val meta = resource.getMeta();
    val profiles = meta.getProfile();
    if (profiles != null && !profiles.isEmpty()) {
      return Optional.ofNullable(profiles.get(0).getValue());
    } else {
      return Optional.empty();
    }
  }
}
