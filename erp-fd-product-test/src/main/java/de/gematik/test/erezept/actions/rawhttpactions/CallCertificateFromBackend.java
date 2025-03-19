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

package de.gematik.test.erezept.actions.rawhttpactions;

import de.gematik.test.erezept.abilities.RawHttpAbility;
import de.gematik.test.erezept.actions.rawhttpactions.pki.PKICertificatesDTOEnvelop;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Optional;
import kong.unirest.core.HttpResponse;
import lombok.AllArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@AllArgsConstructor
public class CallCertificateFromBackend
    implements Question<HttpResponse<PKICertificatesDTOEnvelop>> {
  private String rootCA;

  public static CallCertificateFromBackend withRootCa(String rootCA) {
    return new CallCertificateFromBackend(rootCA);
  }

  @Override
  public HttpResponse<PKICertificatesDTOEnvelop> answeredBy(Actor actor) {
    val httpClient = SafeAbility.getAbility(actor, RawHttpAbility.class);
    return httpClient
        .get("/PKICertificates")
        .queryString("currentRoot", rootCA)
        .asObject(PKICertificatesDTOEnvelop.class)
        .map(actual -> Optional.ofNullable(actual).orElseGet(PKICertificatesDTOEnvelop::new));
  }
}
