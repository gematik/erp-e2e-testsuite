/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.lei.steps;

import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.screenplay.questions.HasReceivedCommunication;
import de.gematik.test.erezept.screenplay.questions.ResponseOfAbortOperation;
import de.gematik.test.erezept.screenplay.questions.TheLastPrescription;
import de.gematik.test.erezept.screenplay.task.CheckTheReturnCode;
import de.gematik.test.erezept.screenplay.task.HandoverDispenseRequestAsRepresentative;
import io.cucumber.java.de.Aber;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.Ensure;

public class RepresentativePatientSteps {

  /**
   * TMD-1649
   *
   * @param representativeName Name des Vertreters
   * @param patientName Name des versicherten
   */
  @Dann(
      "^hat (?:der Vertreter|die Vertreterin) (.+) die Nachricht mit dem Rezept (?:der Versicherten|des Versicherten) (.+) empfangen$")
  @Und(
      "^(?:der Vertreter|die Vertreterin) (.+) hat die Nachricht mit dem Rezept (?:der Versicherten|des Versicherten) (.+) empfangen$")
  public void thenRepresentativeReceivedMessageFrom(String representativeName, String patientName) {
    val theRepresentative = OnStage.theActorCalled(representativeName);
    val thePatient = OnStage.theActorCalled(patientName);
    then(theRepresentative)
        .attemptsTo(
            Ensure.that(HasReceivedCommunication.representative().from(thePatient)).isTrue());
  }

  @Wenn(
      "^(?:der Vertreter|die Vertreterin) (.+) (?:sein|ihr) (letztes|erstes) von (.+) zugewiesenes E-Rezept der Apotheke (.+) via Data Matrix Code zuweist$")
  public void whenAssignDispenseRequestPhysicallyAsRepresentative(
      String representativeName, String order, String patientName, String pharmacyName) {
    val theRepresentative = OnStage.theActorCalled(representativeName);
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    when(theRepresentative)
        .attemptsTo(
            HandoverDispenseRequestAsRepresentative.fromStack(order)
                .ofTheOwner(thePatient)
                .to(thePharmacy));
  }

  @Dann("^wird (?:dem Vertreter|der Vertreterin) (.+) das neue E-Rezept angezeigt$")
  public void thenFetchPrescriptionAsRepresentative(String representativeName) {
    val theRepresentative = OnStage.theActorCalled(representativeName);
    then(theRepresentative)
        .attemptsTo(Ensure.that(TheLastPrescription.prescribed().existsInBackend()).isTrue());
  }

  @Dann(
      "^kann (?:der Vertreter|die Vertreterin) (.+) das (letzte|erste) von (.+) zugewiesene E-Rezept ohne AccessCode nicht löschen$")
  public void thenRepresentativeCannotDeletePrescription(
      String representativeName, String order, String patientName) {
    val theRepresentative = OnStage.theActorCalled(representativeName);
    val thePatient = OnStage.theActorCalled(patientName);
    then(theRepresentative)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfAbortOperation
                        .asPatient() // execute as patient will result in call without AccessCode
                        .fromStack(order))
                .isEqualTo(403));
  }

  @Aber(
      "^(?:der Vertreter|die Vertreterin) (.+) kann das (letzte|erste) von (.+) zugewiesene E-Rezept mit AccessCode löschen$")
  public void thenRepresentativeCanDeletePrescription(
      String representativeName, String order, String patientName) {
    val theRepresentative = OnStage.theActorCalled(representativeName);
    val thePatient = OnStage.theActorCalled(patientName);
    then(theRepresentative)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asRepresentative().fromStack(order))
                .isEqualTo(204));
  }
}
