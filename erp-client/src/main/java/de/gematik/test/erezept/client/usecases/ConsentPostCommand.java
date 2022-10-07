/*
 * Copyright (c) 2022 gematik GmbH
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

import de.gematik.test.erezept.client.rest.HttpRequestMethod;
import de.gematik.test.erezept.fhir.builder.erp.ErxConsentBuilder;
import de.gematik.test.erezept.fhir.resources.erp.ErxConsent;
import java.util.Optional;
import org.hl7.fhir.r4.model.Resource;

public class ConsentPostCommand extends BaseCommand<ErxConsent> {

  private final ErxConsent requestBody;

  public ConsentPostCommand(String kvid) {
    this(ErxConsentBuilder.forKvid(kvid).build());
  }

  public ConsentPostCommand(ErxConsent requestBody) {
    super(ErxConsent.class, HttpRequestMethod.POST, "Consent");
    this.requestBody = requestBody;
  }

  /**
   * This method returns the last (tailing) part of the URL of the inner-HTTP Request e.g.
   * /Task/[id] or /Communication?[queryParameter]
   *
   * @return the tailing part of the URL which combines to full URL like [baseUrl][tailing Part]
   */
  @Override
  public String getRequestLocator() {
    return this.getResourcePath();
  }

  /**
   * Get the FHIR-Resource for the Request-Body (of the inner-HTTP)
   *
   * @return an Optional.of(FHIR-Resource) for the Request-Body or an empty Optional if Request-Body
   *     is empty
   */
  @Override
  public Optional<Resource> getRequestBody() {
    return Optional.of(requestBody);
  }
}
