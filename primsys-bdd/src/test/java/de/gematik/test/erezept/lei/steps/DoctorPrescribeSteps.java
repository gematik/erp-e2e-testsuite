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

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.screenplay.task.IssuePrescription;
import de.gematik.test.erezept.screenplay.task.Negate;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

/** Testschritte die aus der Perspektive eines Arztes bzw. einer Ärztin ausgeführt werden */
@Slf4j
public class DoctorPrescribeSteps {

  @Angenommen(
      "^(?:der Arzt|die Ärztin) (.+) verschreibt folgende(?:s)? E-Rezept(?:e)? an (?:den|die)"
          + " Versicherte(?:n)? (.+):$")
  public void givenIssueMultiplePrescriptionsToActor(
      String docName, String patientName, DataTable medications) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);

    givenThat(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .forPznPrescription(medications.asMaps()));
  }

  @Angenommen(
      "^(?:der Arzt|die Ärztin) verschreibt folgende(?:s)? E-Rezept(?:e)? an (?:den|die)"
          + " Versicherte(?:n)? (.+):$")
  public void givenIssueMultiplePrescriptionsToActor(String patientName, DataTable medications) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);

    givenThat(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .forPznPrescription(medications.asMaps()));
  }

  @Angenommen(
      "^(?:der Arzt|die Ärztin) (.+) verschreibt ein E-Rezept an (?:den|die) Versicherte(?:n)?"
          + " (.+)$")
  public void givenIssueSingleRandomPrescriptionsToActor(String docName, String patientName) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);

    givenThat(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .randomPrescription());
  }

  @Angenommen(
      "^(?:der Arzt|die Ärztin) verschreibt ein E-Rezept an (?:den|die) Versicherte(?:n)? (.+)$")
  public void givenIssueSingleRandomPrescriptionsToActor(String patientName) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);

    givenThat(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .randomPrescription());
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (.+) folgende(?:s)? E-Rezept(?:e)? an (?:den|die) Versicherte(?:n)?"
          + " (.+) verschreibt:$")
  public void whenIssueMultiplePrescriptionsToActor(
      String docName, String patientName, DataTable medications) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .forPznPrescription(medications.asMaps()));
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) folgende(?:s)? E-Rezept(?:e)? an (?:den|die) Versicherte(?:n)? (.+)"
          + " verschreibt:$")
  public void whenIssueMultiplePrescriptionsToActor(String patientName, DataTable medications) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .forPznPrescription(medications.asMaps()));
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (.+) ein E-Rezept an (?:den|die) Versicherte(?:n)? (.+)"
          + " verschreibt$")
  public void whenIssueSingleRandomPrescriptionsToActor(String docName, String patientName) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .randomPrescription());
  }

  @Wenn("^(?:der Arzt|die Ärztin) ein E-Rezept an (?:den|die) Versicherte(?:n)? (.+) verschreibt$")
  public void whenIssueMultiplePrescriptionsToActor(String patientName) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .randomPrescription());
  }

  /**
   * TMD-1598 Variante Apothekenpflichtiges Medikament
   *
   * @param docName ist der Name des ausstellenden Arztes
   * @param patientName ist der Name des empfangenden Patienten
   * @param medications ist die Liste der Medikationen, die der Arzt dem Patienten ausstellt
   */
  @Wenn(
      "^(?:der Arzt|die Ärztin) (.+) (?:dem|der) Versicherten (.+) folgende(?:s)?"
          + " apothekenpflichtige(?:s)? Medikament(?:e)? verschreibt:$")
  public void whenIssuePharmacyOnlyPrescriptionToActor(
      String docName, String patientName, DataTable medications) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .forPznPrescription(medications.asMaps()));
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (.+) (?:dem|der) Versicherten (.+) folgende"
          + " apothekenpflichtige Freitext Verordnung(?:en)? verschreibt:$")
  public void whenIssuePharmacyOnlyFreeTextPrescriptionToActor(
      String docName, String patientName, DataTable freitextVerordnung) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .forFreitextVerordnung(freitextVerordnung.asMaps()));
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (.+) (?:dem|der) Versicherten (.+) folgende"
          + " apothekenpflichtige Rezeptur Verordnung(?:en)? verschreibt:$")
  public void whenIssuePharmacyOnlyCompoundingPrescriptionToActor(
      String docName, String patientName, DataTable rezepturVerordnung) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .forRezepturVerordnung(rezepturVerordnung.asMaps()));
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (.+) (?:dem|der) Versicherten (.+) folgende"
          + " apothekenpflichtige Wirkstoff Verordnung(?:en)? verschreibt:$")
  public void whenIssuePharmacyOnlyIngredientPrescriptionToActor(
      String docName, String patientName, DataTable wirkstoffVerordnung) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .forWirkstoffVerordnung(wirkstoffVerordnung.asMaps()));
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (?:dem|der) Versicherten (.+) folgende(?:s)?"
          + " apothekenpflichtige(?:s)? Medikament(?:e)? verschreibt:$")
  public void whenIssuePharmacyOnlyPrescriptionToActor(String patientName, DataTable medications) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .forPznPrescription(medications.asMaps()));
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (.+) (?:dem|der) Versicherten (.+) ein apothekenpflichtiges"
          + " Medikament verschreibt$")
  @Angenommen(
      "^(?:der Arzt|die Ärztin) (.+) hat (?:dem|der) Versicherten (.+) ein apothekenpflichtiges"
          + " Medikament verschrieben$")
  public void whenIssueSingleRandomPharmacyOnlyPrescriptionToActor(
      String docName, String patientName) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .randomPrescription());
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (?:dem|der) Versicherten (.+) ein apothekenpflichtiges Medikament"
          + " verschreibt$")
  public void whenIssuePharmacyOnlyPrescriptionToActor(String patientName) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .randomPrescription());
  }

  /**
   * Dies ist eine temporäre Vorgabe, gemäß A_22222 bei der sonstige Kostenträger vom E-Rezept
   * ausgeschlossen sind
   *
   * @param docName ist der Name des verschreibenden Arztes
   * @param medications ist die Liste der Informationen zu den einzelnen Verschreibungen
   */
  @Dann(
      "^darf (?:der Arzt|die Ärztin) (.+) (?:diesem|dieser) Versicherten das folgende E-Rezept"
          + " nicht ausstellen:$")
  public void thenIsNotAllowedToPrescribe(String docName, DataTable medications) {
    val thePatient = OnStage.theActorInTheSpotlight();
    val theDoctor = OnStage.theActorCalled(docName);

    then(theDoctor)
        .attemptsTo(
            Negate.the(
                    IssuePrescription.forPatient(thePatient)
                        .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                        .forPznPrescription(medications.asMaps()))
                .with(UnexpectedResponseResourceError.class));
  }

  @Dann(
      "^darf (?:der Arzt|die Ärztin) (.+) (?:der|dem) Versicherten (.+) kein E-Rezept ausstellen$")
  public void thenIsNotAllowedToPrescribe(String docName, String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val theDoctor = OnStage.theActorCalled(docName);

    then(theDoctor)
        .attemptsTo(
            Negate.the(
                    IssuePrescription.forPatient(thePatient)
                        .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                        .randomPrescription())
                .with(UnexpectedResponseResourceError.class));
  }

  /**
   * Dies ist eine temporäre Vorgabe gemäß A_22222, bei der sonstige Kostenträger vom E-Rezept
   * ausgeschlossen sind
   *
   * @param docName ist der Name des verschreibenden Arztes
   * @param patientName ist der Name des Patienten, an den die Verschreibung ausgestellt wird
   * @param medications ist die Liste der Informationen zu den einzelnen Verschreibungen
   */
  @Dann(
      "^darf (?:der Arzt|die Ärztin) (.+) (?:dem|der) Versicherten (.+) das folgende E-Rezept nicht"
          + " ausstellen:$")
  public void thenIsNotAllowedToPrescribe(
      String docName, String patientName, DataTable medications) {
    val thePatient = OnStage.theActorCalled(patientName);
    val theDoctor = OnStage.theActorCalled(docName);

    then(theDoctor)
        .attemptsTo(
            Negate.the(
                    IssuePrescription.forPatient(thePatient)
                        .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                        .forPznPrescription(medications.asMaps()))
                .with(UnexpectedResponseResourceError.class));
  }

  @Dann(
      "^kann (?:der Arzt|die Ärztin) (.+) (?:der|dem) Versicherten (.+) kein E-Rezept verschreiben,"
          + " weil die PZN eine falsche Länge hat$")
  public void thenPznIsNotAllowed(String docName, String patientName, DataTable medications) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);

    then(theDoctor)
        .attemptsTo(
            Negate.the(
                    IssuePrescription.forPatient(thePatient)
                        .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                        .forPznPrescription(medications.asMaps()))
                .with(UnexpectedResponseResourceError.class));
  }
}
