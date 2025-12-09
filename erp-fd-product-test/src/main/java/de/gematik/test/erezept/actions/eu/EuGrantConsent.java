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

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.client.usecases.eu.EuConsentPostCommand;
import de.gematik.test.erezept.fhir.builder.eu.EuConsentBuilder;
import de.gematik.test.erezept.fhir.r4.eu.EuConsent;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.erezept.tasks.EnsureConsent;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;

@AllArgsConstructor
public class EuGrantConsent extends ErpAction<EuConsent> {

  @Nullable private final EuConsent consent;
  @Nullable private KVNR kvnr;
  private final boolean ensureConsentIsUnset;

  public static EuGrantConsentBuilder withKvnrAndDecideIsUnset(
      KVNR kvnr, boolean ensureConsentIsUnset) {
    return new EuGrantConsentBuilder(ensureConsentIsUnset, kvnr);
  }

  public static EuGrantConsentBuilder forOneSelf() {
    return new EuGrantConsentBuilder();
  }

  @Override
  @Step("{0} sendet eine EU-Einwilligung (EUDISPCONS) an den Fachdienst")
  public ErpInteraction<EuConsent> answeredBy(Actor actor) {
    if (ensureConsentIsUnset) {
      EnsureConsent.shouldBeUnset().performAs(actor);
    }

    val baseData = SafeAbility.getAbility(actor, ProvidePatientBaseData.class);
    if (kvnr == null) {
      kvnr = baseData.getKvnr();
    }

    val euConsent =
        Objects.requireNonNullElseGet(consent, () -> EuConsentBuilder.forKvnr(kvnr).build());
    return performCommandAs(new EuConsentPostCommand(euConsent), actor);
  }

  @AllArgsConstructor
  @NoArgsConstructor
  public static class EuGrantConsentBuilder {
    boolean ensureConsentIsUnset = false;
    @Nullable private KVNR kvnr;

    public EuGrantConsent withDefaultConsent() {
      return new EuGrantConsent(null, kvnr, ensureConsentIsUnset);
    }

    public EuGrantConsent withConsent(EuConsent consent) {
      return new EuGrantConsent(consent, kvnr, ensureConsentIsUnset);
    }

    public EuGrantConsentBuilder withKvnr(KVNR kvnr) {
      this.kvnr = kvnr;
      return this;
    }
  }
}
