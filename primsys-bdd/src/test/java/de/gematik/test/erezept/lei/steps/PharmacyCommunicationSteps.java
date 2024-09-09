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

import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.screenplay.questions.HasNewSubscriptionPing;
import de.gematik.test.erezept.screenplay.questions.ResponseOfGetCommunicationFrom;
import de.gematik.test.erezept.screenplay.questions.ResponseOfPostCommunication;
import de.gematik.test.erezept.screenplay.task.*;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.Ensure;

public class PharmacyCommunicationSteps {

  @Wenn("^die Apotheke (.+) sich für die Subscription (.+) registriert$")
  public void whenRegisterForSubscription(String pharmName, String criteria) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy).attemptsTo(RegisterNewSubscription.forCriteria(criteria));
  }

  @Dann("^wird die Apotheke (.+) durch den Subscription Service informiert$")
  public void hasSubscriptionPing(String pharmName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy).attemptsTo(Ensure.that(HasNewSubscriptionPing.hasNewPing()).isTrue());
  }

  /** TMD-1642 */
  @Wenn("^die Apotheke (.+) die (letzte|erste) Zuweisung per Nachricht von (.+) akzeptiert$")
  public void whenAcceptDispenseRequest(String pharmName, String order, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePharmacy).attemptsTo(AcceptDispenseRequest.of(order).from(thePatient));
  }

  /**
   * TMD-1645
   *
   * @param pharmName
   * @param patientName
   */
  @Wenn("^die Apotheke (.+) die (letzte|erste) Nachricht von (.+) beantwortet$")
  public void whenPharmacyAnswersToMessage(String pharmName, String order, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePharmacy)
        .attemptsTo(
            SendCommunication.with(
                ResponseOfPostCommunication.reply()
                    .forCommunicationRequestFromBackend(order) // letzte | erste
                    .receivedFrom(thePatient)
                    .withRandomMessage()));
  }

  @Wenn("^die Apotheke die (letzte|erste) Nachricht von (.+) beantwortet$")
  public void whenPharmacyAnswersToMessage(String order, String patientName) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePharmacy)
        .attemptsTo(
            SendCommunication.with(
                ResponseOfPostCommunication.reply()
                    .forCommunicationRequestFromBackend(order) // letzte | erste
                    .receivedFrom(thePatient)
                    .withRandomMessage()));
  }

  @Dann(
      "^kann (.+) die (letzte|erste) Nachricht von (.+) nicht beantworten, weil sie keine Apotheke"
          + " ist$")
  public void whenPharmacyCannotAnswersToMessage(
      String pharmName, String order, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    when(thePharmacy)
        .attemptsTo(
            Negate.the(
                    SendCommunication.with(
                        ResponseOfPostCommunication.reply()
                            .forCommunicationRequestFromBackend(order)
                            .receivedFrom(thePatient)
                            .withRandomMessage()))
                .with(UnexpectedResponseResourceError.class));
  }

  @Wenn(
      "^die Apotheke (.+) die (letzte|erste) Nachricht (?:der|des) Versicherten (.+) mit dem"
          + " Änderungswunsch empfängt und beantwortet$")
  public void whenReceiveAndAnswerChargeChangeReq(
      String pharmName, String order, String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmName);

    when(thePharmacy)
        .attemptsTo(
            SendCommunication.with(
                ResponseOfPostCommunication.changeReply()
                    .forCommunicationRequestFromBackend(order)
                    .receivedFrom(thePatient)
                    .withRandomMessage()));
  }

  @Dann(
      "^kann die Apotheke (.+) die (letzte|erste) Nachricht von (.+) nicht abrufen, weil die"
          + " Nachricht bereits gelöscht wurde$")
  public void thenCannotGetCommunicationFrom(String pharmName, String order, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);

    then(thePharmacy)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfGetCommunicationFrom.sender(thePatient).onStack(order))
                .isEqualTo(404));
  }

  @Und("^die Apotheke (.+) ihre (letzte|erste) versendete Nachricht löscht$")
  public void whenPharmacyDeletesCommunication(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    when(thePharmacy).attemptsTo(DeleteSentCommunication.fromStack(order));
  }
}
