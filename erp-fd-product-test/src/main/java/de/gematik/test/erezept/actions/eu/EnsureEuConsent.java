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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.client.usecases.eu.EuConsentDeleteCommand;
import de.gematik.test.erezept.client.usecases.eu.EuConsentGetCommand;
import de.gematik.test.erezept.client.usecases.eu.EuConsentPostCommand;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import org.hl7.fhir.r4.model.Bundle;

@RequiredArgsConstructor
public class EnsureEuConsent implements Task {

  private final boolean shouldBeSet;

  @Override
  @Step("{0} stellt sicher, dass der EU-Consent = #shouldBeSet ist")
  public <T extends Actor> void performAs(T actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val getConsentCmd = new EuConsentGetCommand();
    val getConsentResponse = erpClient.request(getConsentCmd);
    val isSet = getConsentResponse.getResourceOptional().map(Bundle::hasEntry).orElse(false);

    if (!isSet && shouldBeSet) {
      // no EU-consent present but must be set!
      val patientData = SafeAbility.getAbility(actor, ProvidePatientBaseData.class);
      val postConsentCmd = new EuConsentPostCommand(patientData.getKvnr());
      val postConsentResponse = erpClient.request(postConsentCmd);
      assertTrue(postConsentResponse.isOfExpectedType());
    } else if (isSet && !shouldBeSet) {
      // EU-consent is present but must be unset
      val deleteConsentCmd = new EuConsentDeleteCommand();
      val deleteConsentResponse = erpClient.request(deleteConsentCmd);
      assertFalse(deleteConsentResponse.isOperationOutcome());
    }
  }

  public static EnsureEuConsent shouldBePresent() {
    return shouldBeSet(true);
  }

  public static EnsureEuConsent shouldBeUnset() {
    return shouldBeSet(false);
  }

  public static EnsureEuConsent shouldBeSet(boolean value) {
    return new EnsureEuConsent(value);
  }
}
