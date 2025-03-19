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

package de.gematik.test.erezept.apimeasure;

import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ICommand;
import org.hl7.fhir.r4.model.Resource;

public interface ApiCallStopwatch {

  <T extends Resource> void measurement(
      ClientType type, ICommand<T> command, ErpResponse<T> response);

  void close();

  default <T extends Resource> String getResourceType(ICommand<T> command) {
    return command.getRequestBody().map(b -> b.getClass().getSimpleName()).orElse("none");
  }

  default <T extends Resource> String getResourceType(ErpResponse<T> response) {
    if (response.getResourceType() != null) {
      return response.getResourceType().getSimpleName();
    } else {
      return "none";
    }
  }
}
