/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.lei.steps;

import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.screenplay.task.AlternativelyAssign;
import de.gematik.test.erezept.screenplay.task.DecryptPSPMessage;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

public class AlternativeAssignmentSteps {

  /**
   * @param pharmName
   */
  @Wenn(
      "^die Apotheke (.+) eine Nachricht mit einer alternativen Zuweisung vom Dienstleister"
          + " empfängt und entschlüsselt$")
  public void whenPharmacyGetsMessageFromServiceProvider(String pharmName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy).attemptsTo(DecryptPSPMessage.receivedFromPharmacyService());
  }

  /**
   * Teststep zum Auslösen der alternativen Zuweisung im FdV
   *
   * @param patientName
   * @param order
   * @param pharmName
   * @param option
   */
  @Wenn(
      "^(?:der Versicherte|die Versicherte) (.+) für das (letzte|erste) E-Rezept die alternative"
          + " Zuweisung an die Apotheke (.+) mit der Option (.+) auslöst$")
  public void whenPatientInitiatesAlternativeAssignment(
      String patientName, String order, String pharmName, String option) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmName);

    when(thePatient)
        .attemptsTo(
            AlternativelyAssign.thePrescriptionReceived(order).to(thePharmacy).with(option));
  }
}
