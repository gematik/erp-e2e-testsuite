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

import static net.serenitybdd.screenplay.GivenWhenThen.*;

import de.gematik.test.erezept.screenplay.questions.*;
import de.gematik.test.erezept.screenplay.task.*;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.Ensure;

/**
 * Testschritte die aus der Perspektive eines Versicherten bzw. einer Versicherten ausgeführt werden
 */
@Slf4j
public class PatientSteps {

  /**
   * Prüfe, ob der angegebene Versicherte mit dem Namen aus {@code patientName} die angegebene
   * Versicherungsart aus {@code insuranceType} zugewiesen bekommen hat.
   *
   * <p>Dieser Schritt führt <b>keine</b> Aktion beim Fachdienst aus, sondern prüft lediglich die
   * Versicherungsart dem Versicherten zugewiesen wurde
   *
   * @param patientName ist der Name des Versicherten, dessen Versicherungsart geprüft wird
   * @param insuranceType ist die erwartete Versicherungsart
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) die Versicherungsart (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI)"
          + " aufweist$")
  public void whenInsuranceTypeIs(String patientName, String insuranceType) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient)
        .attemptsTo(Ensure.that(HisInsuranceType.equalsExpected(insuranceType)).isTrue());
  }

  /**
   * Dieser Schritt bildet den Vorgang in einer realen Apotheke nach. Der Versicherte mit dem Namen
   * aus {@code patientName} ruft zunächst alle seine E-Rezepte beim Fachdienst ab und wählt das
   * gewünschte E-Rezept anhand von {@code order} aus. Im Anschluss erfolgt die Zuweisung über den
   * Data Matrix Code an die Apotheke mit dem Namen {@code pharmacyName}.
   *
   * <p>Mit der Zuweisung über den DMC (Data * Matrix Code) findet keine weitere Interaktion durch
   * den Versicherten mit dem Fachdienst statt. Stattdessen wird dem Apotheker der DMC vorgezeigt.
   *
   * @param patientName ist der Name des Versicherten, der ein E-Rezept bei einer Apotheke einlösen
   *     möchte
   * @param order ist die Reihenfolge, mit der das gewünschte E-Rezept ausgewählt wird
   * @param pharmacyName ist die Apotheke, welche den DMC zugewiesen bekommt
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) (ausgestellte|gelöschte)"
          + " E-Rezept der Apotheke (.+) via Data Matrix Code zuweist$")
  public void whenAssignDataMatrixCodeFromStack(
      String patientName, String order, String dmcStack, String pharmacyName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    when(thePatient)
        .attemptsTo(HandoverDataMatrixCode.fromStack(dmcStack).with(order).to(thePharmacy));
  }

  @Wenn(
      "^(?:der|die) Versicherte (?:sein|ihr) (letztes|erstes) (ausgestellte|gelöschte) E-Rezept der"
          + " Apotheke (.+) via Data Matrix Code zuweist$")
  public void whenAssignDataMatrixCodeFromStack(
      String order, String dmcStack, String pharmacyName) {
    val thePatient = OnStage.theActorInTheSpotlight();
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    when(thePatient)
        .attemptsTo(HandoverDataMatrixCode.fromStack(dmcStack).with(order).to(thePharmacy));
  }

  @Dann("^hat (?:der|die) Versicherte (.+) noch kein E-Rezept über DMC erhalten$")
  public void thenDidNotReceiveDmc(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient).attemptsTo(Ensure.that(HasDataMatrixCodes.exactly(0)).isTrue());
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

  @Wenn(
      "^(?:der|die) Versicherte (.+) das (letztes|erstes) (?:ihm|ihr) zugewiesene E-Rezept"
          + " herunterlädt$")
  public void whenDownloadPrescription(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(RetrievePrescriptionFromServer.andChooseWith(order));
  }

  /**
   * Der angegebene Patient ruft das letzte verschriebene Rezept auf dem Patientenstapel beim FD ab
   *
   * @see <a href="https://service.gematik.de/browse/TMD-1605">TMD-1605</a>
   * @param patientName ist der Name des Versicherten
   */
  @Dann("^wird (?:der|dem) Versicherten (.+) das neue E-Rezept angezeigt$")
  @Dann("^wird (?:der|dem) Versicherten (.+) das neue E-Rezept ohne AccessCode angezeigt$")
  public void thenFetchPrescriptionFromBackend(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(Ensure.that(TheLastPrescription.prescribed().existsInBackend()).isTrue());
  }

  /**
   * Der angegebene Patient ruft das letzte verschriebene Rezept auf dem Patientenstapel beim FD ab
   *
   * @see <a href="https://service.gematik.de/browse/TMD-1605">TMD-1605</a>
   */
  @Dann("^wird (?:der|dem) Versicherten das neue E-Rezept angezeigt$")
  public void thenFetchPrescriptionFromBackend() {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient)
        .attemptsTo(Ensure.that(TheLastPrescription.prescribed().existsInBackend()).isTrue());
  }

  /**
   * Negierung von TMD-1605
   *
   * @param patientName ist der Name des Versicherten
   */
  @Dann(
      "^wird (?:der|dem) Versicherten (.+) (?:sein|ihr) letztes (ausgestellte|gelöschte) E-Rezept"
          + " nicht mehr angezeigt$")
  public void thenPrescriptionNotDisplayed(String patientName, String stack) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(Ensure.that(TheLastPrescription.from(stack).existsInBackend()).isFalse());
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
      "^kann (?:der|die) Versicherte (.+) (\\d+) Dispensierinformation(?:en)? für (?:sein|ihr)"
          + " (erstes|letztes) E-Rezept abrufen$")
  public void thenPatientGetsMedicationDispense(String patientName, long amount, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            Ensure.that(
                    MedicationDispenseContains.forThePatient()
                        .andPrescription(order)
                        .numberOfMedicationDispenses(amount))
                .isTrue());
  }
}
