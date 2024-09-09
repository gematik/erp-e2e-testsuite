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

package de.gematik.test.erezept.app.steps;

import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.GivenWhenThen.when;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.gematik.test.erezept.app.questions.IsIssued;
import de.gematik.test.erezept.app.questions.ListRedeemedPrescriptions;
import de.gematik.test.erezept.app.questions.RedeemedPrescription;
import de.gematik.test.erezept.app.questions.StatusAndValidity;
import de.gematik.test.erezept.app.questions.TheLastPrescriptionInTheMainScreen;
import de.gematik.test.erezept.app.questions.TheTaskId;
import de.gematik.test.erezept.app.task.DeleteBatchArchivedPrescription;
import de.gematik.test.erezept.app.task.DeleteBatchRedeemablePrescriptions;
import de.gematik.test.erezept.app.task.DeleteRedeemedPrescription;
import de.gematik.test.erezept.app.task.ios.AssignPrescriptionToPharmacyOnIos;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import io.cucumber.java.de.Wenn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.Ensure;

@Slf4j
public class PrescriptionAssignmentSteps {

  @Dann(
      "kann der Versicherte {string} den Status {string} und das Gültigkeitsdatum {string} des"
          + " Rezepts {string} überprüfen")
  public void userCanCheckIfGivenPrescriptionIsValid(
      String user, String status, String validityDate, String prescription) {
    val theAppUser = OnStage.theActorCalled(user);
    then(theAppUser).asksFor(StatusAndValidity.check(status, validityDate, prescription));

    val expectedTaskId =
        SafeAbility.getAbility(theAppUser, ManageDataMatrixCodes.class).getLastDmc().getTaskId();

    then(theAppUser)
        .attemptsTo(
            Ensure.that(
                    TheLastPrescriptionInTheMainScreen.isPresent(
                        prescription, validityDate, status))
                .isTrue());
    then(theAppUser)
        .attemptsTo(
            Ensure.that(TheTaskId.ofTheLastPrescriptionInTheMainScreen())
                .matches(expectedTaskId.getValue()));
  }

  @Wenn(
      "^(?:der|die) Versicherte (.+) (?:sein|ihr) (letztes|erstes) E-Rezept in der App der Apotheke"
          + " (.+) per Nachricht zuweist")
  public void whenAssignPrescriptionViaDispReq(String userName, String order, String pharmacyName) {
    val theAppUser = OnStage.theActorCalled(userName);
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    // TODO: remove me later, this is just for debugging!
    // 160.000.006.629.699.34 / 9de30f5c71eb0ea387251f628b8fd6f6fa2e5b8b2a675437c26f0eb77ad8ae0c
    //        thePharmacy.can(ProvideApoVzdInformation.withName("Apotheke am FlughafenTEST-ONLY"));
    //        thePharmacy.can(ManageCommunications.heExchanges());
    //        theAppUser.abilityTo(ManageDataMatrixCodes.class).appendDmc(
    //            DmcPrescription.ownerDmc(TaskId.from("160.000.006.629.697.40"),
    // AccessCode.fromString(
    //                "beede44bded27aa8e6fff8540ae1a86be7a25a789a36640b80541513a9390131")));

    when(theAppUser)
        .attemptsTo(AssignPrescriptionToPharmacyOnIos.fromStack(order).toPharmacy(thePharmacy));
  }

  @Und("das E-Rezept {string} hat den Status {string}")
  public void dasERezeptHatDenStatus(String receipt, String status) {
    val theAppUser = OnStage.theActorInTheSpotlight();
    val managePrescriptions = SafeAbility.getAbility(theAppUser, ManageDataMatrixCodes.class);
    assertTrue(
        theAppUser.asksFor(
            RedeemedPrescription.lastRedeemedPrescriptionWithStatusAndTaskId(
                receipt, status, managePrescriptions.getLastDmc().getTaskId().getValue())));
  }

  @Und("das E-Rezept {string} enthält das Einlösedatum")
  public void dasERezeptEnthältDasEinlösedatum(String receipt) {
    val theAppUser = OnStage.theActorInTheSpotlight();
    assertTrue(theAppUser.asksFor(IsIssued.today()));
  }

  @Wenn("der Versicherte kann das eingelöste Rezept erfolgreich entfernen")
  public void derVersicherteKannDasEingelösteRezeptErfolgreichEntfernen() {
    val theAppUser = OnStage.theActorInTheSpotlight();

    when(theAppUser).attemptsTo(DeleteRedeemedPrescription.insideTheApp());
  }

  @Und("keine Rezepte im Zustand Einlösbar oder Eingelöst in {string} App")
  public void keineRezepteImZustandEinlösbarOderEingelöstInAliceApp(String user) {
    val theAppUser = OnStage.theActorCalled(user);
    then(theAppUser).attemptsTo(DeleteBatchRedeemablePrescriptions.insideTheApp());

    then(theAppUser).attemptsTo(DeleteBatchArchivedPrescription.insideTheApp());
  }

  @Dann("das E-Rezept {string} ist nicht mehr Teil des eingelöste Rezepte Mainscreens")
  public void dasERezeptIstNichtMehrTeilDesEingelösteRezepteMainscreens(String arg0) {
    val theAppUser = OnStage.theActorInTheSpotlight();
    assertFalse(theAppUser.asksFor(ListRedeemedPrescriptions.isEmpty()));
  }
}
