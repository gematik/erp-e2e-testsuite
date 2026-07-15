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

package de.gematik.test.erezept.apimeasure;

import de.gematik.bbriccs.rest.fd.FhirBRequest;
import de.gematik.bbriccs.rest.fd.FhirBResponse;
import de.gematik.test.erezept.client.ClientType;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
public class LoggingStopwatch implements ApiCallStopwatch {
  @Override
  public <T extends Resource, R extends Resource> void measurement(
      ClientType type, FhirBRequest<T, R> command, FhirBResponse<R> response) {
    log.info(
        "{} request from {} to {} with return code {} and payload {} took {}ms",
        command.getMethod().name(),
        type.name(),
        command.getRequestLocator(),
        response.getStatusCode(),
        this.getResourceType(response),
        response.getDuration().toMillis());
  }

  @Override
  public void close() {
    log.info("{} stopped", this.getClass().getSimpleName());
  }
}
