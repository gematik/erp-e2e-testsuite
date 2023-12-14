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

package de.gematik.test.erezept.apimeasure;

import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ICommand;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Resource;

import static java.text.MessageFormat.format;

@Slf4j
public class LoggingStopwatch implements ApiCallStopwatch {
  @Override
  public <T extends Resource>  void measurement(ClientType type, ICommand<T> command, ErpResponse<T> response) {
    log.info(
        format(
            "{0} request from {1} to {2} with return code {3} and payload {4} took {5}ms",
            command.getMethod().name(),
            type.name(),
            command.getRequestLocator(),
            response.getStatusCode(),
            this.getResourceType(response),
            response.getDuration().toMillis()));
  }

  @Override
  public void close() {
    log.info(format("{0} stopped", this.getClass().getSimpleName()));
  }
}
