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

import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.screenplay.task.IssuePrescription;
import de.gematik.test.erezept.screenplay.task.Negate;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.GivenWhenThen.when;

public class DoctorDirectAssignmentSteps {

  /**
   * TMD-1598 Variante Direktzuweisung
   *
   * @param docName ist der Name des ausstellenden Arztes
   * @param patientName ist der Name des empfangenden Patienten
   * @param medications ist die Liste der Medikamente, die der Arzt dem Patienten ausstellt
   */
  @Wenn(
      "^(?:der Arzt|die Ärztin) (.+) (?:dem|der) Versicherten (.+) folgende(?:s)? Medikament(?:e)? verschreibt und der Apotheke (.+) direkt zuweist:$")
  public void whenIssueDirectAssignmentPrescriptionToActor(
      String docName, String patientName, String pharmacyName, DataTable medications) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
                .to(thePharmacy)
                .from(medications.asMaps()));
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (?:dem|der) Versicherten (.+) folgende(?:s)? Medikament(?:e)? verschreibt und der Apotheke (.+) direkt zuweist:$")
  public void whenIssueDirectAssignmentPrescriptionToActor(
      String patientName, String pharmacyName, DataTable medications) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
                .to(thePharmacy)
                .from(medications.asMaps()));
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (.+) (?:dem|der) Versicherten (.+) ein Medikament verschreibt und der Apotheke (.+) direkt zuweist$")
  public void whenIssueSingleRandomDirectAssignmentPrescriptionToActor(
      String docName, String patientName, String pharmacyName) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
                .to(thePharmacy)
                .randomPrescription());
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (?:dem|der) Versicherten (.+) ein Medikament verschreibt und der Apotheke (.+) direkt zuweist$")
  public void whenIssueSingleRandomDirectAssignmentPrescriptionToActor(
      String patientName, String pharmacyName) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
                .to(thePharmacy)
                .randomPrescription());
  }

  @Dann(
      "^darf (?:der Arzt|die Ärztin) (.+) (?:dem|der) Versicherten (.+) das folgende E-Rezept nicht verschreiben und der Apotheke (.+) direkt zuweisen:$")
  public void thenIssueDirectAssignmentPrescriptionToActorNotAllowed(
      String docName, String patientName, String pharmacyName, DataTable medications) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    then(theDoctor)
        .attemptsTo(
            Negate.the(
                    IssuePrescription.forPatient(thePatient)
                        .as(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
                        .to(thePharmacy)
                        .from(medications.asMaps()))
                .with(UnexpectedResponseResourceError.class));
  }
}
