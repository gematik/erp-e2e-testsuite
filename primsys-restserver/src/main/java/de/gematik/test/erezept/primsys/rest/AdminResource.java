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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.gematik.test.erezept.fhir.parser.profiles.cfg.ParserConfigurations;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@Path("admin")
public class AdminResource {

  @POST
  @Path("config")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @ApiResponse(description = "Administration of the service")
  public Response changeConfig(@QueryParam("key") String key, AdminRequest request) {
    val adminKey = System.getenv().getOrDefault("COMMIT_HASH", "");
    if (adminKey == null || adminKey.isBlank() || adminKey.isEmpty() || !adminKey.equals(key)) {
      return Response.status(403).entity(AdminResponse.withMessage("invalid key")).build();
    }

    if (request != null) {
      if (request.profile != null) {
        System.setProperty(ParserConfigurations.SYS_PROP_TOGGLE, request.profile.toString());
      }

      if (request.defaultMedicationDispense != null) {
        // see ErxMedicationDispenseBuilder
        System.setProperty(
            "erp.fhir.medicationdispense.default", request.defaultMedicationDispense.toString());
      }

      return Response.accepted(AdminResponse.withMessage("configuration changed")).build();
    } else {
      return Response.status(400).entity(AdminResponse.withMessage("nothing to change")).build();
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
    private Profile profile;
    private Boolean defaultMedicationDispense;
  }

  public enum Profile {
    @JsonProperty("1.1.1")
    V_1_1_1,
    @JsonProperty("1.2.0")
    @JsonAlias("1.2")
    V_1_2_0;

    @Override
    public String toString() {
      return this.name().replace("V_", "").replace("_", ".").toLowerCase();
    }
  }
}
