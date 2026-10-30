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
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.common.screenplay.questions.HasDispensedDrugs;
import de.gematik.test.erezept.fdv.questions.*;
import de.gematik.test.erezept.fdv.task.DeleteRedeemablePrescription;
import de.gematik.test.erezept.fdv.task.EnsureThatThePrescription;
import io.cucumber.java.de.Aber;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.Ensure;

public class PrescriptionSteps {
  @Wenn("^(?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept löscht$")
  public void whenDeletePrescription(String userName, String order) {
    val theAppUser = OnStage.theActorCalled(userName);
    when(theAppUser).attemptsTo(DeleteRedeemablePrescription.fromStack(order));
  }

  @Wenn("^(?:der|die) Versicherte (?:sein|ihr) (letztes|erstes) E-Rezept in der App löscht$")
  public void whenDeletePrescription(String order) {
    val theAppUser = OnStage.theActorInTheSpotlight();
    when(theAppUser).attemptsTo(DeleteRedeemablePrescription.fromStack(order));
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept nicht"
          + " löschen, weil (?:sie|er) nicht das Recht dazu hat$")
  @Aber(
      "^(?:der|die) Versicherte (.+) kann (?:sein|ihr) (letztes|erstes) E-Rezept nicht"
          + " löschen, weil (?:sie|er) nicht das Recht dazu hat$")
  public void thenCannotDeletePrescription(String userName, String order) {
    val theAppUser = OnStage.theActorCalled(userName);
    then(theAppUser)
        .attemptsTo(Ensure.that(DeletingThePrescription.wasSuccessful(order)).isFalse());
  }

  @Wenn("^(?:der|dem) Versicherten (.+) das (letzte|erste) E-Rezept in der App angezeigt wird$")
  public void whenCheckPrescription(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(EnsureThatThePrescription.fromStack(order).isShownCorrectly());
  }

  @Dann("^wird (?:der|dem) Versicherten das (letzte|erste) E-Rezept in der App angezeigt$")
  @Und("^(?:der|dem) Versicherten wird das (letzte|erste) E-Rezept in der App angezeigt$")
  public void thenCheckPrescription(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient).attemptsTo(EnsureThatThePrescription.fromStack(order).isShownCorrectly());
  }

  @Dann("^wird (?:der|dem) Versicherten (.+) das (letzte|erste) E-Rezept angezeigt$")
  @Und("^(?:der|dem) Versicherten (.+) wird das (letzte|erste) E-Rezept angezeigt$")
  public void thenCheckPrescription(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient).attemptsTo(EnsureThatThePrescription.fromStack(order).isShownCorrectly());
  }

  @Dann(
      "^hat (?:der|die) Versicherte (.+) (\\d+) Dispensierinformation(?:en)? für (?:sein|ihr)"
          + " (erstes|letztes) E-Rezept abrufen$")
  public void thenReceivedPrescription(String userName, String order) {
    val theAppUser = OnStage.theActorCalled(userName);
    assertTrue(
        then(theAppUser).asksFor(HasReceivedDispensedMedication.fromStack(order)),
        "E-Rezept elektronisch nicht erhalten");
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) (\\d+) Dispensierinformation(?:en)? für (?:sein|ihr)"
          + " (erstes|letztes) E-Rezept abrufen$")
  public void thenPatientGetsMedicationDispense(String patientName, long amount, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            Ensure.that(
                    MedicationDispenseContains.forPatient()
                        .andPrescription(order)
                        .numberOfMedicationDispenses(amount))
                .isTrue());
  }

  @Und("^(?:der|die) Versicherte (.+) hat das (letzte|erste) E-Rezept elektronisch erhalten")
  public void andReceivedPrescription(String userName, String order) {
    val theAppUser = OnStage.theActorCalled(userName);
    assertTrue(
        and(theAppUser).asksFor(HasReceivedDispensedMedication.fromStack(order)),
        "E-Rezept elektronisch nicht erhalten");
  }

  @Dann(
      "^wird (?:der|dem) Versicherten (.+) (?:sein|ihr) letztes (ausgestellte|gelöschte) E-Rezept"
          + " nicht mehr angezeigt$")
  public void thenPrescriptionNotDisplayed(String patientName, String stack) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(Ensure.that(TheLastPrescription.from(stack).existsInBackend()).isFalse());
  }

  /**
   * Der angegebene Patient ruft das letzte verschriebene Rezept auf dem Patientenstapel beim FD ab
   *
   * @param patientName ist der Name des Versicherten
   * @see <a href="https://service.gematik.de/browse/TMD-1605">TMD-1605</a>
   */
  @Dann("^wird (?:der|dem) Versicherten (.+) das neue E-Rezept angezeigt$")
  @Dann("^wird (?:der|dem) Versicherten (.+) das neue E-Rezept ohne AccessCode angezeigt$")
  public void thenFetchPrescriptionFromBackend(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(Ensure.that("", TheLastPrescription.prescribed().existsInBackend()).isTrue());
  }

  @Dann(
      "^wird das letzte (ausgestellte|gelöschte) E-Rezept (?:dem|der) Versicherten nicht mehr"
          + " angezeigt$")
  public void thenPrescriptionNotDisplayed(String stack) {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient)
        .attemptsTo(Ensure.that(TheLastPrescription.from(stack).existsInBackend()).isFalse());
  }

  @Dann(
      "^hat (?:der|die) Versicherte (.+) (mindestens|maximal|genau) (\\d+) Medikament(?:e)?"
          + " erhalten$")
  public void thenReceivedDrugs(String patientName, String adverb, long amount) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient).attemptsTo(Ensure.that(HasDispensedDrugs.of(adverb, amount)).isTrue());
  }

  @Und(
      "^(?:der|die) Versicherte (.+) hat (mindestens|maximal|genau) (\\d+) Medikament(?:e)?"
          + " erhalten$")
  public void andReceivedDrugs(String patientName, String adverb, long amount) {
    val thePatient = OnStage.theActorCalled(patientName);
    and(thePatient).attemptsTo(Ensure.that(HasDispensedDrugs.of(adverb, amount)).isTrue());
  }

  @Und(
      "^(?:der|die) Versicherte kann (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen, weil"
          + " (?:sie|er) nicht das Recht dazu hat$")
  public void andCannotDeletePrescription403(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    and(thePatient)
        .attemptsTo(
            Ensure.that(ResponseOfAbortOperation.asPatient().fromStack(order)).isEqualTo("403"));
  }
}
