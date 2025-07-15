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

package de.gematik.test.erezept.client.usecases;

import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.test.erezept.fhir.r4.erp.ErxCapabilityStatement;
import java.util.Optional;
import org.hl7.fhir.r4.model.Resource;

/** Command to fetch the FHIR CapabilityStatement from the ERP server */
public class GetCapabilityStatementCommand extends BaseCommand<ErxCapabilityStatement> {

  /** Constructs a command to fetch the CapabilityStatement */
  public GetCapabilityStatementCommand() {
    super(ErxCapabilityStatement.class, HttpRequestMethod.GET, "metadata");
  }

  /**
   * There is no request body for a CapabilityStatement GET request
   *
   * @return empty optional
   */
  @Override
  public Optional<Resource> getRequestBody() {
    return Optional.empty();
  }
}
