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

import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.GivenWhenThen.when;
import static org.junit.Assert.assertTrue;

import de.gematik.test.erezept.app.questions.DeletingThePrescription;
import de.gematik.test.erezept.app.questions.HasReceivedDispensedMedication;
import de.gematik.test.erezept.app.questions.PrescriptionHasGone;
import de.gematik.test.erezept.app.task.DeleteRedeemablePrescription;
import de.gematik.test.erezept.app.task.EnsureThatThePrescription;
import de.gematik.test.erezept.app.task.EnsureThatThePrescriptionValidity;
import de.gematik.test.erezept.app.task.RefreshPrescriptions;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.Ensure;

public class PrescriptionSteps {

  @Wenn("^(?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept in der App löscht$")
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
      "^kann (?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept in der App nicht"
          + " löschen")
  public void thenCannotDeletePrescription(String userName, String order) {
    val theAppUser = OnStage.theActorCalled(userName);
    then(theAppUser)
        .attemptsTo(Ensure.that(DeletingThePrescription.canNotBePerformedFor(order)).isTrue());
  }

  @Dann(
      "^wird (?:der|dem) Versicherten (.+) (?:sein|ihr) (letztes|erstes) (ausgestellte|gelöschte)"
          + " E-Rezept in der App nicht mehr angezeigt$")
  public void thenPrescriptionNotDisplayed(String patientName, String order, String stack) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            Ensure.that(
                    "the deleted prescription is gone",
                    PrescriptionHasGone.fromStack(stack).withDeque(order))
                .isTrue());
  }

  @Dann(
      "^wird das (letzte|erste) (ausgestellte|gelöschte) E-Rezept (?:dem|der) Versicherten in der"
          + " App nicht mehr angezeigt$")
  public void thenPrescriptionNotDisplayed(String order, String stack) {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient)
        .attemptsTo(
            Ensure.that(
                    "the deleted prescription is gone",
                    PrescriptionHasGone.fromStack(stack).withDeque(order))
                .isTrue());
  }

  @Wenn("^(?:der|dem) Versicherten (.+) das (letzte|erste) E-Rezept in der App angezeigt wird$")
  public void whenCheckPrescription(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(EnsureThatThePrescription.fromStack(order).isShownCorrectly());
  }

  @Wenn("^(?:der|dem) Versicherten das (letzte|erste) E-Rezept in der App angezeigt wird$")
  public void whenCheckPrescription(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    when(thePatient).attemptsTo(EnsureThatThePrescription.fromStack(order).isShownCorrectly());
  }

  @Dann("^wird (?:der|dem) Versicherten (.+) das (letzte|erste) E-Rezept in der App angezeigt$")
  @Und("^(?:der|dem) Versicherten (.+) wird das (letzte|erste) E-Rezept in der App angezeigt$")
  public void thenCheckPrescription(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient).attemptsTo(EnsureThatThePrescription.fromStack(order).isShownCorrectly());
  }

  @Dann("^wird (?:der|dem) Versicherten das (letzte|erste) E-Rezept in der App angezeigt$")
  @Und("^(?:der|dem) Versicherten wird das (letzte|erste) E-Rezept in der App angezeigt$")
  public void thenCheckPrescription(String order) {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient).attemptsTo(EnsureThatThePrescription.fromStack(order).isShownCorrectly());
  }

  @Dann(
      "^wird (?:der|dem) Versicherten (.+) das (letzte|erste) E-Rezept noch für (\\d+) Tage"
          + " einlösbar angezeigt$")
  public void thenCheckPrescriptionValidity(String patientName, String order, int remainingDays) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            EnsureThatThePrescriptionValidity.fromStack(order)
                .isStillValidForRemainingDays(remainingDays));
  }

  @Dann(
      "^wird (?:der|dem) Versicherten das (letzte|erste) E-Rezept noch für (\\d+) Tage einlösbar"
          + " angezeigt$")
  @Und(
      "^(?:der|dem) Versicherten wird das (letzte|erste) E-Rezept noch für (\\d+) Tage einlösbar"
          + " angezeigt$")
  public void thenCheckPrescriptionValidity(String order, int remainingDays) {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient)
        .attemptsTo(
            EnsureThatThePrescriptionValidity.fromStack(order)
                .isStillValidForRemainingDays(remainingDays));
  }

  @Wenn("^(?:der|die) Versicherte (.+) (?:seine|ihre) E-Rezepte abruft")
  public void whenRefreshPrescriptions(String userName) {
    val theAppUser = OnStage.theActorCalled(userName);
    when(theAppUser).attemptsTo(RefreshPrescriptions.byTap());
  }

  @Wenn("^(?:der|die) Versicherte (?:seine|ihre) E-Rezepte abruft")
  public void whenRefreshPrescriptions() {
    val theAppUser = OnStage.theActorInTheSpotlight();
    when(theAppUser).attemptsTo(RefreshPrescriptions.byTap());
  }

  @Dann("^hat (?:der|die) Versicherte (.+) das (letzte|erste) E-Rezept elektronisch erhalten")
  public void thenReceivedPrescription(String userName, String order) {
    val theAppUser = OnStage.theActorCalled(userName);
    assertTrue(
        "E-Rezept elektronisch nicht erhalten",
        then(theAppUser).asksFor(HasReceivedDispensedMedication.fromStack(order)));
  }

  @Dann("^hat (?:der|die) Versicherte das (letzte|erste) E-Rezept elektronisch erhalten")
  public void thenReceivedPrescription(String order) {
    val theAppUser = OnStage.theActorInTheSpotlight();
    assertTrue(
        "E-Rezept elektronisch nicht erhalten",
        then(theAppUser).asksFor(HasReceivedDispensedMedication.fromStack(order)));
  }
}
