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

import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.screenplay.questions.ResponseOfAbortOperation;
import de.gematik.test.erezept.screenplay.task.AbortPrescription;
import de.gematik.test.erezept.screenplay.task.CheckTheReturnCode;
import de.gematik.test.erezept.screenplay.task.Negate;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

public class DoctorDeletePrescriptionSteps {

  /**
   * Löschen eines Rezeptes durch den verschreibenden Arzt
   *
   * @param docName ist der Name des verschreibenden Arztes
   * @param order definiert das Rezept auf dem Stack des Arztes
   */
  @Wenn(
      "^(?:der Arzt|die Ärztin) (.+) das (letzte|erste) von (?:ihm|ihr) eingestellte E-Rezept"
          + " löscht$")
  public void whenDocAbortsIssuedPrescription(String docName, String order) {
    val theDoctor = OnStage.theActorCalled(docName);
    when(theDoctor).attemptsTo(AbortPrescription.asDoctor().fromStack(order));
  }

  @Wenn("^(?:der Arzt|die Ärztin) das (letzte|erste) von (?:ihm|ihr) eingestellte E-Rezept löscht$")
  public void whenDocAbortsIssuedPrescription(String order) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    when(theDoctor).attemptsTo(AbortPrescription.asDoctor().fromStack(order));
  }

  /**
   * Negierung Löschen eines Rezeptes durch den verschreibenden Arzt
   *
   * @param docName ist der Name des verschreibenden Arztes
   */
  @Dann(
      "^darf (?:der Arzt|die Ärztin) (.+) das (letzte|erste) von (?:ihm|ihr) eingestellte E-Rezept"
          + " nicht löschen$")
  public void thenIsNotAllowedToAbortPrescription(String docName, String order) {
    val theDoctor = OnStage.theActorCalled(docName);
    then(theDoctor)
        .attemptsTo(
            Negate.the(AbortPrescription.asDoctor().fromStack(order))
                .with(UnexpectedResponseResourceError.class));
  }

  @Dann(
      "^darf (?:der Arzt|die Ärztin) das (letzte|erste) von (?:ihm|ihr) eingestellte E-Rezept nicht"
          + " löschen$")
  public void thenIsNotAllowedToAbortPrescription(String order) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    then(theDoctor)
        .attemptsTo(
            Negate.the(AbortPrescription.asDoctor().fromStack(order))
                .with(UnexpectedResponseResourceError.class));
  }

  @Dann(
      "^kann (?:der Arzt|die Ärztin) (.+) das (letzte|erste) von (?:ihm|ihr) eingestellte E-Rezept"
          + " nicht löschen, weil (?:er|sie) nicht das Recht dazu hat$")
  public void thenHasNoRightToAbortPrescription(String docName, String order) {
    val theDoctor = OnStage.theActorCalled(docName);
    then(theDoctor)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asDoctor().fromStack(order))
                .isEqualTo(403));
  }

  @Dann(
      "^kann (?:der Arzt|die Ärztin) das (letzte|erste) von (?:ihm|ihr) eingestellte E-Rezept nicht"
          + " löschen, weil (?:er|sie) nicht das Recht dazu hat$")
  public void thenHasNoRightToAbortPrescription(String order) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    then(theDoctor)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asDoctor().fromStack(order))
                .isEqualTo(403));
  }

  @Dann(
      "^kann (?:der Arzt|die Ärztin) (.+) das (letzte|erste) von (?:ihm|ihr) eingestellte E-Rezept"
          + " nicht löschen, weil es einen Konflikt gibt$")
  public void thenConflictToAbortPrescription(String docName, String order) {
    val theDoctor = OnStage.theActorCalled(docName);
    then(theDoctor)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asDoctor().fromStack(order))
                .isEqualTo(409));
  }

  @Dann(
      "^kann (?:der Arzt|die Ärztin) das (letzte|erste) von (?:ihm|ihr) eingestellte E-Rezept nicht"
          + " löschen, weil es einen Konflikt gibt$")
  public void thenConflictToAbortPrescription(String order) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    then(theDoctor)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asDoctor().fromStack(order))
                .isEqualTo(409));
  }
}
