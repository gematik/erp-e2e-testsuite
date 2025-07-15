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

import static net.serenitybdd.screenplay.GivenWhenThen.then;

import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.questions.HasReceipts;
import de.gematik.test.erezept.screenplay.task.RetrieveReceiptAgain;
import de.gematik.test.erezept.screenplay.task.VerifyReceiptSignature;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Und;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.Ensure;

public class PharmacyReceiptSteps {

  @Dann("^hat die Apotheke (.+) (mindestens|maximal|genau) eine Quittung vorliegen$")
  @Und("^die Apotheke (.+) hat (mindestens|maximal|genau) eine Quittung vorliegen$")
  public void hasOneReceipt(String pharmName, String adverb) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy).attemptsTo(Ensure.that(HasReceipts.of(adverb, 1)).isTrue());
  }

  @Dann("^hat die Apotheke (mindestens|maximal|genau) eine Quittung vorliegen$")
  @Und("^die Apotheke hat (mindestens|maximal|genau) eine Quittung vorliegen$")
  public void hasOneReceipt(String adverb) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy).attemptsTo(Ensure.that(HasReceipts.of(adverb, 1)).isTrue());
  }

  @Dann("^hat die Apotheke (.+) (mindestens|maximal|genau) (\\d+) Quittung(?:en)? vorliegen$")
  @Und("^die Apotheke (.+) hat (mindestens|maximal|genau) (\\d+) Quittung(?:en)? vorliegen$")
  public void hasReceipts(String pharmName, String adverb, long amount) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy).attemptsTo(Ensure.that(HasReceipts.of(adverb, amount)).isTrue());
  }

  @Dann("^hat die Apotheke (mindestens|maximal|genau) (\\d+) Quittung(?:en)? vorliegen$")
  @Und("^die Apotheke hat (mindestens|maximal|genau) (\\d+) Quittung(?:en)? vorliegen$")
  public void hasReceipts(String adverb, long amount) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy).attemptsTo(Ensure.that(HasReceipts.of(adverb, amount)).isTrue());
  }

  @Dann("^hat die Apotheke (mindestens|maximal|genau) (\\d+) Quittung(?:en)? für (.+) vorliegen$")
  @Und("^die Apotheke hat (mindestens|maximal|genau) (\\d+) Quittung(?:en)? für (.+) vorliegen$")
  public void hasReceiptFor(String adverb, long amount, String patientName) {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);
    val kvnr = SafeAbility.getAbility(thePatient, ProvideEGK.class).getKvnr();

    then(thePharmacy)
        .attemptsTo(Ensure.that(HasReceipts.forPatient(adverb, amount, kvnr)).isTrue());
  }

  @Dann(
      "^hat die Apotheke (.+) (mindestens|maximal|genau) (\\d+) Quittung(?:en)? für (.+)"
          + " vorliegen$")
  @Und(
      "^die Apotheke hat (.+) (mindestens|maximal|genau) (\\d+) Quittung(?:en)? für (.+)"
          + " vorliegen$")
  public void hasReceiptFor(String pharmName, String adverb, long amount, String patientName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    val thePatient = OnStage.theActorCalled(patientName);
    val kvnr = SafeAbility.getAbility(thePatient, ProvideEGK.class).getKvnr();

    then(thePharmacy)
        .attemptsTo(Ensure.that(HasReceipts.forPatient(adverb, amount, kvnr)).isTrue());
  }

  @Dann("^hat die Apotheke (.+) keine Quittung vorliegen$")
  @Und("^die Apotheke (.+) hat(?: noch)? keine Quittung vorliegen$")
  public void hasNoReceipt(String pharmName) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy).attemptsTo(Ensure.that(HasReceipts.atMost(0)).isTrue());
  }

  @Dann("^hat die Apotheke(?: noch)? keine Quittung vorliegen$")
  @Und("^die Apotheke hat(?: noch)? keine Quittung vorliegen$")
  public void hasNoReceipt() {
    val thePharmacy = OnStage.theActorInTheSpotlight();
    then(thePharmacy).attemptsTo(Ensure.that(HasReceipts.atMost(0)).isTrue());
  }

  /**
   * TMD-1673
   *
   * @param pharmName ist der Name der Apotheke, die ihre Quittung validieren soll
   */
  @Dann(
      "^kann die Apotheke (.+) die Signatur der (letzten|ersten) Quittung erfolgreich mit dem"
          + " Konnektor validieren$")
  public void thenPharmacySuccessfullyValidatesSignatureofReceipt(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy).attemptsTo(VerifyReceiptSignature.fromStack(order));
  }

  /**
   * versendet HTTP-GET-Operation auf einen einzelnen Task mittels "/Task/<id>?secret=..." FD gibt
   * Task + Quittungs-Bundle an den Apotheker zurück
   *
   * @param pharmName
   */
  @Dann("^kann die Apotheke (.+) die (letzte|erste) Quittung erneut abrufen$")
  public void thenPharmacyCanRetrieveReceiptAgain(String pharmName, String order) {
    val thePharmacy = OnStage.theActorCalled(pharmName);
    then(thePharmacy).attemptsTo(RetrieveReceiptAgain.fromStack(order));
  }
}
