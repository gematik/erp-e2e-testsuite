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

package de.gematik.test.erezept.fdv.steps;

import static net.serenitybdd.screenplay.GivenWhenThen.and;
import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.fdv.questions.HasReceivedCommunication;
import de.gematik.test.erezept.screenplay.questions.ResponseOfPostCommunication;
import de.gematik.test.erezept.screenplay.task.SendCommunication;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.Ensure;

public class PatientCommunicationDiGASteps {

  /**
   * TMD-1640
   *
   * @param patientName
   * @param order
   * @param insuranceName
   */
  @Wenn(
      "^(?:der|die) GKV Versicherte (.+) (?:sein|ihr) (letztes|erstes) ausgestelltes (?:EVGDA"
          + " E-Rezept|E-Rezept) ihrem Kostenträger (.+) zuweist$")
  public void whenRequestDispenseViaCommunication(
      String patientName, String order, String insuranceName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val theInsurance = OnStage.theActorCalled(insuranceName);
    when(thePatient)
        .attemptsTo(
            SendCommunication.with(
                ResponseOfPostCommunication.dispenseDiGARequest()
                    .forPrescriptionFromBackend(order)
                    .sentTo(theInsurance)
                    .withoutMessage())); // without message for DiGA Usecase
  }

  @Und(
      "^(?:der|die) GKV Versicherte (.+) kann für die (letzte|erste) EVGDA Verordnung Informationen"
          + " vom Kostenträger (.*) im FdV einsehen$")
  public void
      dieGKVVersicherteHannaBäckerKannFürDieLetzteEVGDAInformationenImFdVEineBegründungEinsehen(
          String patientName, String order, String insuranceName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val theInsurance = OnStage.theActorCalled(insuranceName);
    and(thePatient)
        .attemptsTo(Ensure.that(HasReceivedCommunication.reply().from(theInsurance)).isTrue());
  }
}
