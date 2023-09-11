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

package de.gematik.test.erezept.app.steps;

import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.GivenWhenThen.when;
import static org.junit.Assert.assertTrue;

import de.gematik.test.erezept.app.questions.HasReceivedPrescription;
import de.gematik.test.erezept.app.questions.MovingToPrescription;
import de.gematik.test.erezept.app.task.DeleteRedeemablePrescription;
import de.gematik.test.erezept.app.task.EnsureThatThePrescription;
import de.gematik.test.erezept.app.task.EnsureThatThePrescriptionValidity;
import de.gematik.test.erezept.app.task.RefreshPrescriptions;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import java.util.Optional;
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
      "^wird (?:der|dem) Versicherten (.+) (?:sein|ihr) (letztes|erstes) (ausgestellte|gelöschte) E-Rezept in der App nicht mehr angezeigt$")
  public void thenPrescriptionNotDisplayed(String patientName, String order, String stack) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            Ensure.that(
                "the deleted prescription is gone",
                MovingToPrescription.fromStack(stack).withDeque(order),
                Optional::isEmpty));
  }

  @Dann(
      "^wird das (letzte|erste) (ausgestellte|gelöschte) E-Rezept (?:dem|der) Versicherten in der App nicht mehr angezeigt$")
  public void thenPrescriptionNotDisplayed(String order, String stack) {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient)
        .attemptsTo(
            Ensure.that(
                "the deleted prescription is gone",
                MovingToPrescription.fromStack(stack).withDeque(order),
                Optional::isEmpty));
  }

  @Dann("^wird (?:der|dem) Versicherten (.+) das (letzte|erste) E-Rezept in der App angezeigt$")
  @Dann(
      "^wird (?:der|dem) Versicherten (.+) das (letzte|erste) E-Rezept in der App ohne AccessCode angezeigt$")
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
      "^wird (?:der|dem) Versicherten (.+) das (letzte|erste) E-Rezept noch für (\\d+) Tage gültig angezeigt$")
  public void thenCheckPrescriptionValidity(String patientName, String order, int remainingDays) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            EnsureThatThePrescriptionValidity.fromStack(order)
                .isStillValidForRemainingDays(remainingDays));
  }

  @Dann(
      "^wird (?:der|dem) Versicherten das (letzte|erste) E-Rezept noch für (\\d+) Tage gültig angezeigt$")
  @Und(
      "^(?:der|dem) Versicherten wird das (letzte|erste) E-Rezept noch für (\\d+) Tage gültig angezeigt$")
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

  @Dann("^hat (?:der|die) Versicherte (.+) das letzte E-Rezept elektronisch erhalten")
  public void thenReceivedPrescription(String userName) {
    val theAppUser = OnStage.theActorCalled(userName);
    assertTrue(
        "E-Rezept elektronisch nicht erhalten",
        then(theAppUser).asksFor(HasReceivedPrescription.withSomeStrategy()));
  }

  @Dann("^hat (?:der|die) Versicherte das letzte E-Rezept elektronisch erhalten")
  public void thenReceivedPrescription() {
    val theAppUser = OnStage.theActorInTheSpotlight();
    assertTrue(
        "E-Rezept elektronisch nicht erhalten",
        then(theAppUser).asksFor(HasReceivedPrescription.withSomeStrategy()));
  }
}
