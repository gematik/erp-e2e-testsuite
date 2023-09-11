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

package de.gematik.test.erezept.primsys;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.jaxrs2.SwaggerSerializers;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.ws.rs.ApplicationPath;
import lombok.val;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;

@OpenAPIDefinition(
    info =
        @Info(
            description = "PrimSys is an easy REST-Wrapper for ePrescriptions",
            title = "E-Rezept PrimSys",
            // Note: should this be a different version from the server? versioning the API itself?
            version = "v0.3.0",
            contact = @Contact(name = "Gematik", url = "https://www.gematik.de/"),
            license =
                @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0")),
    servers = {
      @Server(description = "local development server", url = "http://localhost:9095"),
      @Server(
          description = "TU-Proxy @ gematik.solutions",
          url = "https://erpps-test.dev.gematik.solutions/tu"),
      @Server(
          description = "RU-Proxy @ gematik.solutions",
          url = "https://erpps-test.dev.gematik.solutions/ru"),
      @Server(
          description = "RU-DEV-Proxy @ gematik.solutions",
          url = "https://erpps-test.dev.gematik.solutions/rudev")
    },
    security = {@SecurityRequirement(name = "apikey")},
    externalDocs =
        @ExternalDocumentation(
            description = "E-Rezept E2E Testsuite",
            url = "https://github.com/gematik/erp-e2e-testsuite"))
@SecuritySchemes(
    value = {
      @SecurityScheme(
          type = SecuritySchemeType.APIKEY,
          name = "apikey",
          in = SecuritySchemeIn.HEADER,
          paramName = "apikey")
    })
@ApplicationPath("")
public class PrimSysApplication extends ResourceConfig {

  public PrimSysApplication() {
    packages("de.gematik.test.erezept.primsys.rest");
    val mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    val provider = new JacksonJsonProvider(mapper);
    register(provider);
    register(new CORSFilter()); // will allow Cross Origin Resource Sharing

    // swagger
    register(OpenApiResource.class);
    register(SwaggerSerializers.class);
  }

  @Override
  public String getApplicationName() {
    return "PrimSys";
  }
}
