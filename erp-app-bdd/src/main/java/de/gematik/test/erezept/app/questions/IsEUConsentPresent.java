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

package de.gematik.test.erezept.app.questions;

import de.gematik.test.erezept.client.usecases.eu.EuConsentGetCommand;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
public class IsEUConsentPresent implements Question<Boolean> {
  @Override
  public Boolean answeredBy(final Actor actor) {
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);

    val getEUConsentCommand = new EuConsentGetCommand();
    val response = erpClientAbility.request(getEUConsentCommand);
    val euConsent = response.getExpectedResource().getConsent();

    return euConsent.isPresent();
  }

  public static IsEUConsentPresent onFD() {
    return new IsEUConsentPresent();
  }
}
