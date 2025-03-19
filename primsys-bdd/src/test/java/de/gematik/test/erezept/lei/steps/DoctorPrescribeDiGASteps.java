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

package de.gematik.test.erezept.lei.steps;

import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.screenplay.task.IssueDiGAPrescription;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

public class DoctorPrescribeDiGASteps {

  @Wenn(
      "^(?:der|die) (Psychotherapeut|Psychologischer"
          + " Psychotherapeut|Kinderpsychotherapeut|Arzt|Zahnarzt) (.+) (?:dem|der) Versicherten"
          + " (.+) ein EVGDA E-Rezept verschreibt$")
  public void whenIssueDiGAPrescription(String role, String doctorName, String patientName) {
    val theDoctor = OnStage.theActorCalled(doctorName);
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor).attemptsTo(IssueDiGAPrescription.forPatient(thePatient));
  }
}
