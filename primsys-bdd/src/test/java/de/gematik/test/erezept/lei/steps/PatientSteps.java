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

import static net.serenitybdd.screenplay.GivenWhenThen.and;
import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.screenplay.questions.HasDataMatrixCodes;
import de.gematik.test.erezept.screenplay.questions.HasDispensedDrugs;
import de.gematik.test.erezept.screenplay.questions.HasReceivedCommunication;
import de.gematik.test.erezept.screenplay.questions.HisInsuranceType;
import de.gematik.test.erezept.screenplay.questions.HistSentCommunications;
import de.gematik.test.erezept.screenplay.questions.MedicationDispenseContains;
import de.gematik.test.erezept.screenplay.questions.PatientDoesHaveMessagesForTask;
import de.gematik.test.erezept.screenplay.questions.ResponseOfAbortOperation;
import de.gematik.test.erezept.screenplay.questions.ResponseOfGetCommunicationFrom;
import de.gematik.test.erezept.screenplay.questions.ResponseOfPostCommunication;
import de.gematik.test.erezept.screenplay.questions.TheLastPrescription;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.task.AbortPrescription;
import de.gematik.test.erezept.screenplay.task.AlternativelyAssign;
import de.gematik.test.erezept.screenplay.task.CheckTheReturnCode;
import de.gematik.test.erezept.screenplay.task.DeleteAllSentCommunications;
import de.gematik.test.erezept.screenplay.task.DeleteSentCommunication;
import de.gematik.test.erezept.screenplay.task.HandoverDataMatrixCode;
import de.gematik.test.erezept.screenplay.task.RedeemPrescription;
import de.gematik.test.erezept.screenplay.task.RetrievePrescriptionFromServer;
import de.gematik.test.erezept.screenplay.task.SendCommunication;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.core.PendingStepException;
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
      "^(?:der|die) Versicherte (.+) die Versicherungsart (GKV|PKV|BG|SEL|SOZ|GPV|PPV|BEI) aufweist$")
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
      "^(?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) (ausgestellte|gelöschte) E-Rezept der Apotheke (.+) via Data Matrix Code zuweist$")
  public void whenAssignDataMatrxiCodeFromStack(
      String patientName, String order, String dmcStack, String pharmacyName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    when(thePatient)
        .attemptsTo(HandoverDataMatrixCode.fromStack(dmcStack).with(order).to(thePharmacy));
  }

  @Wenn(
      "^(?:der|die) Versicherte (?:sein|ihr) (letztes|erstes) (ausgestellte|gelöschte) E-Rezept der Apotheke (.+) via Data Matrix Code zuweist$")
  public void whenAssignDataMatrxiCodeFromStack(
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
      "^hat (?:der|die) Versicherte (.+) (mindestens|maximal|genau) (\\d+) Medikament(?:e)? erhalten$")
  public void thenReceivedDrugs(String patientName, String adverb, long amount) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient).attemptsTo(Ensure.that(HasDispensedDrugs.of(adverb, amount)).isTrue());
  }

  @Und(
      "^(?:der|die) Versicherte (.+) hat (mindestens|maximal|genau) (\\d+) Medikament(?:e)? erhalten$")
  public void andReceivedDrugs(String patientName, String adverb, long amount) {
    val thePatient = OnStage.theActorCalled(patientName);
    and(thePatient).attemptsTo(Ensure.that(HasDispensedDrugs.of(adverb, amount)).isTrue());
  }

  @Wenn(
      "^(?:der|die) Versicherte (.+) das (letztes|erstes) (?:ihm|ihr) zugewiesene E-Rezept herunterlädt$")
  public void whenDownloadPrescription(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(RetrievePrescriptionFromServer.andChooseWith(order));
  }

  @Wenn(
      "^(?:der|die) Versicherte (.+) das (letztes|erstes) heruntergeladene E-Rezept der Apotheke (.+) zuweist$")
  public void whenAssignPrescription(String patientName, String order, String pharmName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacist = OnStage.theActorCalled(pharmName);
    when(thePatient)
        .attemptsTo(RedeemPrescription.assign(thePharmacist, DequeStrategy.fromString(order)));
  }

  @Wenn(
      "^(?:der|die) Versicherte (.+) für das (letztes|erstes) heruntergeladene E-Rezept eine Anfrage an die Apotheke (.+) schickt$")
  public void whenReserveRequest(String patientName, String order, String pharmName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacist = OnStage.theActorCalled(pharmName);
    when(thePatient)
        .attemptsTo(RedeemPrescription.reserve(thePharmacist, DequeStrategy.fromString(order)));
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
      "^wird (?:der|dem) Versicherten (.+) (?:sein|ihr) letztes (ausgestellte|gelöschte) E-Rezept nicht mehr angezeigt$")
  public void thenPrescriptionNotDisplayed(String patientName, String stack) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(Ensure.that(TheLastPrescription.from(stack).existsInBackend()).isFalse());
  }

  @Dann(
      "^wird das letzte (ausgestellte|gelöschte) E-Rezept (?:dem|der) Versicherten nicht mehr angezeigt$")
  public void thenPrescriptionNotDisplayed(String stack) {
    val thePatient = OnStage.theActorInTheSpotlight();
    then(thePatient)
        .attemptsTo(Ensure.that(TheLastPrescription.from(stack).existsInBackend()).isFalse());
  }

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

  /**
   * TMD-1640
   *
   * @param patientName
   * @param order
   * @param pharmName
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept der Apotheke (.+) per Nachricht zuweist$")
  public void whenRequestDispenseViaCommunication(
      String patientName, String order, String pharmName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePatient)
        .attemptsTo(
            SendCommunication.with(
                ResponseOfPostCommunication.dispenseRequest()
                    .forPrescriptionFromBackend(order)
                    .sentTo(thePharmacy)
                    .withRandomMessage()));
  }

  /**
   * TMD-1644
   *
   * @param patientName
   * @param order
   * @param pharmName
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) zu (?:seinem|ihrem) (letzten|ersten) E-Rezept der Apotheke (.+) eine Anfrage schickt$")
  public void whenRequestInformationViaCommunication(
      String patientName, String order, String pharmName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePatient)
        .attemptsTo(
            SendCommunication.with(
                ResponseOfPostCommunication.infoRequest()
                    .forPrescriptionFromBackend(order) // letzte | erste
                    .sentTo(thePharmacy)
                    .withRandomMessage()));
  }

  @Wenn(
      "^(?:der|die) Versicherte zu (?:seinem|ihrem) (letzten|ersten) E-Rezept der Apotheke (.+) eine Anfrage schickt$")
  public void whenRequestInformationViaCommunication(String order, String pharmName) {
    val thePatient = OnStage.theActorInTheSpotlight();
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePatient)
        .attemptsTo(
            SendCommunication.with(
                ResponseOfPostCommunication.infoRequest()
                    .forPrescriptionFromBackend(order) // letzte | erste
                    .sentTo(thePharmacy)
                    .withRandomMessage()));
  }

  /**
   * TMD-1646
   *
   * @param patientName
   * @param pharmName
   */
  @Dann("^hat (?:der|die) Versicherte (.+) eine Antwort von der Apotheke (.+) erhalten$")
  public void thenHasReceivedResponseFrom(String patientName, String pharmName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePatient)
        .attemptsTo(Ensure.that(HasReceivedCommunication.reply().from(thePharmacy)).isTrue());
  }

  @Dann(
      "^hat (?:der|die) Versicherte (.+) keine Antwort von der Apotheke (.+) für das (letzte|erste) E-Rezept erhalten$")
  public void thenHasNotReceivedResponseFrom(String patientName, String pharmName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmName);

    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfGetCommunicationFrom.sender(thePharmacy).onStack(order))
                .isEqualTo(404));
  }

  @Dann("^hat (?:der|die) Versicherte eine Antwort von der Apotheke (.+) erhalten$")
  public void thenHasReceivedResponseFrom(String pharmName) {
    val thePatient = OnStage.theActorInTheSpotlight();
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePatient)
        .attemptsTo(Ensure.that(HasReceivedCommunication.reply().from(thePharmacy)).isTrue());
  }

  /**
   * TMD-1648
   *
   * @param patientName
   * @param representativeName
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept per Nachricht an (?:den Vertreter|die Vertreterin) (.+) schickt$")
  public void whenSendCommunicationRepresentative(
      String patientName, String order, String representativeName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val theRepresentative = OnStage.theActorCalled(representativeName);
    when(thePatient)
        .attemptsTo(
            SendCommunication.with(
                ResponseOfPostCommunication.representative()
                    .forPrescriptionFromBackend(order) // letzte | erste
                    .sentTo(theRepresentative)
                    .withRandomMessage()));
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept nicht per Nachricht an (?:den Vertreter|die Vertreterin) (.+) schicken$")
  public void thenCanotSendCommunicationRepresentative400(
      String patientName, String order, String representativeName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val theRepresentative = OnStage.theActorCalled(representativeName);
    then(thePatient)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfPostCommunication.representative()
                        .forPrescriptionFromBackend(order) // letzte | erste
                        .sentTo(theRepresentative)
                        .withRandomMessage())
                .isEqualTo(400));
  }

  /**
   * Der Step prüft, dass für den Versicherten eine Nachricht vom Typ ChargChangeReplay von der
   * Apotheke vorliegt
   *
   * @param patientName ist der Name des Versicherten Patienten
   * @param pharmName ist der Name der Apotheke, von der die Antwort erwartet wird
   */
  @Dann(
      "^hat (?:der|die) Versicherte (.+) eine Antwort auf (?:seinen|ihren) Änderungswunsch von der Apotheke (.+) erhalten$")
  public void thenHasReceivedResponseToChangeRequestFrom(String patientName, String pharmName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmName);

    then(thePatient)
        .attemptsTo(Ensure.that(HasReceivedCommunication.changeReply().from(thePharmacy)).isTrue());
  }

  /**
   * Der Step erzeugt und zeigt den DMC zum erzeugten E-Rezept, damit er mit dem E-Rezept
   * eingescannt werden kann
   *
   * @param patientName
   */
  @Wenn("^(?:der Versicherte|die Versicherte) (.+) ein DMC zum Rezept erhält$")
  public void whenDmcIsGivenToPatient(String patientName) {
    throw new PendingStepException("Not yet implemented");
  }

  /**
   * Manueller Teststep zum Auslösen der alternativen Zuweisung im FdV
   *
   * @param patientName
   * @param order
   * @param pharmName
   * @param option
   */
  @Wenn(
      "^(?:der Versicherte|die Versicherte) (.+) für das (letzte|erste) E-Rezept die alternative Zuweisung an die Apotheke (.+) mit der Option (.+) auslöst$")
  public void whenPatientInitiatsAlternativeAssignment(
      String patientName, String order, String pharmName, String option) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmName);

    when(thePatient)
        .attemptsTo(
            AlternativelyAssign.thePrescriptionReceived(order).to(thePharmacy).with(option));
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) (\\d+) Dispensierinformation(?:en)? für (?:sein|ihr) (erstes|letztes) E-Rezept abrufen$")
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

  @Dann(
      "^kann (?:der|die) Versicherte (.+) nicht mehr die Nachrichten zu (?:seinem|ihrem) (ersten|letzten) E-Rezept abrufen$")
  public void thenPatientCannotGetCommunicationsBasedOnTask(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(Ensure.that(PatientDoesHaveMessagesForTask.fromStack(order)).isFalse());
  }

  @Wenn("^(?:der|die) Versicherte (.+) alle (?:seinem|ihre) versendeten Nachrichten löscht$")
  public void whenPatientDeletesAllCommunications(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(DeleteAllSentCommunications.fromBackend());
  }

  @Wenn("^(?:der|die) Versicherte (.+) (?:ihre|seine) (letzte|erste) versendete Nachricht löscht$")
  public void whenPharmacyDeletesCommunication(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePatient).attemptsTo(DeleteSentCommunication.fromStack(order));
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) keine (?:ihrer|seiner) versendeten Nachrichten mehr abrufen$")
  public void thenPatientCannotGetAnyCommunications(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(Ensure.that(HistSentCommunications.onBackend().noneExistAnymore()).isTrue());
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) (?:ihre|seine) (letzte|erste) Nachricht nicht mehr abrufen$")
  public void thenPatientCannotGetASentCommunication(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            Ensure.that(HistSentCommunications.onBackend().fromQueueStillExists(order)).isFalse());
  }

  @Dann(
      "^hat (?:der|die) Versicherte (.+) für das (letzte|erste) dispensiert E-Rezept im Zugriffsprotokoll einen Protokolleintrag$")
  public void thenPatientHasNewEntryInAccessProtocolForPrescription(
      String patientName, String order) {
    throw new PendingStepException("Not yet implemented");
  }

  @Wenn("^(?:der Versicherte|die Versicherte) (.+) alle löschbaren E-Rezepte löscht$")
  public void whenPatientDeletesAllPresciptions(String patientName) {
    throw new PendingStepException("Not yet implemented");
  }

  @Dann(
      "^werden (?:der Versicherten|dem Versicherten) (.+) keine löschbaren Rezepte mehr im FDV angezeigt$")
  public void FdvShowsNoPresciptions(String patientName) {
    throw new PendingStepException("Not yet implemented");
  }
}
