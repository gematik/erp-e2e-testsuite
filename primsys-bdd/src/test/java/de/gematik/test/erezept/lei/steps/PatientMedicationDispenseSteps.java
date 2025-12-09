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

package de.gematik.test.erezept.lei.steps;

import static net.serenitybdd.screenplay.GivenWhenThen.then;

import de.gematik.test.erezept.screenplay.questions.MedicationDispenseContains;
import de.gematik.test.erezept.screenplay.questions.RedeemCodeIsAvailable;
import io.cucumber.java.de.Und;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.Ensure;

public class PatientMedicationDispenseSteps {

  @Und(
      "^(?:der|die) GKV Versicherte (.+) kann für die (letzte|erste) EVDGA den Freischaltcode"
          + " abrufen$")
  public void callEvdgaUnlockCode(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            Ensure.that(
                    MedicationDispenseContains.forThePatient()
                        .andPrescription(order)
                        .hasRedeemCode())
                .isTrue());
  }

  @Und(
      "^(?:der|die) GKV Versicherte (.+) kann für die (letzte|erste) EVDGA keinen Freischaltcode"
          + " abrufen$")
  public void couldNotCallEvdgaUnlockCode(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(Ensure.that(RedeemCodeIsAvailable.forThePrescription(order)).isFalse());
  }
}
