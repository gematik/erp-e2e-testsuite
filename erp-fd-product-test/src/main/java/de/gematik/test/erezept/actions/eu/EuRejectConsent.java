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

package de.gematik.test.erezept.actions.eu;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.client.usecases.eu.EuConsentDeleteCommand;
import lombok.AllArgsConstructor;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;

@AllArgsConstructor
public class EuRejectConsent extends ErpAction<EmptyResource> {

  private final EuConsentDeleteCommand command;
  private boolean ensureConsentIsPresent = false;

  public static EuRejectConsentBuilder forOneSelf() {
    return new EuRejectConsentBuilder();
  }

  @Step("{0} widerruft den EU-Consent")
  @Override
  public ErpInteraction<EmptyResource> answeredBy(Actor actor) {
    if (ensureConsentIsPresent) {
      EnsureEuConsent.shouldBePresent().performAs(actor);
    }

    return performCommandAs(command, actor);
  }

  public static class EuRejectConsentBuilder {
    public EuRejectConsent build() {
      return new EuRejectConsent(new EuConsentDeleteCommand(), true);
    }
  }
}
