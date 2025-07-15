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

package de.gematik.test.erezept.primsys.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.primsys.data.error.ErrorDto;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.glassfish.grizzly.http.server.ErrorPageGenerator;
import org.glassfish.grizzly.http.server.Request;

@Slf4j
public class PrimSysErrorPageGenerator implements ErrorPageGenerator {

  @SneakyThrows
  @Override
  public String generate(
      final Request request,
      final int status,
      final String reasonPhrase,
      final String description,
      final Throwable exception) {

    log.error(
        "Error while processing request: {} - {} {}\n",
        request.getContextPath(),
        status,
        reasonPhrase,
        exception);

    // trick Grizzly to response with JSON
    request.getResponse().setContentType("application/json");
    request.getResponse().setStatus(400);

    val mapper = new ObjectMapper();
    return mapper.writeValueAsString(ErrorDto.internalError("Bad Request"));
  }
}
