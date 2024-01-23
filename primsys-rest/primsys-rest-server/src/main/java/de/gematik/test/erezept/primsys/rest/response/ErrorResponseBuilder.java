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

package de.gematik.test.erezept.primsys.rest.response;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.fhir.util.OperationOutcomeWrapper;
import de.gematik.test.erezept.primsys.data.error.ErrorDto;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

public class ErrorResponseBuilder {

  private ErrorResponseBuilder() throws IllegalAccessException {
    throw new IllegalAccessException("utility class");
  }

  public static Response createInternalError(int statusCode, String message) {
    return Response.status(statusCode).entity(ErrorDto.internalError(message)).build();
  }

  public static WebApplicationException createInternalErrorException(
      int statusCode, String message) {
    return new WebApplicationException(createInternalError(statusCode, message));
  }

  public static void throwInternalError(int statusCode, String message)
      throws WebApplicationException {
    throw createInternalErrorException(statusCode, message);
  }

  public static <R extends Resource> Response createFachdienstError(ErpResponse<R> erpResponse) {
    val message = extractErrorMessage(erpResponse);
    return Response.status(erpResponse.getStatusCode())
        .entity(ErrorDto.fachdienstError(message))
        .build();
  }

  public static <R extends Resource> WebApplicationException createFachdienstErrorException(
      ErpResponse<R> erpResponse) {
    return new WebApplicationException(createFachdienstError(erpResponse));
  }

  public static <R extends Resource> void throwFachdienstError(ErpResponse<R> erpResponse)
      throws WebApplicationException {
    throw createFachdienstErrorException(erpResponse);
  }

  private static <R extends Resource> String extractErrorMessage(ErpResponse<R> erpResponse) {
    if (erpResponse.isOperationOutcome()) {
      val oo = erpResponse.getAsOperationOutcome();
      return OperationOutcomeWrapper.extractFrom(oo);
    } else if (erpResponse.isEmptyBody()) {
      throw ErrorResponseBuilder.createInternalErrorException(
          400, "Unknown Error: expected OperationOutcome but received an empty body");
    } else {
      val resourceType =
          Optional.ofNullable(erpResponse.getResourceType())
              .map(Class::getSimpleName)
              .orElse("empty body");
      throw ErrorResponseBuilder.createInternalErrorException(
          400, format("Expected OperationOutcome but received {0}", resourceType));
    }
  }
}
