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

import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.screenplay.questions.*;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.task.*;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.Ensure;

public class PatientCommunicationSteps {

  /**
   * TMD-1640
   *
   * @param patientName
   * @param order
   * @param pharmName
   */
  @Wenn(
      "^(?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept der Apotheke (.+) per"
          + " Nachricht zuweist$")
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
      "^(?:der|die) Versicherte (.+) zu (?:seinem|ihrem) (letzten|ersten) E-Rezept der Apotheke"
          + " (.+) eine Anfrage schickt$")
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
      "^(?:der|die) Versicherte zu (?:seinem|ihrem) (letzten|ersten) E-Rezept der Apotheke (.+)"
          + " eine Anfrage schickt$")
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
      "^hat (?:der|die) Versicherte (.+) keine Antwort von der Apotheke (.+) für das (letzte|erste)"
          + " E-Rezept erhalten$")
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
      "^(?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept per Nachricht an (?:den"
          + " Vertreter|die Vertreterin) (.+) schickt$")
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
      "^kann (?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept nicht per"
          + " Nachricht an (?:den Vertreter|die Vertreterin) (.+) schicken$")
  public void thenCannotSendCommunicationRepresentative400(
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
      "^hat (?:der|die) Versicherte (.+) eine Antwort auf (?:seinen|ihren) Änderungswunsch von der"
          + " Apotheke (.+) erhalten$")
  public void thenHasReceivedResponseToChangeRequestFrom(String patientName, String pharmName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmName);

    then(thePatient)
        .attemptsTo(Ensure.that(HasReceivedCommunication.changeReply().from(thePharmacy)).isTrue());
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) nicht mehr die Nachrichten zu (?:seinem|ihrem)"
          + " (ersten|letzten) E-Rezept abrufen$")
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
      "^kann (?:der|die) Versicherte (.+) keine (?:ihrer|seiner) versendeten Nachrichten mehr"
          + " abrufen$")
  public void thenPatientCannotGetAnyCommunications(String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(Ensure.that(HistSentCommunications.onBackend().noneExistAnymore()).isTrue());
  }

  @Dann(
      "^kann (?:der|die) Versicherte (.+) (?:ihre|seine) (letzte|erste) Nachricht nicht mehr"
          + " abrufen$")
  public void thenPatientCannotGetASentCommunication(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient)
        .attemptsTo(
            Ensure.that(HistSentCommunications.onBackend().fromQueueStillExists(order)).isFalse());
  }

  @Wenn(
      "^(?:der|die) Versicherte (.+) das (letztes|erstes) heruntergeladene E-Rezept der Apotheke"
          + " (.+) zuweist$")
  public void whenAssignPrescription(String patientName, String order, String pharmName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacist = OnStage.theActorCalled(pharmName);
    when(thePatient)
        .attemptsTo(RedeemPrescription.assign(thePharmacist, DequeStrategy.fromString(order)));
  }

  @Wenn(
      "^(?:der|die) Versicherte (.+) für das (letztes|erstes) heruntergeladene E-Rezept eine"
          + " Anfrage an die Apotheke (.+) schickt$")
  public void whenReserveRequest(String patientName, String order, String pharmName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacist = OnStage.theActorCalled(pharmName);
    when(thePatient)
        .attemptsTo(RedeemPrescription.reserve(thePharmacist, DequeStrategy.fromString(order)));
  }
}
