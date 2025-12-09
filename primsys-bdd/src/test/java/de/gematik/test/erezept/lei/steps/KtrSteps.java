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

package de.gematik.test.erezept.lei.steps;

import static de.gematik.test.erezept.lei.steps.ActorsInitializationSteps.config;
import static net.serenitybdd.screenplay.GivenWhenThen.*;

import de.gematik.test.erezept.screenplay.questions.*;
import de.gematik.test.erezept.screenplay.questions.DispenseDigaPrescription;
import de.gematik.test.erezept.screenplay.questions.HasNewSubscriptionPing;
import de.gematik.test.erezept.screenplay.questions.ResponseOfAbortOperation;
import de.gematik.test.erezept.screenplay.questions.ResponseOfAcceptDispenseRequestOperation;
import de.gematik.test.erezept.screenplay.task.*;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.Ensure;

public class KtrSteps {

  @Dann(
      "^kann der Kostenträger (.+) das (erste|letzte) EVDGA E-Rezept (?:der|des) Versicherten (.+)"
          + " akzeptieren$")
  @Und(
      "^der Kostenträger (.+) das (erste|letzte) EVDGA (?:der|des) Versicherten (.+)"
          + " akzeptiert$")
  public void andThenKtrAcceptDigaPrescription(String ktrName, String order, String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val theKtr = OnStage.theActorCalled(ktrName);

    then(theKtr).attemptsTo(AcceptDispenseRequest.of(order).from(thePatient));
  }

  @Und(
      "^der Kostenträger (.+) kann für die (erste|letzte) EVDGA (?:der|des) Versicherten (.+)"
          + " Abgabeinformationen mit Freischaltcode bereitstellen$")
  public void andKtrProvideDispenseInformation(String ktrName, String order, String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val theKtr = OnStage.theActorCalled(ktrName);

    then(theKtr).attemptsTo(CloseDigaPrescription.of(order).to(thePatient));
  }

  @Und(
      "^der Kostenträger (.+) kann für die (erste|letzte) EVDGA (?:der|des) Versicherten (.+)"
          + " Abgabeinformationen ohne Freischaltcode bereitstellen$")
  public void andKtrProvideDispenseInformationWithoutRedeemCode(
      String ktrName, String order, String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val theKtr = OnStage.theActorCalled(ktrName);
    then(theKtr).attemptsTo(CloseDigaPrescriptionWithoutRedeemCode.of(order).to(thePatient));
  }

  @Dann("^kann der Kostenträger (.+) das (erste|letzte) akzeptierte EVDGA zurückweisen$")
  public void thenKtrRejectPrescription(String ktrName, String order) {
    val theKtr = OnStage.theActorCalled(ktrName);

    then(theKtr).attemptsTo(RejectPrescription.fromStack(order));
  }

  @Und(
      "^der Kostenträger (.+) kann für die (erste|letzte) akzeptierte EVDGA keine zeitnahe"
          + " Dispensierung durchführen$")
  public void andKtrCantDispenseTimelyManner(String ktrName, String order) {
    val theKtr = OnStage.theActorCalled(ktrName);
    and(theKtr)
        .attemptsTo(
            CheckTheReturnCode.of(DispenseDigaPrescription.forThePrescription(order))
                .isEqualTo(403));
  }

  @Dann(
      "^kann der Kostenträger (.+) das (erste|letzte) E-Rezept der Versicherten (.+) nicht"
          + " akzeptieren$")
  public void thenKtrCannotAcceptWF160Prescription(
      String ktrName, String order, String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val theKtr = OnStage.theActorCalled(ktrName);
    then(theKtr)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfAcceptDispenseRequestOperation.forThePrescription(order)
                        .fromPatient(thePatient))
                .isEqualTo(403));
  }

  @Dann("^kann der Kostenträger (.+) das (erste|letzte) akzeptierte EVDGA nicht löschen$")
  public void thenKtrCannotAbortAcceptedEVDGA(String ktrName, String order) {
    val theKtr = OnStage.theActorCalled(ktrName);
    then(theKtr)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asKtr().fromStack(order))
                .isEqualTo(403));
  }

  @Wenn("^der Kostenträger (.+) sich beim E-Rezept Fachdienst registriert$")
  public void whenRegisterForSubscription(String ktrName) {
    val theKtr = OnStage.theActorCalled(ktrName);
    when(theKtr).attemptsTo(RegisterNewSubscription.forCriteria("Communication"));
    when(theKtr)
        .attemptsTo(
            ConnectSubscriptionService.connect(
                config.getActiveEnvironment().getTi().getSubscriptionServiceUrl()));
  }

  @Dann("^wird der Kostenträger (.+) über die neue Zuweisungen informiert$")
  public void hasSubscriptionPing(String ktrName) {
    val theKtr = OnStage.theActorCalled(ktrName);
    then(theKtr).attemptsTo(Ensure.that(HasNewSubscriptionPing.hasNewPing()).isTrue());
  }

  @Dann(
      "^kann der Kostenträger (.+) für das (erste|letzte) akzeptierte EVDGA E-Rezept (?:der|dem)"
          + " Versicherten (.+) eine Begründung senden$")
  public void thenKtrReplaysAReasonToQuestioner(String ktrName, String order, String patientName) {
    val theKtr = OnStage.theActorCalled(ktrName);
    val thePatient = OnStage.theActorCalled(patientName);
    then(theKtr)
        .attemptsTo(
            CheckTheReturnCode.of(
                    ResponseOfReplyAsKtr.replyDiGARequest(order)
                        .sentTo(thePatient)
                        .withMessage(
                            "Die Anfrage zur Ausstellung eines Freischaltcodes für die DiGA wurde"
                                + " abgewiesen, da Sie nicht berechtigt sind.  "))
                .isEqualTo(201));
  }
}
