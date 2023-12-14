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

package de.gematik.test.erezept.app.steps;

import static net.serenitybdd.screenplay.GivenWhenThen.*;
import static org.junit.Assert.*;

import de.gematik.test.erezept.app.questions.*;
import de.gematik.test.erezept.app.task.DeleteBatchArchivedPrescription;
import de.gematik.test.erezept.app.task.DeleteBatchRedeemablePrescriptions;
import de.gematik.test.erezept.app.task.DeleteRedeemedPrescription;
import de.gematik.test.erezept.app.task.NavigateThroughRedeemablePrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import io.cucumber.java.PendingException;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.*;

@Slf4j
public class PrescriptionAssignmentSteps {

  @Angenommen(
    "der Versicherte {string} löst das E-Rezept {string} per Reservierung bei der Apotheke {string} ein")
  public void derUserLöstDasERezeptPerReservierungBeiDerEin(
    String user, String eRezep, String pharmacy) {
    val theAppUser = OnStage.theActorCalled(user);
    val thePharmacyActor = OnStage.theActorCalled(pharmacy);
    givenThat(theAppUser)
      .attemptsTo(NavigateThroughRedeemablePrescriptions.redeemTo(thePharmacyActor));
  }

  @Dann(
    "kann der Versicherte {string} den Status {string} und das Gültigkeitsdatum {string} des Rezepts {string} überprüfen")
  public void userCanCheckIfGivenPrescriptionIsValid(
    String user, String status, String validityDate, String prescription) {
    val theAppUser = OnStage.theActorCalled(user);
    then(theAppUser).asksFor(StatusAndValidity.check(status, validityDate, prescription));

    val expectedTaskId = SafeAbility.getAbility(theAppUser, ManageDataMatrixCodes.class).getLastDmc().getTaskId();

    then(theAppUser).attemptsTo(Ensure.that(TheLastPrescriptionInTheMainScreen.isPresent(prescription,validityDate,status)).isTrue());
    then(theAppUser).attemptsTo(Ensure.that(TheTaskId.ofTheLastPrescriptionInTheMainScreen()).matches(expectedTaskId.getValue()));
  }

  @Und("das E-Rezept {string} kann nicht gelöscht werden")
  public void dasERezeptKannNichtGelöschtWerden(String eRezep) {
    val theAppUser = OnStage.theActorInTheSpotlight();
    then(theAppUser).attemptsTo(Ensure.that(ThePrescriptionInRedemption.canNotBeDeleted(eRezep)).isTrue());
  }

  @Und("das E-Rezept {string} kann nicht eingelöst werden")
  public void dasERezeptKannNichtEingelöstWerden(String eRezep) {
    val theAppUser = OnStage.theActorInTheSpotlight();

    if (!theAppUser.asksFor(IsRedeemable.inMainScreen())) {
      log.info("redeem button not present in the main screen");
    } else {
      throw new PendingException("accesibilityIds not implemented in cose yet");
    }
  }

  @Deprecated
  @Dann("ist das E-Rezept {string} nicht mehr in den Aktuellen Rezepten des Versicherte {string}")
  public void istDasERezeptNichtMehrInDenAktuellenRezeptenDesVersicherte(String receipt, String user) {
    val theAppUser = OnStage.theActorCalled(user);
    val expectedTaskId = SafeAbility.getAbility(theAppUser, ManageDataMatrixCodes.class).getLastDmc().getTaskId();

    then(theAppUser).asksFor(TheLastPrescriptionInTheMainScreen.waitTillIsGone());
    if (then(theAppUser).asksFor(PrescriptionList.isThereElements()))
      then(theAppUser).attemptsTo(Ensure.that(TheTaskId.ofTheLastPrescriptionInTheMainScreen()).doesNotContain(expectedTaskId.getValue()));
  }
  
  @Deprecated
  @Dann("kann die Versicherte {string} das letzte entfernte E-Rezept nicht mehr abrufen")
  public void istDasERezeptInDenAktuellenRezeptenDesVersicherteNichtMehrAnrufen(String user){
    istDasERezeptNichtMehrInDenAktuellenRezeptenDesVersicherte(null,user);
  }

  @Und("das E-Rezept {string} hat den Status {string}")
  public void dasERezeptHatDenStatus(String receipt, String status) {
    val theAppUser = OnStage.theActorInTheSpotlight();
    val managePrescriptions = SafeAbility.getAbility(theAppUser, ManageDataMatrixCodes.class);
    assertTrue(theAppUser
      .asksFor(
        RedeemedPrescription.lastRedeemedPrescriptionWithStatusAndTaskId(
          receipt, status, managePrescriptions.getLastDmc().getTaskId().getValue())));
  }

  @Und("das E-Rezept {string} enthält das Einlösedatum")
  public void dasERezeptEnthältDasEinlösedatum(String receipt) {
    val theAppUser = OnStage.theActorInTheSpotlight();
    assertTrue(theAppUser
      .asksFor(
        IsIssued.today()));
  }

  @Wenn("der Versicherte kann das eingelöste Rezept erfolgreich entfernen")
  public void derVersicherteKannDasEingelösteRezeptErfolgreichEntfernen() {
    val theAppUser = OnStage.theActorInTheSpotlight();

    when(theAppUser)
      .attemptsTo(DeleteRedeemedPrescription.insideTheApp());
  }

  @Und("keine Rezepte im Zustand Einlösbar oder Eingelöst in {string} App")
  public void keineRezepteImZustandEinlösbarOderEingelöstInAliceApp(String user) {
    val theAppUser = OnStage.theActorCalled(user);
    then(theAppUser)
      .attemptsTo(DeleteBatchRedeemablePrescriptions.insideTheApp());

    then(theAppUser)
      .attemptsTo(DeleteBatchArchivedPrescription.insideTheApp());
  }

  @Dann("das E-Rezept {string} ist nicht mehr Teil des eingelöste Rezepte Mainscreens")
  public void dasERezeptIstNichtMehrTeilDesEingelösteRezepteMainscreens(String arg0) {
    val theAppUser = OnStage.theActorInTheSpotlight();
    assertFalse(theAppUser.asksFor(ListRedeemedPrescriptions.isEmpty()));
  }


}
