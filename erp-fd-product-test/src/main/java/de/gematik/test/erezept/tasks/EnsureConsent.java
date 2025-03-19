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

package de.gematik.test.erezept.tasks;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.client.usecases.ConsentDeleteCommand;
import de.gematik.test.erezept.client.usecases.ConsentGetCommand;
import de.gematik.test.erezept.client.usecases.ConsentPostCommand;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor
public class EnsureConsent implements Task {

  private final boolean shouldBeSet;

  @Override
  @Step("{0} stellt sicher, dass der Consent = #shouldBeSet ist")
  public <T extends Actor> void performAs(T actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val getConsentCmd = new ConsentGetCommand();
    val getConsentResponse = erpClient.request(getConsentCmd);
    val consentBundle = getConsentResponse.getExpectedResource();

    if (!consentBundle.hasConsent() && shouldBeSet) {
      // no consent present but must be set!
      val patientData = SafeAbility.getAbility(actor, ProvidePatientBaseData.class);
      val postConsentCmd = new ConsentPostCommand(patientData.getKvnr());
      val postConsentResponse = erpClient.request(postConsentCmd);
      assertTrue(postConsentResponse.isOfExpectedType());
    } else if (consentBundle.hasConsent() && !shouldBeSet) {
      // consent is present but must be unset
      val deleteConsentCmd = new ConsentDeleteCommand();
      val deleteConsentResponse = erpClient.request(deleteConsentCmd);
      assertFalse(deleteConsentResponse.isOperationOutcome());
    }
  }

  public static EnsureConsent shouldBePresent() {
    return shouldBeSet(true);
  }

  public static EnsureConsent shouldBeUnset() {
    return shouldBeSet(false);
  }

  public static EnsureConsent shouldBeSet(boolean value) {
    return new EnsureConsent(value);
  }
}
