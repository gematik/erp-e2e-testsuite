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
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEventBundle;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.r4.model.Resource;

public class AuditEventGetCommand extends BaseCommand<ErxAuditEventBundle> {

  public AuditEventGetCommand(IQueryParameter... param) {
    this(List.of(param));
  }

  public AuditEventGetCommand(List<IQueryParameter> param) {
    super(ErxAuditEventBundle.class, HttpRequestMethod.GET, "AuditEvent");
    queryParameters.addAll(param);
    headerParameters.put("Accept-Language", "de");
  }

  @Override
  public Optional<Resource> getRequestBody() {
    return Optional.empty();
  }
}
