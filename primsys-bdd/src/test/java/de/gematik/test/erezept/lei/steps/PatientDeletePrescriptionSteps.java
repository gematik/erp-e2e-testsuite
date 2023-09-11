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

import de.gematik.test.erezept.screenplay.questions.ResponseOfAbortOperation;
import de.gematik.test.erezept.screenplay.task.AbortPrescription;
import de.gematik.test.erezept.screenplay.task.CheckTheReturnCode;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

import static net.serenitybdd.screenplay.GivenWhenThen.*;
import static net.serenitybdd.screenplay.GivenWhenThen.then;

public class PatientDeletePrescriptionSteps {

  /**
   * TMD-1624
   *
   * @param patientName ist der Name des Versicherten
   */
  @Wenn("^(?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept löscht$")
  public void whenDeletePrescription(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(AbortPrescription.asPatient().fromStack(order));
  }

  @Wenn("^(?:der|die) Versicherte (?:sein|ihr) (letztes|erstes) E-Rezept löscht$")
  public void whenDeletePrescription(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    when(thePatient).attemptsTo(AbortPrescription.asPatient().fromStack(order));
  }

  @Dann("^kann (?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen$")
  public void thenCannotDeletePrescription(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(400));
  }

  @Dann("^kann (?:der|die) Versicherte (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen$")
  public void thenCannotDeletePrescription(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(400));
  }

  @Und("^(?:der|die) Versicherte (.+) kann (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen$")
  public void andCannotDeletePrescription(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    and(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(400));
  }

  @Und("^(?:der|die) Versicherte kann (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen$")
  public void andCannotDeletePrescription(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    and(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(400));
  }

  @Und(
      "^(?:der|die) Versicherte (.+) kann (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen, weil (?:sie|er) nicht das Recht dazu hat$")
  public void andCannotDeletePrescription403(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    and(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(403));
  }

  @Und(
      "^(?:der|die) Versicherte kann (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen, weil (?:sie|er) nicht das Recht dazu hat$")
  public void andCannotDeletePrescription403(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    and(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(403));
  }

  /**
   * Negierung von TMD-1624
   *
   * @param patientName ist der Name des Versicherten
   */
  @Dann(
      "^kann (?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen, weil es einen Konflikt gibt$")
  public void thenCannotDeletePrescription409(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(409));
  }

  @Dann(
      "^kann (?:der|die) Versicherte (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen, weil es einen Konflikt gibt$")
  public void thenCannotDeletePrescription409(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(409));
  }

  @Und(
      "^(?:der|die) Versicherte (.+) kann (?:seine|ihr) (letztes|erstes) E-Rezept nicht löschen, weil es einen Konflikt gibt$")
  public void andCannotDeletePrescription409(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(409));
  }

  @Und(
      "^(?:der|die) Versicherte kann (?:seine|ihr) (letztes|erstes) E-Rezept nicht löschen, weil es einen Konflikt gibt$")
  public void andCannotDeletePrescription409(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(409));
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen, weil (?:sie|er) nicht das Recht dazu hat$")
  public void thenCannotDeletePrescription403(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(403));
  }

  @Dann(
      "^kann (?:der|die) Versicherte (?:sein|ihr) (letztes|erstes) E-Rezept nicht löschen, weil (?:sie|er) nicht das Recht dazu hat$")
  public void thenCannotDeletePrescription403(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asPatient().fromStack(order))
                .isEqualTo(403));
  }
}
