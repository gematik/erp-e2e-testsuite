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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Strings;
import de.gematik.test.erezept.fhir.parser.ProfileFhirParserFactory;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@Path("admin")
public class AdminController {

  @POST
  @Path("config")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiResponse(description = "Administration of the service")
  public Response changeConfig(@QueryParam("key") String key, AdminRequest request) {
    val adminKey = System.getenv().getOrDefault("COMMIT_HASH", "");
    if (Strings.isNullOrEmpty(key) || !adminKey.equals(key)) {
      return ErrorResponseBuilder.createInternalError(403, "invalid key");
    }

    if (request != null) {
      if (request.profile != null) {
        System.setProperty(
            ProfileFhirParserFactory.ERP_FHIR_PROFILES_TOGGLE, request.profile.getVersion());
      }

      return Response.accepted(AdminResponse.withMessage("configuration changed")).build();
    } else {
      return ErrorResponseBuilder.createInternalError(400, "nothing to change");
    }
  }

  @Data
  public static class AdminResponse {
    private String message;

    public static AdminResponse withMessage(String message) {
      val response = new AdminResponse();
      response.message = message;
      return response;
    }
  }

  @Data
  public static class AdminRequest {

    @JsonDeserialize(using = ProfileVersionDeserializer.class)
    private ErpWorkflowVersion profile;
  }

  /**
   * Custom deserializer for {@link ErpWorkflowVersion} By using a custom deserializer we can re-use
   * the {@link ErpWorkflowVersion} without bothering about updating new versions here separately
   *
   * <p><b>NOTE:</b> this approach works only as long we are using the {@link ErpWorkflowVersion}
   * also for the whole "ProfileSet"
   */
  static class ProfileVersionDeserializer extends StdDeserializer<ErpWorkflowVersion> {

    protected ProfileVersionDeserializer() {
      super(ErpWorkflowVersion.class);
    }

    @Override
    public ErpWorkflowVersion deserialize(JsonParser parser, DeserializationContext ctxt)
        throws IOException {
      val value = parser.getText();
      return Arrays.stream(ErpWorkflowVersion.values())
          .filter(v -> v.getVersion().equals(value) || v.name().equalsIgnoreCase(value))
          .findFirst()
          .orElseThrow(
              () ->
                  ErrorResponseBuilder.createInternalErrorException(
                      400,
                      format(
                          "Invalid profile: {0} cannot be mapped to a valid profile version",
                          value)));
    }
  }
}
