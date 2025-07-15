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

package de.gematik.test.erezept.abilities;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.actions.rawhttpactions.pki.OCSPBodyWrapper;
import java.util.Map;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.UnirestInstance;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Ability;
import net.serenitybdd.screenplay.HasTeardown;

@Slf4j
@RequiredArgsConstructor
public class OCSPAbility implements Ability, HasTeardown {
  @Delegate private final UnirestInstance client;
  private final String resourceName;

  public HttpResponse<OCSPBodyWrapper> getOcspResponse(Map<String, Object> query) {
    val abilityResponse =
        this.get(resourceName)
            .queryString("issuer-cn", query.get("issuer-cn")) // fixed order for WireMock Unittest
            .queryString("serial-nr", query.get("serial-nr"))
            .asBytes();
    val response = abilityResponse.map(OCSPBodyWrapper::new);
    log.info(
        format(
            "Send {0} with following responseCode: {1} and responseBody: {2}",
            response.getRequestSummary().getRawPath(),
            response.getStatus(),
            response.getBody().bodyAsString()));
    return response;
  }

  @Override
  public void tearDown() {
    client.close();
  }
}
