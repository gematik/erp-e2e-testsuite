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

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static net.serenitybdd.screenplay.GivenWhenThen.then;

import de.gematik.test.erezept.app.task.AssignPrescriptionToPharmacyAbroad;
import de.gematik.test.erezept.app.task.EnsureThatTheEUFeature;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

public class EUSteps {
  @Angenommen("^(?:der|die) Versicherte (.+) hat das EU Feature aktiviert$")
  public void activateEUFeature(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    givenThat(thePatient).attemptsTo(EnsureThatTheEUFeature.isActive());
  }

  @Dann("^kann (?:der|die) Versicherte (.+) das letzte E-Rezept in (.+) einlösen")
  public void redeemLastEUPrescription(String patientName, String country) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient).attemptsTo(AssignPrescriptionToPharmacyAbroad.in(country));
  }
}
