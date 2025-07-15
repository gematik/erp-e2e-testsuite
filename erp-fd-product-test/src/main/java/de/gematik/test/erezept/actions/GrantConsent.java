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

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.client.usecases.ConsentPostCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxConsent;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.erezept.tasks.EnsureConsent;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;

@AllArgsConstructor
public class GrantConsent extends ErpAction<ErxConsent> {
  @Nullable private final ErxConsent consent;
  @Nullable private KVNR kvnr;
  boolean ensureConsentIsUnset = false;

  public static GrantConsentBuilder withKvnrAndDecideIsUnset(
      KVNR kvnr, boolean ensureConsentIsUnset) {
    return new GrantConsentBuilder(ensureConsentIsUnset, kvnr);
  }

  public static GrantConsentBuilder withKvnrAndEnsureIsUnset(KVNR kvnr) {
    return new GrantConsentBuilder(true, kvnr);
  }

  public static GrantConsentBuilder forOneSelf() {
    return new GrantConsentBuilder();
  }

  @Override
  @Step("{0} sendet eine Einwilligung zum hinterlegen von Rechnungsinformationen an den Fachdienst")
  public ErpInteraction<ErxConsent> answeredBy(Actor actor) {
    if (ensureConsentIsUnset) EnsureConsent.shouldBeUnset().performAs(actor);
    if (kvnr == null) {
      kvnr = SafeAbility.getAbility(actor, ProvidePatientBaseData.class).getKvnr();
    }
    ConsentPostCommand consentPostCommand;
    if (consent != null) {
      consentPostCommand = new ConsentPostCommand(consent);
    } else {
      consentPostCommand = new ConsentPostCommand(kvnr);
    }
    return performCommandAs(consentPostCommand, actor);
  }

  @AllArgsConstructor
  @NoArgsConstructor
  public static class GrantConsentBuilder {
    boolean ensureConsentIsUnset = false;
    @Nullable private KVNR kvnr;

    public GrantConsent withDefaultConsent() {
      return new GrantConsent(null, kvnr, ensureConsentIsUnset);
    }

    public GrantConsent withConsent(ErxConsent consent) {
      return new GrantConsent(consent, kvnr, ensureConsentIsUnset);
    }

    public GrantConsentBuilder ensureConsentIsUnset(boolean ensureConsentIsUnset) {
      this.ensureConsentIsUnset = ensureConsentIsUnset;
      return this;
    }
  }
}
