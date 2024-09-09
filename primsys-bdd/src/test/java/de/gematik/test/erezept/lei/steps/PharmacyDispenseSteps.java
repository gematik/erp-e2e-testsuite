/*
 * Copyright 2024 gematik GmbH
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

import static net.serenitybdd.screenplay.GivenWhenThen.*;

import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.screenplay.questions.GetMedicationDispense;
import de.gematik.test.erezept.screenplay.questions.ResponseOfClosePrescriptionOperation;
import de.gematik.test.erezept.screenplay.questions.ResponseOfDispensePrescriptionAsBundle;
import de.gematik.test.erezept.screenplay.questions.ResponseOfReDispenseMedication;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.task.*;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

public class PharmacyDispenseSteps {

  @Wenn("^die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept korrekt an (.+) dispensiert$")
  public void whenDispenseMedicationTo(String pharmName, String order, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePharmacy)
        .attemptsTo(ClosePrescription.toPatient(order, thePatient).withPrescribedMedications());
  }

  @Wenn("^die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept korrekt dispensiert$")
  public void whenDispenseMedication(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy).attemptsTo(ClosePrescription.fromStack(order).withPrescribedMedications());
  }

  @Wenn("^die Apotheke das (letzte|erste) akzeptierte E-Rezept korrekt dispensiert$")
  public void whenDispenseMedication(String order) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    when(thePharmacy).attemptsTo(ClosePrescription.fromStack(order).withPrescribedMedications());
  }

  @Dann("^kann die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept korrekt dispensieren$")
  @Und("^die Apotheke (.+) kann das (letzte|erste) akzeptierte E-Rezept korrekt dispensieren$")
  public void thenDispenseMedication(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy).attemptsTo(ClosePrescription.fromStack(order).withPrescribedMedications());
  }

  @Wenn(
      "^die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept mit den folgenden Medikamenten"
          + " korrekt an (.+) dispensiert:$")
  public void whenDispenseAlternativeReplacementMedications(
      String pharmName, String order, String patientName, DataTable medications) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePharmacy)
        .attemptsTo(
            ClosePrescription.toPatient(order, thePatient).withAlternativeMedications(medications));
  }

  @Wenn(
      "^die Apotheke das (letzte|erste) akzeptierte E-Rezept mit den folgenden Medikamenten korrekt"
          + " an (.+) dispensiert:$")
  public void whenDispenseAlternativeReplacementMedications(
      String order, String patientName, DataTable medications) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePharmacy)
        .attemptsTo(
            ClosePrescription.toPatient(order, thePatient).withAlternativeMedications(medications));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept nicht mit dem falschen"
          + " Secret (.+) dispensieren$")
  @Und(
      "^die Apotheke (.+) kann das (letzte|erste) akzeptierte E-Rezept nicht mit dem falschen"
          + " Secret (.+) dispensieren$")
  public void thenDispenseMedicationWithWrongSecret(
      String pharmName, String order, String wrongSecret) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            Negate.the(ClosePrescription.withSecret(order, wrongSecret).withPrescribedMedications())
                .with(UnexpectedResponseResourceError.class));
  }

  @Dann(
      "^kann die Apotheke das (letzte|erste) akzeptierte E-Rezept nicht mit dem falschen Secret"
          + " (.+) dispensieren$")
  @Und(
      "^die Apotheke kann das (letzte|erste) akzeptierte E-Rezept nicht mit dem falschen Secret"
          + " (.+) dispensieren$")
  public void thenDispenseMedicationWithWrongSecret(String order, String wrongSecret) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy)
        .attemptsTo(
            Negate.the(ClosePrescription.withSecret(order, wrongSecret).withPrescribedMedications())
                .with(UnexpectedResponseResourceError.class));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept nicht an den Versicherten"
          + " mit KVNR (.+) dispensieren$")
  @Und(
      "^die Apotheke (.+) kann das (letzte|erste) akzeptierte E-Rezept nicht an den Versicherten"
          + " mit KVNR (.+) dispensieren$")
  public void thenDispenseMedicationToWrongPerson(
      String pharmName, String order, String wrongKvnr) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            Negate.the(ClosePrescription.toKvnr(order, wrongKvnr).withPrescribedMedications())
                .with(UnexpectedResponseResourceError.class));
  }

  @Dann(
      "^kann die Apotheke das (letzte|erste) akzeptierte E-Rezept nicht an den Versicherten mit"
          + " KVNR (.+) dispensieren$")
  @Und(
      "^die Apotheke kann das (letzte|erste) akzeptierte E-Rezept nicht an den Versicherten mit"
          + " KVNR (.+) dispensieren$")
  public void thenDispenseMedicationToWrongPerson(String order, String wrongKvnr) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy)
        .attemptsTo(
            Negate.the(ClosePrescription.toKvnr(order, wrongKvnr).withPrescribedMedications())
                .with(UnexpectedResponseResourceError.class));
  }

  @Dann("^kann die Apotheke das (letzte|erste) akzeptierte E-Rezept korrekt dispensieren$")
  @Und("^die Apotheke kann das (letzte|erste) akzeptierte E-Rezept korrekt dispensieren$")
  public void thenDispenseMedication(String order) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy).attemptsTo(ClosePrescription.fromStack(order).withPrescribedMedications());
  }

  @Dann("^kann die Apotheke (.+) noch kein E-Rezept dispensieren$")
  @Und("^die Apotheke (.+) kann(?: noch)? kein E-Rezept dispensieren$")
  public void thenCannotDispenseMedication(String pharmName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            Negate.the(ClosePrescription.fromStack(DequeStrategy.LIFO).withPrescribedMedications())
                .with(MissingPreconditionError.class));
  }

  @Dann("^kann die Apotheke(?: noch)? kein E-Rezept dispensieren$")
  @Und("^die Apotheke kann(?: noch)? kein E-Rezept dispensieren$")
  public void thenCannotDispenseMedication() {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy)
        .attemptsTo(
            Negate.the(ClosePrescription.fromStack(DequeStrategy.LIFO).withPrescribedMedications())
                .with(MissingPreconditionError.class));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept nicht dispensieren, weil es"
          + " nicht mehr existiert$")
  public void thenCannotDispenseMedicationWith410(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfClosePrescriptionOperation.fromStack(order)
                        .forPrescribedMedications())
                .isEqualTo(410));
  }

  @Dann(
      "^kann die Apotheke das (letzte|erste) akzeptierte E-Rezept nicht dispensieren, weil es nicht"
          + " mehr existiert$")
  public void thenCannotDispenseMedicationWith410(String order) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfClosePrescriptionOperation.fromStack(order)
                        .forPrescribedMedications())
                .isEqualTo(410));
  }

  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) akzeptierte E-Rezept nicht dispensieren, weil sie"
          + " nicht das Recht dazu hat$")
  public void thenCannotDispenseMedicationWith403(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfClosePrescriptionOperation.fromStack(order)
                        .forPrescribedMedications())
                .isEqualTo(403));
  }

  @Dann(
      "^kann die Apotheke das (letzte|erste) akzeptierte E-Rezept nicht dispensieren, weil sie"
          + " nicht das Recht dazu hat$")
  public void thenCannotDispenseMedicationWith403(String order) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfClosePrescriptionOperation.fromStack(order)
                        .forPrescribedMedications())
                .isEqualTo(403));
  }

  /**
   * In diesem Schritt versucht eine Apotheke ein bereits dispensiertes E-Rezept erneut zu
   * dispensieren.
   *
   * @param pharmName ist der Name der Apotheke
   * @param order ist die Reihenfolge der Quittung eines bereits dispensierten E-Rezeptes
   */
  @Dann(
      "^kann die Apotheke (.+) das (letzte|erste) dispensierte E-Rezept nicht erneut dispensieren$")
  public void thenNotAllowedToDispenseMedication(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfReDispenseMedication.fromStack(order)).isEqualTo(403));
  }

  @Dann(
      "^darf die Apotheke (.+) die Dispensierinformationen für das (erste|letzte) dispensierte"
          + " E-Rezept nicht abrufen$")
  public void thenPharmacyCannotGetMedicationDispense(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy)
        .attemptsTo(
            ThatNotAllowedToAsk.the(GetMedicationDispense.asPharmacy().forPrescription(order))
                .with(UnexpectedResponseResourceError.class));
  }

  @Und(
      "^die Apotheke (.+) für das (letzte|erste) akzeptierte E-Rezept von (.+) die"
          + " Dispensierinformationen zeitnah bereitstellt$")
  public void andPharmacyProvidesDispensingInformationTimelyManner(
      String pharmName, String order, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    and(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfDispensePrescriptionAsBundle.fromStack(order)
                        .forPatient(thePatient)
                        .build())
                .isEqualTo(200));
  }
}
