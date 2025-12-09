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

package de.gematik.test.erezept.client.usecases.eu;

import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.test.erezept.client.usecases.BaseCommand;
import de.gematik.test.erezept.fhir.builder.eu.EuAccessPermissionRequestBuilder;
import de.gematik.test.erezept.fhir.r4.eu.EuAccessPermission;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import java.util.Optional;
import org.hl7.fhir.r4.model.Resource;

/*
 @see <a href="https://github.com/gematik/api-erp/blob/master/docs/erp_eprescription.adoc#erstellen-eines-zugriffscodes-f%C3%BCr-das-einl%C3%B6sen-im-eu-ausland">Api@GitHub</a>
*/
public class EuGrantAccessPostCommand extends BaseCommand<EuAccessPermission> {

  private final EuAccessPermission requestBody;

  public EuGrantAccessPostCommand(EuAccessCode euAccessCode, IsoCountryCode isoCountryCode) {
    this(
        EuAccessPermissionRequestBuilder.euAccessCode(euAccessCode)
            .countryCode(isoCountryCode)
            .build());
  }

  public EuGrantAccessPostCommand(EuAccessPermission requestBody) {
    super(EuAccessPermission.class, HttpRequestMethod.POST, "access-permission");
    this.requestBody = requestBody;
  }

  @Override
  public String getRequestLocator() {
    return "/$grant-eu-access-permission";
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
