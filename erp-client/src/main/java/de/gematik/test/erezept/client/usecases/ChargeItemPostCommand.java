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

package de.gematik.test.erezept.client.usecases;

import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.test.erezept.client.rest.param.QueryParameter;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.values.Secret;
import java.util.Optional;
import org.hl7.fhir.r4.model.Resource;

public class ChargeItemPostCommand extends BaseCommand<ErxChargeItem> {

  private final ErxChargeItem body;

  public ChargeItemPostCommand(ErxChargeItem body, Secret secret) {
    super(ErxChargeItem.class, HttpRequestMethod.POST, "ChargeItem");
    this.body = body;
    this.queryParameters.add(new QueryParameter("task", body.getPrescriptionId().getValue()));
    this.queryParameters.add(new QueryParameter("secret", secret.getValue()));
  }

  /**
   * Get the FHIR-Resource for the Request-Body (of the inner-HTTP)
   *
   * @return an Optional.of(FHIR-Resource) for the Request-Body or an empty Optional if Request-Body
   *     is empty
   */
  @Override
  public Optional<Resource> getRequestBody() {
    return Optional.of(body);
  }
}
