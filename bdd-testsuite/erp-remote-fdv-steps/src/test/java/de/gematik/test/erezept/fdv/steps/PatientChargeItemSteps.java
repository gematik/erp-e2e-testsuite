/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.fdv.steps;

import static net.serenitybdd.screenplay.GivenWhenThen.*;

import de.gematik.test.erezept.fdv.task.ChargeItemConsent;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

public class PatientChargeItemSteps {

  @Angenommen(
      "^(?:der|die) Versicherte (.+) hat (?:seine|ihre) Einwilligung zum Speichern der"
          + " PKV-Abrechnungsinformationen erteilt")
  public void givenThatConsentIsGranted(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    givenThat(thePatient).attemptsTo(ChargeItemConsent.grantConsent());
  }

  @Wenn(
      "^(?:der|die) Versicherte (.+) (?:seine|ihre) Einwilligung zur Speicherung der"
          + " PKV-Abrechnungsinformationen erteilt")
  public void whenGrantConsent(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(ChargeItemConsent.grantConsent());
  }

  @Wenn(
      "^(?:der|die) Versicherte (.+) (?:seine|ihre) Einwilligung zur Speicherung der"
          + " PKV-Abrechnungsinformationen widerruft")
  public void whenRevokeConsent(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(ChargeItemConsent.revokeConsent());
  }

  @Dann("^kann (?:der|die) Versicherte (.+) seine Einwilligung abrufen$")
  public void thenCanGetConsent(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(ChargeItemConsent.getConsent());
  }
}
