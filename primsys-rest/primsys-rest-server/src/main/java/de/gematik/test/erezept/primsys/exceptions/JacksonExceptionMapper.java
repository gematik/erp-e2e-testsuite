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
 */

package de.gematik.test.erezept.primsys.exceptions;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.core.JacksonException;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.val;

@Provider
public class JacksonExceptionMapper implements ExceptionMapper<JacksonException> {

  @Override
  public Response toResponse(JacksonException exception) {
    val message = format("Invalid JSON Body: {0}", exception.getMessage());
    return ErrorResponseBuilder.createInternalErrorException(400, message).getResponse();
  }
}
