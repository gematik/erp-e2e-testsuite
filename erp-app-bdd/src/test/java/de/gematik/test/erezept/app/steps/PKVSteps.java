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

package de.gematik.test.erezept.app.steps;

import static de.gematik.test.erezept.screenplay.task.BillingInformationConsent.ConsentAction.fromString;
import static net.serenitybdd.screenplay.GivenWhenThen.*;

import de.gematik.test.erezept.app.abilities.HandleChargeItems;
import de.gematik.test.erezept.app.task.DeleteChargeItem;
import de.gematik.test.erezept.app.task.HandleChargeItemConsent;
import de.gematik.test.erezept.app.task.VerifyChargeItemInMessages;
import de.gematik.test.erezept.app.task.VerifyChargeItemInProfileSettings;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

public class PKVSteps {
  @Angenommen("^(?:der|die) Versicherte (.+) kann Kostenbelege verwalten")
  public void canHandleChargeItems(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    givenThat(thePatient).can(HandleChargeItems.forHerself());
  }

  @Wenn(
      "^(?:der|die) Versicherte (.+) (?:seine|ihre) Einwilligung zum Speichern der"
          + " PKV-Abrechnungsinformationen über die App (erteilt|widerruft)$")
  @Dann(
      "^kann (?:der|die) Versicherte (.+) (?:seine|ihre) Einwilligung zum Speichern der"
          + " PKV-Abrechnungsinformationen über die App (erteilen|widerrufen)$")
  public void handleChargeItemConsentThroughTheApp(String patientName, String consentAction) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(HandleChargeItemConsent.withAction(fromString(consentAction)));
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) den Kostenbeleg für das letzte E-Rezept in der App"
          + " einsehen")
  public void thenCanSeeTheChargeItem(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient).attemptsTo(VerifyChargeItemInMessages.fromMainScreen());
    then(thePatient).attemptsTo(VerifyChargeItemInProfileSettings.fromChargeItemDrawer());
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) den Kostenbeleg für das letzte E-Rezept in der App"
          + " löschen")
  public void thenCanDeleteTheChargeItem(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient).attemptsTo(DeleteChargeItem.fromChargeItemDetailScreen());
  }
}
