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

package de.gematik.test.erezept.client.usecases;

import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.client.rest.param.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import java.util.*;
import org.hl7.fhir.r4.model.*;

public class AuditEventGetCommand extends BaseCommand<ErxAuditEventBundle> {

  public AuditEventGetCommand(QueryParameter... param) {
    super(ErxAuditEventBundle.class, HttpRequestMethod.GET, "AuditEvent");
    queryParameters.addAll(List.of(param));
  }

  @Override
  public Optional<Resource> getRequestBody() {
    return Optional.empty();
  }
}
