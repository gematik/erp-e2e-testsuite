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

package de.gematik.test.erezept.client.usecases;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.test.erezept.fhir.builder.erp.ErxConsentBuilder;
import de.gematik.test.erezept.fhir.r4.erp.ErxConsent;
import org.hl7.fhir.r4.model.Resource;

public class ConsentPostCommand extends ErpBaseCommand<ErxConsent> {

  private final ErxConsent requestBody;

  public ConsentPostCommand(KVNR kvnr) {
    this(ErxConsentBuilder.forKvnr(kvnr).build());
  }

  public ConsentPostCommand(ErxConsent requestBody) {
    super(ErxConsent.class, HttpRequestMethod.POST, "Consent");
    this.requestBody = requestBody;
  }

  /**
   * Get the FHIR-Resource for the Request-Body (of the inner-HTTP)
   *
   * @return the FHIR-Resource for the Request-Body
   */
  @Override
  public Resource getRequestBody() {
    return requestBody;
  }
}
