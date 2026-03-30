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

package de.gematik.test.erezept.app.task;

import static de.gematik.test.erezept.screenplay.task.BillingInformationConsent.ConsentAction.*;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.app.questions.IsEUConsentPresent;
import de.gematik.test.erezept.client.usecases.eu.EuConsentDeleteCommand;
import de.gematik.test.erezept.client.usecases.eu.EuConsentPostCommand;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.task.BillingInformationConsent;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor
public class EnsureThatTheEUConsent implements Task {
  private final BillingInformationConsent.ConsentAction consentAction;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val providePatientDataAbility = SafeAbility.getAbility(actor, ProvidePatientBaseData.class);

    val isEUConsentPresent = actor.asksFor(IsEUConsentPresent.onFD());

    if (consentAction.equals(REVOKE) && isEUConsentPresent) {
      deleteEUConsentOnFD(erpClientAbility);
    }

    if (consentAction.equals(GRANT) && !isEUConsentPresent) {
      grantEUConsentOnFD(erpClientAbility, providePatientDataAbility.getKvnr());
    }
  }

  private void deleteEUConsentOnFD(UseTheErpClient erpClientAbility) {
    val deleteEUConsentCommand = new EuConsentDeleteCommand();
    erpClientAbility.request(deleteEUConsentCommand);
  }

  private void grantEUConsentOnFD(UseTheErpClient erpClientAbility, KVNR kvnr) {
    val grantEUConsentCommand = new EuConsentPostCommand(kvnr);
    erpClientAbility.request(grantEUConsentCommand);
  }

  public static EnsureThatTheEUConsent isRevoked() {
    return new EnsureThatTheEUConsent(REVOKE);
  }
}
