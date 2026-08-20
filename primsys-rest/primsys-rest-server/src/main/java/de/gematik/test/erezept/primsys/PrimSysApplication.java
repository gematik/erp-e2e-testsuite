/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.primsys;

import de.gematik.test.erezept.primsys.exceptions.GenericExceptionMapper;
import de.gematik.test.erezept.primsys.exceptions.IdpClientExceptionMapper;
import de.gematik.test.erezept.primsys.exceptions.JacksonExceptionMapper;
import de.gematik.test.erezept.primsys.exceptions.NotFoundExceptionMapper;
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
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.jakarta.rs.json.JacksonJsonProvider;
import tools.jackson.jakarta.rs.xml.JacksonXMLProvider;

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
    packages("de.gematik.test.erezept.primsys.controller");
    val jsonProvider = new JacksonJsonProvider();
    jsonProvider
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
        .enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
    register(jsonProvider);

    val xmlProvider = new JacksonXMLProvider();
    register(xmlProvider);

    register(new CORSFilter());
    register(JacksonExceptionMapper.class);
    register(NotFoundExceptionMapper.class);
    register(IdpClientExceptionMapper.class);
    register(GenericExceptionMapper.class);

    property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, "true");

    // swagger
    register(OpenApiResource.class);
    register(SwaggerSerializers.class);
  }

  @Override
  public String getApplicationName() {
    return "PrimSys";
  }
}
