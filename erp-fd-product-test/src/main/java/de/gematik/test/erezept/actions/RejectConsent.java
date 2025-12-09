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

package de.gematik.test.erezept.actions;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.client.rest.param.QueryParameter;
import de.gematik.test.erezept.client.usecases.ConsentDeleteCommand;
import de.gematik.test.erezept.tasks.EnsureConsent;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.serenitybdd.screenplay.Actor;

@AllArgsConstructor
public class RejectConsent extends ErpAction<EmptyResource> {

  private final ConsentDeleteCommand consentDeleteCommand;
  private boolean ensureConsentIsPresent;

  public static ConsentRejectBuilder forOneSelf() {
    return new ConsentRejectBuilder();
  }

  public static RejectConsent forOneSelf(
      ConsentDeleteCommand consentDeleteCommand, boolean ensureConsentIsPresent) {
    return new RejectConsent(consentDeleteCommand, ensureConsentIsPresent);
  }

  @Override
  public ErpInteraction<EmptyResource> answeredBy(Actor actor) {
    if (ensureConsentIsPresent) EnsureConsent.shouldBePresent().performAs(actor);
    return performCommandAs(consentDeleteCommand, actor);
  }

  @AllArgsConstructor
  @NoArgsConstructor
  public static class ConsentRejectBuilder {
    boolean ensureConsentIsPresent;
    List<QueryParameter> queryParameters;

    public RejectConsent withCustomCommand(
        ConsentDeleteCommand consentDeleteCommand, boolean ensureConsentIsPresent) {
      return new RejectConsent(consentDeleteCommand, ensureConsentIsPresent);
    }

    public RejectConsent buildValid() {
      return new RejectConsent(new ConsentDeleteCommand(), true);
    }

    public RejectConsent withoutEnsureIsPresent() {
      return new RejectConsent(new ConsentDeleteCommand(), false);
    }
  }
}
