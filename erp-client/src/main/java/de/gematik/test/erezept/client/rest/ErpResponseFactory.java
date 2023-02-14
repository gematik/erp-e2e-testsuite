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

package de.gematik.test.erezept.client.rest;

import static java.text.MessageFormat.*;

import ca.uhn.fhir.parser.*;
import de.gematik.test.erezept.fhir.parser.*;
import java.time.Duration;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.hl7.fhir.r4.model.*;

@Slf4j
public class ErpResponseFactory {

  private final FhirParser parser;

  public ErpResponseFactory(FhirParser parser) {
    this.parser = parser;
  }

  public <R extends Resource> ErpResponse createFrom(
      int status, Duration duration, Map<String, String> headers, String content, Class<R> expect) {
    ErpResponse response;

    if (content.length() > 0) {
      val resource = decode(content, expect);
      response = new ErpResponse(status, duration, headers, resource);
    } else {
      response = new ErpResponse(status, duration, headers, null);
    }
    return response;
  }

  public ErpResponse createFrom(
      int status, Duration duration, Map<String, String> headers, Resource resource) {
    return new ErpResponse(status, duration, headers, resource);
  }

  private Resource decode(String content, Class<? extends Resource> expect) {
    Resource ret;
    try {
      ret = parser.decode(expect, content);
    } catch (DataFormatException | IllegalArgumentException e) {
      // well, try to parse as an OperationOutcome as this case may occur:
      // 1. DataFormatException happens, if the Backend responds with an OperationOutcome while a
      // proper resource was
      // expected
      // 2. IllegalArgumentException is thrown if an empty response is expected, but we still get a
      // resource (probably an OperationOutcome)
      log.warn(
          format(
              "Given content of length {0} could not be parsed as {1}, try OperationOutcome",
              content.length(), expect.getSimpleName()));
      ret = parser.decode(OperationOutcome.class, content);
    }
    return ret;
  }
}
