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

import de.gematik.test.erezept.app.mobile.EVDGAStatus;
import de.gematik.test.erezept.app.task.EnsureThatTheEVDGAPrescription;
import de.gematik.test.erezept.app.task.ios.DeleteAnEVDGAOnIOS;
import de.gematik.test.erezept.app.task.ios.ReceiveAnEVDGACodeOnIOS;
import de.gematik.test.erezept.app.task.ios.RequestAnEVDGAonIOS;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

public class EVDGAPrescriptionSteps {

  @Wenn(
      "^(?:der|die) GKV Versicherte (.+) (?:sein|ihr) (letztes|erstes) ausgestelltes "
          + "EVDGA E-Rezept in der App anzeigen kann$")
  public void thenCheckEVDGAPrescription(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient).attemptsTo(EnsureThatTheEVDGAPrescription.fromStack(order).isShownCorrectly());
  }

  @Wenn(
      "^(?:der|die) GKV Versicherte (.+) (?:sein|ihr) (letztes|erstes) ausgestelltes EVDGA E-Rezept"
          + " per App (?:seinem|ihrem) Kostenträger (.+) zuweist$")
  public void thenRequestEVDGAPrescription(String patientName, String order, String ktrName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val theInsurance = OnStage.theActorCalled(ktrName);
    then(thePatient).attemptsTo(RequestAnEVDGAonIOS.fromStack(order).from(theInsurance));
  }

  @Wenn(
      "^(?:der|die) GKV Versicherte (.+) den Freischaltcode der (.+) für (?:seine|ihre)"
          + " (letzte|erste) bereitgestellte EVDGA per App abrufen kann$")
  @Und(
      "^(?:der|die) GKV Versicherte (.+) kann den Freischaltcode für (?:seine|ihre)"
          + " (letzte|erste) bereitgestellte EVDGA per App abrufen$")
  public void thenReceiveEVDGACode(String patientName, String order) {
    val thePatient = OnStage.theActorCalled(patientName);
    then(thePatient).attemptsTo(ReceiveAnEVDGACodeOnIOS.fromStack(order));
  }

  @Wenn(
      "^(?:der|die) GKV Versicherte (.+) die (letzte|erste)"
          + " (bestellbare|abgelehnte|bereitgestellte) EVDGA in der App löschen kann$")
  @Und(
      "^(?:der|die) GKV Versicherte (.+) kann die (letzte|erste)"
          + " (bestellbare|abgelehnte|bereitgestellte) EVDGA in der App löschen$")
  public void thenDeleteEVDGAPrescription(String patientName, String order, String status) {
    val thePatient = OnStage.theActorCalled(patientName);
    EVDGAStatus appStatus;
    if ("bestellbare".equals(status)) {
      appStatus = EVDGAStatus.READY_FOR_REQUEST;
    } else if ("abgelehnte".equals(status)) {
      appStatus = EVDGAStatus.DECLINED;
    } else {
      appStatus = EVDGAStatus.GRANTED;
    }

    then(thePatient).attemptsTo(DeleteAnEVDGAOnIOS.fromStack(order).with(appStatus));
  }
}
