/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.client.vau;

import static java.text.MessageFormat.format;

import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Response {
  @Getter private final int statusCode;
  @Getter private final Map<String, String> header;
  @Getter private final String body;
  @Getter private final String protocol;

  public Response(String protocol, int statusCode, Map<String, String> header, String body) {
    this.statusCode = statusCode;
    this.header = header;
    this.body = body;
    this.protocol = protocol;
    log.info(format("Inner-HTTP Response: {0} with Code {1}", protocol, statusCode));
    if (body.length() > 0) {
      log.debug(format("Received Inner-HTTP Response Body:\n<xmp>{0}</xmp>", body));
    } else {
      log.debug("Received Inner-HTTP with empty Response Body");
    }
  }
}
